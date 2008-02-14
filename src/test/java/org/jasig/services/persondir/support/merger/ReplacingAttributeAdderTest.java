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
 * Testcase for ReplacingAttributeAdder.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ReplacingAttributeAdderTest extends AbstractAttributeMergerTest {

    private ReplacingAttributeAdder adder = new ReplacingAttributeAdder();
    
    /**
     * Test that this implementation replaces colliding attributes with the new 
     * attribute values.
     */
    public void testReplacement() {
        Map<String, List<Object>> mapOne = new HashMap<String, List<Object>>();
        mapOne.put("aaa", Util.list("111"));
        mapOne.put("bbb", Util.list("222"));
        
        Map<String, List<Object>> mapTwo = new HashMap<String, List<Object>>();
        mapTwo.put("bbb", Util.list("bbb"));
        mapTwo.put("ccc", Util.list("333"));
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.putAll(mapOne);
        expected.putAll(mapTwo);
        
        Map<String, List<Object>> result = this.adder.mergeAttributes(mapOne, mapTwo);
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