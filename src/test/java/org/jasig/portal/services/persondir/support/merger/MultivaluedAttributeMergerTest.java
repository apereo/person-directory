/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testcase for the MultivaluedAttributeMerger.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MultivaluedAttributeMergerTest extends AbstractAttributeMergerTest {

    private MultivaluedAttributeMerger adder = new MultivaluedAttributeMerger();

    /**
     * Test identity of adding an empty map.
     */
    public void testAddEmpty() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName", "attValue");
        someAttributes.put("attName2", "attValue2");
        
        Map expected = new HashMap();
        expected.putAll(someAttributes);
        
        Map result = this.adder.mergeAttributes(someAttributes, new HashMap());
        
        assertEquals(expected, result);
    }

    /**
     * Test a simple case of adding one map of attributes to another, with
     * no collisions.
     */
    public void testAddNoncolliding() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName", "attValue");
        someAttributes.put("attName2", "attValue2");
        
        Map otherAttributes = new HashMap();
        otherAttributes.put("attName3", "attValue3");
        otherAttributes.put("attName4", "attValue4");
        
        Map expected = new HashMap();
        expected.putAll(someAttributes);
        expected.putAll(otherAttributes);
        
        Map result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }
    
    /**
     * Test that colliding attributes are not added.
     */
    public void testColliding() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName1", null);
        someAttributes.put("attName2", "attValue2");
        
        someAttributes.put("attName5", null);
        someAttributes.put("attName6", null);
        someAttributes.put("attName7", "attValue7");
        someAttributes.put("attName8", "attValue8.1");
        
        someAttributes.put("attName9", null);
        someAttributes.put("attName10", "attValue10");
        someAttributes.put("attName11", this.asList(new String[] {"attValue11.1", "attValue11.2"}));
        someAttributes.put("attName12", this.asList(new String[] {"attValue12.1", "attValue12.2"}));
        someAttributes.put("attName13", this.asList(new String[] {"attValue13.1.1", "attValue13.1.2"}));
        
        
        Map otherAttributes = new HashMap();
        otherAttributes.put("attName3", null);
        otherAttributes.put("attName4", "attValue4");
        
        otherAttributes.put("attName5", null);
        otherAttributes.put("attName6", "attValue6");
        otherAttributes.put("attName7", null);
        otherAttributes.put("attName8", "attValue8.2");
        
        otherAttributes.put("attName9", this.asList(new String[] {"attValue9.1", "attValue9.2"}));
        otherAttributes.put("attName10", this.asList(new String[] {"attValue10.1", "attValue10.2"}));
        otherAttributes.put("attName11", null);
        otherAttributes.put("attName12", "attValue12");
        otherAttributes.put("attName13", this.asList(new String[] {"attValue13.2.1", "attValue13.2.2"}));

        
        Map expected = new HashMap();
        expected.put("attName1", null);
        expected.put("attName2", "attValue2");
        expected.put("attName3", null);
        expected.put("attName4", "attValue4");
        expected.put("attName5", this.asList(new String[] {null, null}));
        expected.put("attName6", this.asList(new String[] {null, "attValue6"}));
        expected.put("attName7", this.asList(new String[] {"attValue7", null}));
        expected.put("attName8", this.asList(new String[] {"attValue8.1", "attValue8.2"}));

        expected.put("attName9", this.asList(new String[] {null, "attValue9.1", "attValue9.2"}));
        expected.put("attName10", this.asList(new String[] {"attValue10", "attValue10.1", "attValue10.2"}));
        
        expected.put("attName11", this.asList(new String[] {"attValue11.1", "attValue11.2", null}));
        expected.put("attName12", this.asList(new String[] {"attValue12.1", "attValue12.2", "attValue12"}));
        expected.put("attName13", this.asList(new String[] {"attValue13.1.1", "attValue13.1.2", "attValue13.2.1", "attValue13.2.2"}));
        
        Map result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.support.merger.AbstractAttributeMergerTest#getAttributeMerger()
     */
    protected IAttributeMerger getAttributeMerger() {
        return new MultivaluedAttributeMerger();
    }
    
    private List asList(Object[] array) {
        List l = new ArrayList(array.length);
        for (int index = 0; index < array.length; index++) {
            l.add(array[index]);
        }
        return l;
    }
}
