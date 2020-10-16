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
package org.apereo.services.persondir.support;

import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Eric Dalquist

 */
public abstract class AbstractFlatteningPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    @Override
    protected final IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.getAbstractFlatteningPersonAttributeDao();
    }

    protected abstract AbstractFlatteningPersonAttributeDao getAbstractFlatteningPersonAttributeDao();


    public void testFlattenMap() throws Exception {
        final Map<String, List<Object>> backingMap = new HashMap<>();
        backingMap.put("name", Util.list("edalquist"));
        backingMap.put("emails", Util.list("edalquist@foo.com", "ebd@none.org"));
        backingMap.put("phone", Util.list((Object) null));
        backingMap.put("title", null);
        backingMap.put("address", Collections.emptyList());

        final Map<String, Object> expected = new HashMap<>();
        expected.put("name", Util.list("edalquist"));
        expected.put("emails", Util.list("edalquist@foo.com", "ebd@none.org"));
        expected.put("phone", Util.list((Object) null));
        expected.put("title", null);
        expected.put("address", Collections.emptyList());


//        final SimpleDefaultQueryPersonAttributeDao flatteningPersonAttributeDao = new SimpleDefaultQueryPersonAttributeDao(backingMap);
        final var flatteningPersonAttributeDao = new StubPersonAttributeDao(backingMap);

        final var userAttributesUid = flatteningPersonAttributeDao.getPerson("seed", IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expected, userAttributesUid.getAttributes());

        final var userAttributesSet = flatteningPersonAttributeDao.getPeople(Collections.singletonMap("key", new Object()),
            IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expected, userAttributesSet.iterator().next().getAttributes());
    }

//
//    
//    private class SimpleDefaultQueryPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
//        private Map<String, List<Object>> backingMap;
//        
//        public SimpleDefaultQueryPersonAttributeDao(Map<String, List<Object>> backingMap) {
//            this.backingMap = backingMap;
//        }
//        
//        public Map<String, List<Object>> getMultivaluedUserAttributes(String uid) {
//            return this.backingMap;
//        }
//        
//        public Map<String, List<Object>> getMultivaluedUserAttributes(Map<String, List<Object>> seed) {
//            return this.backingMap;
//        }
//
//        public Set<String> getPossibleUserAttributeNames() {
//            return null;
//        }
//    }
}
