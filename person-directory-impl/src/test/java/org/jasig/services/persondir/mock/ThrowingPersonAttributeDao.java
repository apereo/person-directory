/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * A mock, test implementation of ThrowingPersonAttributeDao which always
 * throws a RuntimeException.
 */
public class ThrowingPersonAttributeDao implements IPersonAttributeDao {

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    public Set<String> getAvailableQueryAttributes() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeople(java.util.Map)
     */
    public Set<IPersonAttributes> getPeople(Map<String, Object> query) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPerson(java.lang.String)
     */
    public IPersonAttributes getPerson(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(java.util.Map)
     */
    @SuppressWarnings("deprecation")
    public Map<String, List<Object>> getMultivaluedUserAttributes(Map<String, List<Object>> seed) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public Map<String, List<Object>> getMultivaluedUserAttributes(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    @SuppressWarnings("deprecation")
    public Map<String, Object> getUserAttributes(Map<String, Object> seed) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public Map<String, Object> getUserAttributes(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
}