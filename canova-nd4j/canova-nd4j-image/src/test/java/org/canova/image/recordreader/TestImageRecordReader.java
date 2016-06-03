/*
 *
 *  *
 *  *  * Copyright 2015 Skymind,Inc.
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

package org.canova.image.recordreader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Random;

import org.canova.api.io.filters.BalancedPathFilter;
import org.canova.api.io.filters.RandomPathFilter;
import org.canova.api.io.labels.ParentPathLabelGenerator;
import org.canova.api.io.labels.PatternPathLabelGenerator;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.FileRecordReader;
import org.canova.api.split.BaseInputSplit;
import org.canova.api.split.FileSplit;
import org.canova.api.split.InputSplit;
import org.canova.api.split.InputStreamInputSplit;
import org.canova.api.writable.Writable;
import org.junit.Test;
import org.canova.api.util.ClassPathResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Adam Gibson
 */
public class TestImageRecordReader {

    // TODO fix tests and fix for TravisCI
//    @Test
//    public void testInputStream() throws Exception {
//        RecordReader reader = new ImageRecordReader(28,28,false);
//        // keeps needlessly blowing up
//        ClassPathResource res = new ClassPathResource("/test.jpg");
//        reader.initialize(new InputStreamInputSplit(res.getInputStream(), res.getURI()));
//        assertTrue(reader.hasNext());
//        Collection<Writable> record = reader.next();
//        assertEquals(784,record.size());
//
//    }
//
//    @Test
//    public void testMultipleChannels() throws Exception {
//        RecordReader reader = new ImageRecordReader(28,28,3,false);
//        // keeps needlessly blowing up
//        ClassPathResource res = new ClassPathResource("/test.jpg");
//        reader.initialize(new InputStreamInputSplit(res.getInputStream(), res.getURI()));
//        assertTrue(reader.hasNext());
//        Collection<Writable> record = reader.next();
//        assertEquals(784 * 3,record.size());
//    }
//
//    @Test
//    public void testGetLabel() throws Exception {
//        RecordReader reader = new ImageNameRecordReader(28,28,3,true);
//        // keeps needlessly blowing up
//        ClassPathResource res = new ClassPathResource("/test-1.jpg");
//        reader.initialize(new InputStreamInputSplit(res.getInputStream(), res.getURI()));
//        assertTrue(reader.hasNext());
//        Collection<Writable> record = reader.next();
//        assertEquals(784 * 3 + 1, record.size());
//    }
//
    @Test
    public void testInitializeWithCollectionsSplit() throws Exception{
        BaseInputSplit split = new BaseInputSplit() {
            {
                String[] paths = {
                        "label0/group1_img.tif",
                        "label1/group1_img.jpg",
                        "label2/group1_img.png",
                        "label3/group1_img.jpeg",
                        "label4/group1_img.bmp",
                        "label5/group1_img.JPEG",
                        "label0/group2_img.JPG",
                        "label1/group2_img.TIF",
                        "label2/group2_img.PNG",
                        "label3/group2_img.jpg",
                        "label4/group2_img.jpg",
                        "label5/group2_img.wtf" };

                locations = new URI[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    locations[i] = new URI("file:///" + paths[i]);
                }
            }
            @Override
            public void write(DataOutput out) throws IOException {
            }

            @Override
            public void readFields(DataInput in) throws IOException {
            }
        };
        Random random = new Random(42);
        String[] extensions = {"tif", "jpg", "png", "jpeg", "bmp", "JPEG", "JPG", "TIF", "PNG"};
        ParentPathLabelGenerator parentPathLabelGenerator = new ParentPathLabelGenerator();
        BalancedPathFilter balancedPathFilter = new BalancedPathFilter(random, extensions, parentPathLabelGenerator, 12, 6, 2);

        RecordReader recordReader = new ImageRecordReader(10, 10, 3, new ParentPathLabelGenerator());
        InputSplit[] samples = split.sample(balancedPathFilter, 10, 2);
        assertEquals(9, samples[0].length());
        assertEquals(2, samples[1].length());

        recordReader.initialize(samples[0]);
        assertEquals(6, recordReader.getLabels().size());



    }


}
