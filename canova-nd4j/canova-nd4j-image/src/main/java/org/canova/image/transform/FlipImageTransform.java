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
package org.canova.image.transform;

import java.util.Random;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.canova.image.data.ImageWritable;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 *
 * @author saudet
 */
public class FlipImageTransform extends BaseImageTransform<Mat> {

    int flipMode;

    public FlipImageTransform() {
        this(null);
    }

    public FlipImageTransform(int flipMode) {
        this(null);
        this.flipMode = flipMode;
    }

    public FlipImageTransform(Random random) {
        super(random);
        converter = new OpenCVFrameConverter.ToMat();
    }

    @Override
    public ImageWritable transform(ImageWritable image, Random random) {
        Mat mat = converter.convert(image.getFrame());

        int mode = random != null ? random.nextInt(4) - 2 : flipMode;

        Mat result = new Mat();
        if (mode < -1) {
            // no flip
            mat.copyTo(result);
        } else {
            flip(mat, result, mode);
        }

        return new ImageWritable(converter.convert(result));
    }
}
