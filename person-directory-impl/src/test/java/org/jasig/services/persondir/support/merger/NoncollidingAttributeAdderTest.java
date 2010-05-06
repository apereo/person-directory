/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.support.merger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.util.Util;

/**
 * Testcase for the NoncollidingAttributeAdder.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class NoncollidingAttributeAdderTest extends AbstractAttributeMergerTest {

    private NoncollidingAttributeAdder adder = new NoncollidingAttributeAdder();

    /**
     * Test identity of adding an empty map.
     */
    public void testAddEmpty() {
        Map<String, List<Object>> someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", Util.list("attValue"));
        someAttributes.put("attName2", Util.list("attValue2"));
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.putAll(someAttributes);
        
        Map<String, List<Object>> result = this.adder.mergeAttributes(someAttributes, new HashMap<String, List<Object>>());
        
        assertEquals(expected, result);
    }

    /**
     * Test a simple case of adding one map of attributes to another, with
     * no collisions.
     */
    public void testAddNoncolliding() {
        Map<String, List<Object>> someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", Util.list("attValue"));
        someAttributes.put("attName2", Util.list("attValue2"));
        
        Map<String, List<Object>> otherAttributes = new HashMap<String, List<Object>>();
        otherAttributes.put("attName3", Util.list("attValue3"));
        otherAttributes.put("attName4", Util.list("attValue4"));
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.putAll(someAttributes);
        expected.putAll(otherAttributes);
        
        Map<String, List<Object>> result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }
    
    
    
    /**
     * Test that colliding attributes are not added.
     */
    public void testColliding() {
        Map<String, List<Object>> someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", Util.list("attValue"));
        someAttributes.put("attName2", Util.list("attValue2"));
        
        Map<String, List<Object>> otherAttributes = new HashMap<String, List<Object>>();
        otherAttributes.put("attName", Util.list("attValue3"));
        otherAttributes.put("attName4", Util.list("attValue4"));
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.putAll(someAttributes);
        expected.put("attName4", Util.list("attValue4"));
        
        Map<String, List<Object>> result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.AbstractAttributeMergerTest#getAttributeMerger()
     */
    @Override
    protected IAttributeMerger getAttributeMerger() {
        return new NoncollidingAttributeAdder();
    }
    
}
