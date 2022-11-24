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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A stub IPersonAttributeDao to be used for testing.
 * Backed by a single Map which this implementation will always return.
 *
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th>Property</th>
 *         <th>Description</th>
 *         <th>Required</th>
 *         <th>Default</th>
 *     </tr>
 *     <tr>
 *         <td  valign="top">backingMap</td>
 *         <td>
 *             This Map will always be returned for any query.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * @author andrew.petro@yale.edu

 * @since uPortal 2.5
 */
public class StubPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
    private IPersonAttributes backingPerson = null;

    public StubPersonAttributeDao() {
    }

    public StubPersonAttributeDao(final Map<String, List<Object>> backingMap) {
        this.setBackingMap(backingMap);
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        if (this.backingPerson == null) {
            return new HashSet<>();
        }

        return ImmutableSet.copyOf(this.backingPerson.getAttributes().keySet());
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return null;
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter,
                                                                     final Set<IPersonAttributes> resultPeople) {
        if (query == null) {
            throw new IllegalArgumentException("Illegal to invoke getPeople(Map) with a null argument.");
        }

        if (this.backingPerson == null) {
            return null;
        }
        return Collections.singleton(this.backingPerson);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPerson(java.lang.String)
     */
    @Override
    public IPersonAttributes getPerson(final String uid,
                                       final Set<IPersonAttributes> resultPeople,
                                       final IPersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getPerson(String) with a null argument.");
        }
        return this.backingPerson;
    }

    /**
     * Get the Map which this stub object will return for all legal invocations of
     * attributesForUser()
     *
     * @return Returns the backingMap.
     */
    public Map<String, List<Object>> getBackingMap() {
        return new HashMap(this.backingPerson.getAttributes());
    }

    /**
     * Set the Map which this stub object will return for all legal invocations of
     * attributesForUser().
     *
     * @param backingMap The backingMap to set, may not be null.
     */
    public void setBackingMap(final Map<String, List<Object>> backingMap) {
        this.backingPerson = new AttributeNamedPersonImpl(backingMap);
    }
}
