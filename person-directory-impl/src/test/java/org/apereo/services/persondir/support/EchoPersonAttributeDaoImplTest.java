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

import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Dalquist

 */
public class EchoPersonAttributeDaoImplTest extends AbstractPersonAttributeDaoTest {

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return new EchoPersonAttributeDaoImpl();
    }

    public void testMapEcho() {
        final EchoPersonAttributeDaoImpl dao = new EchoPersonAttributeDaoImpl();

        final Map<String, List<Object>> testMap = new HashMap<>();
        testMap.put("key1", Util.list("val1"));
        testMap.put("key2", Util.list("val2"));

        final Map<String, List<Object>> goalMap = new HashMap<>(testMap);

        final Map<String, List<Object>> resultMap = dao.getMultivaluedUserAttributes(testMap, IPersonAttributeDaoFilter.alwaysChoose());

        assertEquals(goalMap, resultMap);
    }

}
