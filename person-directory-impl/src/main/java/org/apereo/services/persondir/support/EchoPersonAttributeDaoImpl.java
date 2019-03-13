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
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Simply returns the seed it is passed.
 *
 * @author Eric Dalquist

 * @since uPortal 2.5
 */
public class EchoPersonAttributeDaoImpl extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * Returns a duplicate of the seed it is passed.
     * @return a Map equal to but not the same reference as the seed.
     * @see IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map, org.apereo.services.persondir.IPersonAttributeDaoFilter)
     */
    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter) {
        if (query == null) {
            throw new IllegalArgumentException("seed may not be null");
        }

        return Collections.singleton((IPersonAttributes)
                new AttributeNamedPersonImpl(getUsernameAttributeProvider().getUsernameAttribute(), query));
    }

    /**
     * Possible attributes are unknown; will always return <code>null</code>.
     * @return null
     * @see IPersonAttributeDao#getPossibleUserAttributeNames(IPersonAttributeDaoFilter)
     */
    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return null;
    }
}
