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

package org.canova.image.loader;


import org.canova.api.io.filters.BalancedPathFilter;
import org.canova.api.io.labels.ParentPathLabelGenerator;
import org.canova.api.io.labels.PathLabelGenerator;
import org.canova.api.io.labels.PatternPathLabelGenerator;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.split.FileSplit;
import org.canova.api.split.InputSplit;
import org.canova.api.split.LimitFileSplit;
import org.canova.image.recordreader.ImageRecordReader;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Loads LFW faces data transform.
 * Customize the size of images by passing in preferred dimensions.
 *
 * DataSet
 *      5749 different individuals
 *      1680 people have two or more images in the database
 *      4069 people have just a single image in the database
 *      available as 250 by 250 pixel JPEG images
 *      most images are in color, although a few are grayscale
 *
 */
public class LFWLoader extends BaseImageLoader implements Serializable {

    public final static int NUM_IMAGES = 13233;
    public final static int NUM_LABELS = 5749;
    public final static int SUB_NUM_IMAGES = 1054;
    public final static int SUB_NUM_LABELS = 432;
    public final static int CHANNELS = 3;
    public final static int HEIGHT = 250;
    public final static int WIDTH = 250;
    public final static String DATA_URL = "http://vis-www.cs.umass.edu/lfw/lfw.tgz";
    public final static String LABEL_URL =  "http://vis-www.cs.umass.edu/lfw/lfw-names.txt";
    public final static String SUBSET_URL = "http://vis-www.cs.umass.edu/lfw/lfw-a.tgz";

    public String dataFile = "lfw";
    public String labelFile = "lfw-names.txt";
    public String subsetFile = "lfw-a";

    public String localDir = "lfw";
    public String localSubDir = "lfw-a/lfw";
    protected File fullDir = new File(BASE_DIR, localDir);
    protected String regexPattern = ".[0-9]+";
    protected PathLabelGenerator labelGenerator = new PatternPathLabelGenerator(regexPattern);
    protected boolean useSubset = false;
    protected int numExamples = NUM_IMAGES;
    protected int numLabels = NUM_LABELS;
    protected int batchSize = 0;
    protected double splitTrainTest = 1;
    protected boolean train = true;

    public static Map<String, String> lfwData = new HashMap<>();
    public static Map<String, String> lfwLabel = new HashMap<>();
    public static Map<String, String> lfwSubsetData = new HashMap<>();


    public LFWLoader(String localDir, boolean useSubset){
        this.localDir = localDir;
        this.fullDir = new File(localDir);
        this.useSubset = useSubset;
        if (useSubset) {
            this.numExamples = SUB_NUM_IMAGES;
            this.numLabels = SUB_NUM_LABELS;
        }
        generateLfwMaps();
        if (!imageFilesExist()) load();
    }

    public LFWLoader(boolean useSubset){
        this.useSubset = useSubset;
        if (useSubset) {
            this.fullDir = new File(BASE_DIR, localSubDir);
            this.numExamples = SUB_NUM_IMAGES;
            this.numLabels = SUB_NUM_LABELS;
        }
        generateLfwMaps();
        if (!imageFilesExist()) load();
    }

    public LFWLoader(String path){
        this(path, false);
    }

    public LFWLoader(){this(false);}

    @Override
    public INDArray asRowVector(File f) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray asRowVector(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray asMatrix(File f) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray asMatrix(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void generateLfwMaps() {
        if(useSubset) {
            // Subset of just faces with a name starting with A
            lfwSubsetData.put("filesFilename", new File(SUBSET_URL).getName());
            lfwSubsetData.put("filesURL", SUBSET_URL);
            lfwSubsetData.put("filesFilenameUnzipped", subsetFile);

        } else {
            lfwData.put("filesFilename", new File(DATA_URL).getName());
            lfwData.put("filesURL", DATA_URL);
            lfwData.put("filesFilenameUnzipped", dataFile);

            lfwLabel.put("filesFilename", labelFile);
            lfwLabel.put("filesURL",LABEL_URL);
            lfwLabel.put("filesFilenameUnzipped", labelFile);
        }

    }

    public void load()  {
        if (!fullDir.exists() || fullDir.listFiles() == null || fullDir.listFiles().length == 0) {
            fullDir.mkdir();

            if (useSubset) {
                log.info("Downloading {} subset...", localDir);
                downloadAndUntar(lfwSubsetData, fullDir);
            }
            else {
                log.info("Downloading {}...", localDir);
                downloadAndUntar(lfwData, fullDir);
                downloadAndUntar(lfwLabel, fullDir);
            }
        }
    }

    public boolean imageFilesExist(){
        if(useSubset){
            File f = new File(BASE_DIR, lfwSubsetData.get("filesFilenameUnzipped"));
            if (!f.exists()) return false;
        } else {
            File f = new File(BASE_DIR, lfwData.get("filesFilenameUnzipped"));
            if (!f.exists()) return false;
            f = new File(BASE_DIR, lfwLabel.get("filesFilenameUnzipped"));
            if (!f.exists()) return false;
        }
        return true;
    }


    public RecordReader getRecordReader() {
        return getRecordReader(HEIGHT, WIDTH, CHANNELS);
    }

    public RecordReader getRecordReader(int numExamples) {
        this.numExamples = numExamples;
        return getRecordReader(HEIGHT, WIDTH, CHANNELS);
    }

    public RecordReader getRecordReader(int numExamples, int batchSize, boolean train) {
        this.numExamples = numExamples;
        this.batchSize = batchSize;
        this.train = train;
        return getRecordReader(HEIGHT, WIDTH, CHANNELS);
    }

    public RecordReader getRecordReader(int numExamples, int batchSize, int height, int width, int channels, boolean train, Random rng) {
        this.numExamples = numExamples;
        this.rng = rng;
        this.batchSize = batchSize;
        this.train = train;
        return getRecordReader(height, width, channels);
    }


    public RecordReader getRecordReader(int numExamples, int batchSize, int height, int width, int channels, PathLabelGenerator labelGenerator, boolean train, Random rng) {
        this.numExamples = numExamples;
        this.rng = rng;
        this.batchSize = batchSize;
        this.labelGenerator = labelGenerator;
        this.train = train;
        return getRecordReader(height, width, channels);
    }

    public RecordReader getRecordReader(int numExamples, int batchSize, int height, int width, int channels,
                                        int numLabels, PathLabelGenerator labelGenerator,
                                         double splitTrainTest, boolean train, Random rng) {
        this.numExamples = numExamples;
        this.numLabels = numLabels;
        this.rng = rng;
        this.labelGenerator = labelGenerator;
        this.batchSize = batchSize;
        this.splitTrainTest = splitTrainTest;
        this.train = train;
        return getRecordReader(height, width, channels);
    }

    public RecordReader getRecordReader(int height, int width, int channels) {
        // TODO add image scaling flexibility for iterator
        if (!imageFilesExist()) load();
        RecordReader recordReader = new ImageRecordReader(height, width, channels, labelGenerator);
        FileSplit fileSplit = new FileSplit(fullDir, BaseImageLoader.ALLOWED_FORMATS, rng);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, BaseImageLoader.ALLOWED_FORMATS, labelGenerator, numExamples, numLabels, 0, batchSize);
        InputSplit[] inputSplit = fileSplit.sample(pathFilter, numExamples*splitTrainTest, numExamples*(1-splitTrainTest));

        try {
            InputSplit data = train? inputSplit[0]: inputSplit[1];
            recordReader.initialize(data);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return recordReader;
    }

}
