/*
 *
 *  *
 *  *  * Copyright 2016 Skymind,Inc.
 *  *  *
 *  *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *  *    you may not use this file except in compliance with the License.
 *  *  *    You may obtain a copy of the License at
 *  *  *
 *  *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *    Unless required by applicable law or agreed to in writing, software
 *  *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *    See the License for the specific language governing permissions and
 *  *  *    limitations under the License.
 *  *
 *
 */
package org.canova.image.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.canova.image.data.ImageWritable;
import org.canova.image.transform.ImageTransform;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Uses JavaCV to load images.
 *
 * @author saudet
 */
public class NativeImageLoader extends BaseImageLoader {

    OpenCVFrameConverter.ToMat converter = null;

    public NativeImageLoader() {
    }

    /**
     * Instantiate an image with the given
     * height and width
     * @param height the height to load*
     * @param width  the width to load

     */
    public NativeImageLoader(int height,int width) {
        this.height = height;
        this.width = width;
    }


    /**
     * Instantiate an image with the given
     * height and width
     * @param height the height to load
     * @param width  the width to load
     * @param channels the number of channels for the image*
     */
    public NativeImageLoader(int height, int width, int channels) {
        this.height = height;
        this.width = width;
        this.channels = channels;
    }

    public NativeImageLoader(int height, int width, int channels, boolean centerCropIfNeeded) {
        this(height, width, channels);
        this.centerCropIfNeeded = centerCropIfNeeded;
    }

    public NativeImageLoader(int height, int width, int channels, ImageTransform imageTransform) {
        this(height, width, channels);
        this.imageTransform = imageTransform;
        this.converter = new OpenCVFrameConverter.ToMat();
    }

    /**
     * Convert a file to a row vector
     *
     * @param f the image to convert
     * @return the flattened image
     * @throws IOException
     */
    @Override
    public INDArray asRowVector(File f) throws IOException {
        return asMatrix(f).ravel();
    }

    @Override
    public INDArray asRowVector(InputStream is) throws IOException {
        return asMatrix(is).ravel();
    }

    public INDArray asRowVector(Mat image) throws IOException {
        return asMatrix(image).ravel();
    }

    @Override
    public INDArray asMatrix(File f) throws IOException {
        Mat image = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_ANYDEPTH | CV_LOAD_IMAGE_ANYCOLOR);
        if (image == null) {
            throw new IOException("Could not read image from file: " + f);
        }
        return asMatrix(image);
    }

    @Override
    public INDArray asMatrix(InputStream is) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        Mat image = imdecode(new Mat(bytes), CV_LOAD_IMAGE_ANYDEPTH | CV_LOAD_IMAGE_ANYCOLOR);
        if (image == null) {
            throw new IOException("Could not decode image from input stream");
        }
        return asMatrix(image);
    }

    public INDArray asMatrix(Mat image) throws IOException {
        if (imageTransform != null && converter != null) {
            ImageWritable writable = new ImageWritable(converter.convert(image));
            writable = imageTransform.transform(writable);
            image = converter.convert(writable.getFrame());
        }

        if (channels > 0 && image.channels() != channels) {
            int code = -1;
            switch (image.channels()) {
                case 1:
                    switch (channels) {
                        case 3: code = CV_GRAY2BGR; break;
                        case 4: code = CV_GRAY2RGBA; break;
                    }
                    break;
                case 3:
                    switch (channels) {
                        case 1: code = CV_BGR2GRAY; break;
                        case 4: code = CV_BGR2RGBA; break;
                    }
                    break;
                case 4:
                    switch (channels) {
                        case 1: code = CV_RGBA2GRAY; break;
                        case 3: code = CV_RGBA2BGR; break;
                    }
                    break;
            }
            if (code < 0) {
                throw new IOException("Cannot convert from " + image.channels()
                                                    + " to " + channels + " channels.");
            }
            Mat newimage = new Mat();
            cvtColor(image, newimage, code);
            image = newimage;
        }
        if (centerCropIfNeeded) {
            image = centerCropIfNeeded(image);
        }
        image = scalingIfNeed(image);
        int rows = image.rows();
        int cols = image.cols();
        int channels = image.channels();
        Indexer idx = image.createIndexer();
        INDArray ret = channels > 1 ? Nd4j.create(channels, rows, cols) : Nd4j.create(rows, cols);
        for (int k = 0; k < channels; k++) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (channels > 1) {
                        ret.putScalar(k, i, j, idx.getDouble(i, j, k));
                    } else {
                        ret.putScalar(i, j, idx.getDouble(i, j));
                    }
                }
            }
        }
        return ret;
    }

    // TODO build flexibility on where to crop the image
    protected Mat centerCropIfNeeded(Mat img) {
        int x = 0;
        int y = 0;
        int height = img.rows();
        int width = img.cols();
        int diff = Math.abs(width - height) / 2;

        if (width > height) {
            x = diff;
            width = width - diff;
        } else if (height > width) {
            y = diff;
            height = height - diff;
        }
        return img.apply(new Rect(x, y, width, height));
    }

    protected Mat scalingIfNeed(Mat image) {
        return scalingIfNeed(image, height, width);
    }

    protected Mat scalingIfNeed(Mat image, int dstHeight, int dstWidth) {
        Mat scaled = image;
        if (dstHeight > 0 && dstWidth > 0 && (image.rows() != dstHeight || image.cols() != dstWidth)) {
            resize(image, scaled = new Mat(), new Size(dstWidth, dstHeight));
        }
        return scaled;
    }
}
