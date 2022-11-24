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

import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extension of the {@link StubPersonAttributeDao} that is able to identify itself
 * by populating the backing map with the received username. This allows for static attributes
 * to be merged with other DAOs via {@link MergingPersonAttributeDaoImpl}.
 * Without the unique identifier that is username, the merge would fail resulting in two distinct attribute sets
 * for the same principal in the ultimate attribute map.
 * @author Misagh Moayyed
 */
public class NamedStubPersonAttributeDao extends StubPersonAttributeDao {

    public NamedStubPersonAttributeDao() {
        super();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NamedStubPersonAttributeDao(final Map backingMap) {
        super(backingMap);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                           final IPersonAttributeDaoFilter filter,
                                                                           final Set<IPersonAttributes> resultPeople) {

        final List<?> list = query.get("username");
        final Map m = new HashMap(this.getBackingMap());

        m.put("username", list);

        this.setBackingMap(m);
        return super.getPeopleWithMultivaluedAttributes(query, filter, resultPeople);
    }
}
