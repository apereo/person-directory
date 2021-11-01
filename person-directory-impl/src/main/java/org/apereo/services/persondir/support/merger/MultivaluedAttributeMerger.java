/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support.merger;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * Merger that retains values from both maps. If a value exists for
 * a key in both maps the following is done:
 * <ul>
 *  <li>If both maps have a {@link List} they are merged into a single {@link List}</li>
 *  <li>If one map has a {@link List} and the other a single value the value is added to the {@link List}</li>
 *  <li>If both maps have a single value a {@link List} is created from the two.</li>
 * </ul>
 *
 * @author Eric Dalquist

 */
public class MultivaluedAttributeMerger extends BaseAdditiveAttributeMerger {
    private boolean distinctValues;

    public void setDistinctValues(final boolean distinctValues) {
        this.distinctValues = distinctValues;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.BaseAdditiveAttributeMerger#mergePersonAttributes(java.util.Map, java.util.Map)
     */
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify,
                                                              final Map<String, List<Object>> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");

        for (var sourceEntry : toConsider.entrySet()) {
            var sourceKey = sourceEntry.getKey();

            var values = toModify.computeIfAbsent(sourceKey, k -> new LinkedList<>());

            var sourceValue = sourceEntry.getValue();
            if (this.distinctValues) {
                final Set<Object> temp = new TreeSet<>((o1, o2) -> {
                    if (o1 instanceof String && o2 instanceof String && o1.toString().equalsIgnoreCase(o2.toString())) {
                        return 0;
                    }
                    if (o1 instanceof Comparable && o2 instanceof Comparable
                        && o1.getClass().isAssignableFrom(o2.getClass())) {
                        return ((Comparable<Object>) o1).compareTo(o2);
                    }
                    return -1;
                });
                temp.addAll(values);
                temp.addAll(sourceValue);
                toModify.put(sourceKey, new ArrayList<>(temp));
            } else {
                values.addAll(sourceValue);
            }
        }

        return toModify;
    }
}
