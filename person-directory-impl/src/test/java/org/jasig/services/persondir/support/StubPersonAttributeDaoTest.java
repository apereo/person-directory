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
package org.jasig.services.persondir.support;

import org.jasig.services.persondir.AbstractPersonAttributeDaoTest;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.Util;

import java.util.*;


/**
 * Testcase for StubPersonAttributeDao.
 * @version $Revision$ $Date$
 */
public class StubPersonAttributeDaoTest 
    extends AbstractPersonAttributeDaoTest {

    private StubPersonAttributeDao testInstance;
    private Map<String, List<Object>> backingMap;
    
    
    @Override
    protected void setUp() throws Exception {
        final Map<String, List<Object>> map = new HashMap<>();
        map.put("shirtColor", Util.list("blue"));
        map.put("phone", Util.list("777-7777"));
        
        this.backingMap = map;
        
        this.testInstance = new StubPersonAttributeDao();
        this.testInstance.setBackingMap(map);
        
        super.setUp();
    }
    
    /**
     * Test that when the backing map is set properly reports possible 
     * attribute names and when the map is not set returns null for
     * possible attribute names.
     */
    public void testGetPossibleUserAttributeNames() {
        final Set<String> expectedAttributeNames = new HashSet<>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        final Set<String> possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames();
        assertEquals(expectedAttributeNames, possibleAttributeNames);
        
        final StubPersonAttributeDao nullBacking = new StubPersonAttributeDao();
        assertEquals(Collections.EMPTY_SET, nullBacking.getPossibleUserAttributeNames());
    }

    public void testGetUserAttributesMap() {
        assertEquals(this.backingMap, this.testInstance.getMultivaluedUserAttributes(new HashMap<String, List<Object>>()));

    }

    public void testGetUserAttributesString() {
        assertEquals(this.backingMap, this.testInstance.getMultivaluedUserAttributes("wombat"));
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.testInstance;
    }


}

