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

import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testcase for StubPersonAttributeDao.
 */
public class StubPersonAttributeDaoTest
    extends AbstractPersonAttributeDaoTest {

    private StubPersonAttributeDao testInstance;

    private Map<String, List<Object>> backingMap;


    @BeforeEach
    protected void setUp() throws Exception {
        final Map<String, List<Object>> map = new HashMap<>();
        map.put("shirtColor", Util.list("blue"));
        map.put("phone", Util.list("777-7777"));

        this.backingMap = map;

        this.testInstance = new StubPersonAttributeDao();
        this.testInstance.setBackingMap(map);

    }

    /**
     * Test that when the backing map is set properly reports possible
     * attribute names and when the map is not set returns null for
     * possible attribute names.
     */
    @Test
    public void testGetPossibleUserAttributeNames() {
        final Set<String> expectedAttributeNames = new HashSet<>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        var possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedAttributeNames, possibleAttributeNames);

        var nullBacking = new StubPersonAttributeDao();
        assertEquals(Collections.EMPTY_SET, nullBacking.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
    }

    /**
     * Return stub attributes regardless of input (e.g. empty map)
     */
    @Test
    public void testGetUserAttributesMap() {
        var resultsSet = this.testInstance.getPeopleWithMultivaluedAttributes(new HashMap<>(),
            IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(this.backingMap, resultsSet.iterator().next().getAttributes());

    }

    /**
     * Return stub attributes regardless of uid, e.g. random name wombat.
     */
    @Test
    public void testGetUserAttributesString() {
        assertEquals(this.backingMap, this.testInstance.getPerson("wombat", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes());
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.testInstance;
    }


}

