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
import org.canova.image.data.ImageWritable;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 *
 * @author saudet
 */
public class MultiImageTransform extends BaseImageTransform<Mat> {

    ImageTransform[] imageTransforms = null;

    public MultiImageTransform(ImageTransform... transforms) {
        this(null, transforms);
    }

    public MultiImageTransform(Random random, ImageTransform... transforms) {
        super(random);
        imageTransforms = transforms;
    }

    @Override
    public ImageWritable transform(ImageWritable image, Random random) {
        for (int i = 0; i < imageTransforms.length; i++) {
            image = random != null ? imageTransforms[i].transform(image, random)
                                   : imageTransforms[i].transform(image);
        }
        return image;
    }
}
