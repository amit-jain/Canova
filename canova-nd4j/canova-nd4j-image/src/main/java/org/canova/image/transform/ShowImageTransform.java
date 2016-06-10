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
import org.bytedeco.javacv.CanvasFrame;
import org.canova.image.data.ImageWritable;

/**
 * Shows images on the screen, does not actually transform them.
 *
 * @author saudet
 */
public class ShowImageTransform extends BaseImageTransform {

    CanvasFrame canvas;

    /**
     * Constructs an instance of the ImageTransform from a {@link CanvasFrame}.
     *
     * @param canvas to display images in
     */
    public ShowImageTransform(CanvasFrame canvas) {
        super(null);
        this.canvas = canvas;
    }

    /**
     * Constructs an instance of the ImageTransform with a new {@link CanvasFrame}.
     *
     * @param title of the new CanvasFrame to display images in
     */
    public ShowImageTransform(String title) {
        super(null);
        canvas = new CanvasFrame(title);
    }

    @Override
    public ImageWritable transform(ImageWritable image, Random random) {
        canvas.showImage(image.getFrame());
        return image;
    }
}
