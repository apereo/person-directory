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
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link IPersonAttributeDao} implementation which iterates over children 
 * IPersonAttributeDaos queries each with the same data and merges their
 * reported attributes in a configurable way. The default merger is
 * {@link MultivaluedAttributeMerger}.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist

 * @since uPortal 2.5
 */
public class MergingPersonAttributeDaoImpl extends AbstractAggregatingDefaultQueryPersonAttributeDao {
    public MergingPersonAttributeDaoImpl() {
        this.attrMerger = new MultivaluedAttributeMerger();
    }

    /**
     * Calls the current IPersonAttributeDao from using the seed.
     *
     * @see AbstractAggregatingDefaultQueryPersonAttributeDao#getAttributesFromDao(java.util.Map, boolean, IPersonAttributeDao, java.util.Set, IPersonAttributeDaoFilter)
     */
    @Override
    protected Set<IPersonAttributes> getAttributesFromDao(final Map<String, List<Object>> seed, final boolean isFirstQuery,
                                                          final IPersonAttributeDao currentlyConsidering,
                                                          final Set<IPersonAttributes> resultPeople,
                                                          final IPersonAttributeDaoFilter filter) {
        return currentlyConsidering.getPeopleWithMultivaluedAttributes(seed, filter, resultPeople);
    }
}
