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
package org.apereo.services.persondir.support;

import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Testcase for ComplexStubPersonAttributeDao.

 */
public class ComplexStubPersonAttributeDaoTest
    extends AbstractDefaultQueryPersonAttributeDaoTest {

    private ComplexStubPersonAttributeDao testInstance;

    private Map<String, Map<String, List<Object>>> backingMap;


    @BeforeEach
    protected void setUp() throws Exception {
        // built the user attributes for awp9
        final Map<String, List<Object>> awp9Map = new HashMap<>();
        awp9Map.put("shirtColor", Util.list("blue"));
        awp9Map.put("phone", Util.list("777-7777"));
        awp9Map.put("wearsTie", Util.list("false"));

        // build the user attributes for aam26
        final Map<String, List<Object>> aam26Map = new HashMap<>();
        aam26Map.put("shirtColor", Util.list("white"));
        aam26Map.put("phone", Util.list("666-6666"));
        aam26Map.put("musicalInstrumentOfChoice", Util.list("trumpet"));

        // build the backing map, which maps from username to attribute map
        final Map<String, Map<String, List<Object>>> bMap = new HashMap<>();
        bMap.put("awp9", awp9Map);
        bMap.put("aam26", aam26Map);

        this.backingMap = bMap;

        this.testInstance = new ComplexStubPersonAttributeDao(this.backingMap);
    }

    /**
     * Test that when the backing map is set properly reports possible 
     * attribute names and when the map is not set returns null for
     * possible attribute names.
     */
    @Test
    public void testGetPossibleUserAttributeNames() {
        var expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        expectedAttributeNames.add("musicalInstrumentOfChoice");
        expectedAttributeNames.add("wearsTie");
        var possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());

        // test that it properly computed the set of possible attribute names

        assertEquals(expectedAttributeNames, possibleAttributeNames);

        // here we test that it returns the same Set each time
        // this is an implementation detail - the impl could implement the interface
        // by making a new Set each time, but since we know it's trying to cache
        // the computed set, we can test whether it's doing what it indends.

        assertSame(possibleAttributeNames, this.testInstance.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));

    }

    /**
     * Test getting user attributes using a Map key.
     */
    @Test
    public void testGetUserAttributesMap() {
        final Map<String, List<Object>> awp9Key = new HashMap<>();
        awp9Key.put("username", Util.list("awp9"));
        var resultSet = this.testInstance.getPeopleWithMultivaluedAttributes(awp9Key, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(this.backingMap.get("awp9"), resultSet.iterator().next().getAttributes());

        final Map<String, List<Object>> unknownUserKey = new HashMap<>();
        unknownUserKey.put("uid", Util.list("unknownUser"));

        assertNull(this.testInstance.getPeopleWithMultivaluedAttributes(unknownUserKey, IPersonAttributeDaoFilter.alwaysChoose()));
    }

    /**
     * Test getting user attributes using a String key.
     */
    @Test
    public void testGetUserAttributesString() {
        var result = this.testInstance.getPerson("aam26", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(result);
        assertEquals(this.backingMap.get("aam26"), result.getAttributes());

        assertNull(this.testInstance.getPerson("unknownUser", IPersonAttributeDaoFilter.alwaysChoose()));
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.testInstance;
    }
}

