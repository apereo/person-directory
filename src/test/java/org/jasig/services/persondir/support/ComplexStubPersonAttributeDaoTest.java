/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.util.Util;


/**
 * Testcase for ComplexStubPersonAttributeDao.
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {

    private ComplexStubPersonAttributeDao testInstance;
    private Map<String, Map<String, List<Object>>> backingMap;
    
    
    @Override
    protected void setUp() throws Exception {
        // built the user attributes for awp9
        Map<String, List<Object>> awp9Map = new HashMap<String, List<Object>>();
        awp9Map.put("shirtColor", Util.list("blue"));
        awp9Map.put("phone", Util.list("777-7777"));
        awp9Map.put("wearsTie", Util.list("false"));
        
        // build the user attributes for aam26
        Map<String, List<Object>> aam26Map = new HashMap<String, List<Object>>();
        aam26Map.put("shirtColor", Util.list("white"));
        aam26Map.put("phone",Util.list( "666-6666"));
        aam26Map.put("musicalInstrumentOfChoice", Util.list("trumpet"));
        
        // build the backing map, which maps from username to attribute map
        Map<String, Map<String, List<Object>>> bMap = new HashMap<String, Map<String, List<Object>>>();
        bMap.put("awp9", awp9Map);
        bMap.put("aam26", aam26Map);
        
        this.backingMap = bMap;
        
        this.testInstance = new ComplexStubPersonAttributeDao(this.backingMap);
        
        super.setUp();
    }
    
    /**
     * Test that when the backing map is set properly reports possible 
     * attribute names and when the map is not set returns null for
     * possible attribute names.
     */
    public void testGetPossibleUserAttributeNames() {
        HashSet<String> expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        expectedAttributeNames.add("musicalInstrumentOfChoice");
        expectedAttributeNames.add("wearsTie");
        Set<String> possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames();
        
        // test that it properly computed the set of possible attribute names
        
        assertEquals(expectedAttributeNames, possibleAttributeNames);
        
        // here we test that it returns the same Set each time
        // this is an implementation detail - the impl could implement the interface
        // by making a new Set each time, but since we know it's trying to cache
        // the computed set, we can test whether it's doing what it indends.
        
        assertSame(possibleAttributeNames, this.testInstance.getPossibleUserAttributeNames());
        
    }

    /**
     * Test getting user attributes using a Map key.
     */
    public void testGetUserAttributesMap() {
        Map<String, List<Object>> awp9Key = new HashMap<String, List<Object>>();
        awp9Key.put("username", Util.list("awp9"));
        assertEquals(this.backingMap.get("awp9"), this.testInstance.getUserAttributes(awp9Key));
        
        Map<String, List<Object>> unknownUserKey = new HashMap<String, List<Object>>();
        unknownUserKey.put("uid", Util.list("unknownUser"));
        
        assertNull(this.testInstance.getUserAttributes(unknownUserKey));
    }

    /**
     * Test getting user attributes using a String key.
     */
    public void testGetUserAttributesString() {
        assertEquals(this.backingMap.get("aam26"), this.testInstance.getUserAttributes("aam26"));
        
        assertNull(this.testInstance.getUserAttributes("unknownUser"));
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.testInstance;
    }
}

