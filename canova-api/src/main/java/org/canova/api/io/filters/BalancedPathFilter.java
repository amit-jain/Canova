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
package org.canova.api.io.filters;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import org.canova.api.io.labels.PathLabelGenerator;
import org.canova.api.writable.Writable;

/**
 *
 * @author saudet
 */
public class BalancedPathFilter extends RandomPathFilter {

    protected PathLabelGenerator labelGenerator;
    protected int maxLabels = 0, maxPathsPerLabel = 0;

    public BalancedPathFilter(Random random, String[] extensions, PathLabelGenerator labelGenerator) {
        this(random, extensions, labelGenerator, 0, 0, 0);
    }
    public BalancedPathFilter(Random random, String[] extensions, PathLabelGenerator labelGenerator,
            int maxPaths, int maxLabels, int maxPathsPerLabel) {
        super(random, extensions, maxPaths);
        this.labelGenerator = labelGenerator;
        this.maxLabels = maxLabels;
        this.maxPathsPerLabel = maxPathsPerLabel;
    }

    @Override
    public URI[] filter(URI[] paths) {
        paths = super.filter(paths);

        HashMap<Writable, Integer> labelsCount = new HashMap<Writable, Integer>();
        for (int i = 0; i < paths.length; i++) {
            URI path = paths[i];
            Writable label = labelGenerator.getLabelForPath(path);
            Integer count = labelsCount.get(label);
            if (count == null) {
                if (maxLabels > 0 && labelsCount.size() >= maxLabels) {
                    continue;
                }
                count = 0;
            }
            labelsCount.put(label, count + 1);
        }

        int minCount = Integer.MAX_VALUE;
        for (Integer count : labelsCount.values()) {
            if (minCount > count) {
                minCount = count;
            }
        }
        if (maxPathsPerLabel > 0 && minCount > maxPathsPerLabel) {
            minCount = maxPathsPerLabel;
        }

        labelsCount.clear();
        ArrayList<URI> newpaths = new ArrayList<URI>();
        for (int i = 0; i < paths.length; i++) {
            URI path = paths[i];
            Writable label = labelGenerator.getLabelForPath(path);
            Integer count = labelsCount.get(label);
            if (count == null) {
                if (maxLabels > 0 && labelsCount.size() >= maxLabels) {
                    continue;
                }
                count = 0;
            }
            labelsCount.put(label, count + 1);
            if (count < minCount) {
                newpaths.add(path);
            }
        }

        Collections.shuffle(newpaths, random);
        return newpaths.toArray(new URI[newpaths.size()]);
    }
}
