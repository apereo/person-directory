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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Eric Dalquist

 */
public class MessageFormatPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    private final MessageFormatPersonAttributeDao messageFormatPersonAttributeDao = new MessageFormatPersonAttributeDao();

    public MessageFormatPersonAttributeDaoTest() {

        final var formatAttribute = new MessageFormatPersonAttributeDao.FormatAttribute();
        final Set<String> set2 =
                new HashSet<>();
        set2.add("displayName");
        formatAttribute.setAttributeNames(set2);
        formatAttribute.setFormat("{0} {1}");
        formatAttribute.setSourceAttributes(Util.genList("firstName", "lastName"));

        final Set<MessageFormatPersonAttributeDao.FormatAttribute> set =
                new HashSet<>();
        set.add(formatAttribute);
        messageFormatPersonAttributeDao.setFormatAttributes(set);
    }

    public void testMessageFormatAttributes() throws Exception {


        final Map<String, List<Object>> query = new LinkedHashMap<>();
        query.put("firstName", Util.list("Eric"));
        query.put("lastName", Util.list("Dalquist"));

        final var result = messageFormatPersonAttributeDao.getPeopleWithMultivaluedAttributes(query,
            IPersonAttributeDaoFilter.alwaysChoose());


        final Map<String, List<Object>> expectedResult = new LinkedHashMap<>();
        expectedResult.put("displayName", Util.list("Eric Dalquist"));

        assertEquals(expectedResult, result.iterator().next().getAttributes());
    }


    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.messageFormatPersonAttributeDao;
    }
}
