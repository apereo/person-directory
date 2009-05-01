package org.jasig.services.persondir.support;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class AdditionalDescriptorsTest extends TestCase {
    
    private static final List<Object> VALUES_LIST = Arrays.asList(new Object[]{"ONE", "TWO", "THREE"});

    /*
     * Public API.
     */
    
    public void testGetAttributeValue() {
        
        AdditionalDescriptors ad = new AdditionalDescriptors();
        ad.setAttributeValues("foo", VALUES_LIST);
        
        TestCase.assertEquals(ad.getAttributeValue("foo"), VALUES_LIST.get(0));

        TestCase.assertFalse(VALUES_LIST.get(1).equals(ad.getAttributeValue("foo")));
        
        TestCase.assertNull(ad.getAttributeValue("bar"));
        
    }
    
    public void testGetAttributeValues() {
        
        AdditionalDescriptors ad = new AdditionalDescriptors();
        ad.setAttributeValues("foo", VALUES_LIST);

        TestCase.assertEquals(ad.getAttributeValues("foo"), VALUES_LIST);

        TestCase.assertNull(ad.getAttributeValue("bar"));

    }
    
    public void testSetAttributeValues() {
        
        AdditionalDescriptors ad = new AdditionalDescriptors();
        
        boolean caught = false;
        try {
            ad.setAttributeValues(null, VALUES_LIST);
        } catch (IllegalArgumentException iae) {
            caught = true;
        }
        
        if (!caught) fail();

    }
    
}
