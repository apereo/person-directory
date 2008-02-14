/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
     * @see org.jasig.portal.services.persondir.support.merger.AbstractAttributeMergerTest#getAttributeMerger()
     */
    @Override
    protected IAttributeMerger getAttributeMerger() {
        return new NoncollidingAttributeAdder();
    }
    
}
