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

package org.canova.nd4j.nlp.reader;

import org.canova.api.conf.Configuration;
import org.canova.api.io.data.IntWritable;
import org.canova.api.records.reader.impl.FileRecordReader;
import org.canova.api.split.InputSplit;
import org.canova.api.vector.Vectorizer;
import org.canova.api.writable.Writable;
import org.canova.common.RecordConverter;
import org.canova.nd4j.nlp.vectorizer.TfidfVectorizer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * TFIDF record reader (wraps a tfidf vectorizer for delivering labels and conforming to the record reader interface)
 *
 * @author Adam Gibson
 */
public class TfidfRecordReader extends FileRecordReader  {
    private TfidfVectorizer tfidfVectorizer;
    private Collection<Collection<Writable>> records = new ArrayList<>();
    private Iterator<Collection<Writable>> recordIter;
    private Configuration conf;
    private int numFeatures;


    @Override
    public void initialize(InputSplit split) throws IOException, InterruptedException {
        initialize(new Configuration(),split);
    }

    @Override
    public void initialize(Configuration conf, InputSplit split) throws IOException, InterruptedException {
        super.initialize(conf,split);
        //train  a new one since it hasn't been specified
        if(tfidfVectorizer == null) {
            tfidfVectorizer = new TfidfVectorizer();
            tfidfVectorizer.initialize(conf);
            INDArray ret = tfidfVectorizer.fitTransform(this, new Vectorizer.RecordCallBack() {
                @Override
                public void onRecord(Collection<Writable> record) {
                    records.add(record);
                }
            });
            //clear out old strings
            records.clear();
            for(int i = 0; i< ret.rows(); i++) {
                records.add(RecordConverter.toRecord(ret.getRow(i)));
            }

            //cache the number of features used for each document
            numFeatures = ret.columns();
            recordIter = records.iterator();
        }
        else {
            records = new ArrayList<>();

            //the record reader has 2 phases, we are skipping the
            //document frequency phase and just using the super() to get the file contents
            //and pass it to the already existing vectorizer.
            while(hasNext()) {
                Collection<Writable> fileContents = next();
                records.add(RecordConverter.toRecord(tfidfVectorizer.transform(fileContents)));
            }
            recordIter = records.iterator();
        }


    }

    @Override
    public Collection<Writable> next() {
        if(recordIter == null)
            return super.next();
        Collection<Writable> record = recordIter.next();
        if(appendLabel)
            record.add(new IntWritable(getCurrentLabel()));
        return record;
    }

    @Override
    public boolean hasNext() {
        //we aren't done vectorizing yet
        if(recordIter == null)
            return super.hasNext();
        return recordIter.hasNext();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public TfidfVectorizer getTfidfVectorizer() {
        return tfidfVectorizer;
    }

    public void setTfidfVectorizer(TfidfVectorizer tfidfVectorizer) {
        this.tfidfVectorizer = tfidfVectorizer;
    }

    public int getNumFeatures() {
        return numFeatures;
    }
}

