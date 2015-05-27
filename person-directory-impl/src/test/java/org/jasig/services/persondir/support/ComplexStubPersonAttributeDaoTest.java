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
        final Map<String, List<Object>> awp9Map = new HashMap<String, List<Object>>();
        awp9Map.put("shirtColor", Util.list("blue"));
        awp9Map.put("phone", Util.list("777-7777"));
        awp9Map.put("wearsTie", Util.list("false"));
        
        // build the user attributes for aam26
        final Map<String, List<Object>> aam26Map = new HashMap<String, List<Object>>();
        aam26Map.put("shirtColor", Util.list("white"));
        aam26Map.put("phone",Util.list( "666-6666"));
        aam26Map.put("musicalInstrumentOfChoice", Util.list("trumpet"));
        
        // build the backing map, which maps from username to attribute map
        final Map<String, Map<String, List<Object>>> bMap = new HashMap<String, Map<String, List<Object>>>();
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
        final HashSet<String> expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        expectedAttributeNames.add("musicalInstrumentOfChoice");
        expectedAttributeNames.add("wearsTie");
        final Set<String> possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames();
        
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
        final Map<String, List<Object>> awp9Key = new HashMap<String, List<Object>>();
        awp9Key.put("username", Util.list("awp9"));
        assertEquals(this.backingMap.get("awp9"), this.testInstance.getMultivaluedUserAttributes(awp9Key));
        
        final Map<String, List<Object>> unknownUserKey = new HashMap<String, List<Object>>();
        unknownUserKey.put("uid", Util.list("unknownUser"));
        
        assertNull(this.testInstance.getMultivaluedUserAttributes(unknownUserKey));
    }

    /**
     * Test getting user attributes using a String key.
     */
    public void testGetUserAttributesString() {
        assertEquals(this.backingMap.get("aam26"), this.testInstance.getMultivaluedUserAttributes("aam26"));
        
        assertNull(this.testInstance.getMultivaluedUserAttributes("unknownUser"));
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.testInstance;
    }
}

