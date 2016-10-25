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
package org.apereo.services.persondir.mock;

import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.BasePersonAttributeDao;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mock, test implementation of ThrowingPersonAttributeDao which always
 * throws a RuntimeException.
 */
public class ThrowingPersonAttributeDao extends BasePersonAttributeDao {

    public Set<String> getAvailableQueryAttributes() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
    
    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }


    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }


    @Override
    public IPersonAttributes getPerson(final String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }


    @Override
    public Set<String> getPossibleUserAttributeNames() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    @Override
    public void setOrder(final int order) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    @Override
    protected Map<String, Object> flattenResults(final Map<String, List<Object>> multivaluedUserAttributes) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
}
