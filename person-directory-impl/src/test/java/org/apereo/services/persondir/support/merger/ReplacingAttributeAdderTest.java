/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support.merger;

import org.apereo.services.persondir.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testcase for ReplacingAttributeAdder.
 * @author andrew.petro@yale.edu

 */
public class ReplacingAttributeAdderTest extends AbstractAttributeMergerTest {

    private ReplacingAttributeAdder adder = new ReplacingAttributeAdder();

    /**
     * Test that this implementation replaces colliding attributes with the new 
     * attribute values.
     */
    public void testReplacement() {
        final Map<String, List<Object>> mapOne = new HashMap<>();
        mapOne.put("aaa", Util.list("111"));
        mapOne.put("bbb", Util.list("222"));

        final Map<String, List<Object>> mapTwo = new HashMap<>();
        mapTwo.put("bbb", Util.list("bbb"));
        mapTwo.put("ccc", Util.list("333"));

        final Map<String, List<Object>> expected = new HashMap<>();
        expected.putAll(mapOne);
        expected.putAll(mapTwo);

        final Map<String, List<Object>> result = this.adder.mergeAttributes(mapOne, mapTwo);
        assertEquals(expected, result);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.AbstractAttributeMergerTest#getAttributeMerger()
     */
    @Override
    protected IAttributeMerger getAttributeMerger() {
        return new ReplacingAttributeAdder();
    }

}
