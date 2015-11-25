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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;

/**
 * Base {@link IPersonAttributeDao} that provides implementations of the deprecated methods. This class will be removed
 * in 1.6
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BasePersonAttributeDao implements IPersonAttributeDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    public BasePersonAttributeDao() {
        super();
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(java.util.Map)
     */
    public final Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed) {
        final Set<IPersonAttributes> people = this.getPeopleWithMultivaluedAttributes(seed);

        //Get the first IPersonAttributes to return data for
        final IPersonAttributes person = DataAccessUtils.singleResult(people);
        
        //If null or no results return null
        if (person == null) {
            return null;
        }
        
        //Make a mutable copy of the person's attributes
        return new LinkedHashMap<>(person.getAttributes());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(java.lang.String)
     */
    public final Map<String, List<Object>> getMultivaluedUserAttributes(final String uid) {
        final IPersonAttributes person = this.getPerson(uid);
        
        if (person == null) {
            return null;
        }

        //Make a mutable copy of the person's attributes
        return new LinkedHashMap<>(person.getAttributes());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public final Map<String, Object> getUserAttributes(final Map<String, Object> seed) {
        final Set<IPersonAttributes> people = this.getPeople(seed);

        //Get the first IPersonAttributes to return data for
        final IPersonAttributes person = DataAccessUtils.singleResult(people);
        
        //If null or no results return null
        if (person == null) {
            return null;
        }

        final Map<String, List<Object>> multivaluedUserAttributes = new LinkedHashMap<>(person.getAttributes());
        return this.flattenResults(multivaluedUserAttributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public final Map<String, Object> getUserAttributes(final String uid) {
        Validate.notNull(uid, "uid may not be null.");
        
        //Get the attributes from the subclass
        final Map<String, List<Object>> multivaluedUserAttributes = this.getMultivaluedUserAttributes(uid);
        
        return this.flattenResults(multivaluedUserAttributes);
    }

    /**
     * Takes a &lt;String, List&lt;Object>> Map and coverts it to a &lt;String, Object> Map. This implementation takes
     * the first value of each List to use as the value for the new Map.
     * 
     * @param multivaluedUserAttributes The attribute map to flatten.
     * @return A flattened version of the Map, null if the argument was null.
     * @deprecated This method is just used internally and will be removed with this class in 1.6
     */
    @Deprecated
    protected Map<String, Object> flattenResults(final Map<String, List<Object>> multivaluedUserAttributes) {
        if (multivaluedUserAttributes == null) {
            return null;
        }
        
        //Convert the <String, List<Object> results map to a <String, Object> map using the first value of each List
        final Map<String, Object> userAttributes = new LinkedHashMap<>(multivaluedUserAttributes.size());
        
        for (final Map.Entry<String, List<Object>> attrEntry : multivaluedUserAttributes.entrySet()) {
            final String attrName = attrEntry.getKey();
            final List<Object> attrValues = attrEntry.getValue();
            
            final Object value;
            if (attrValues == null || attrValues.size() == 0) {
                value = null;
            }
            else {
                value = attrValues.get(0);
            }
            
            userAttributes.put(attrName, value);
        }
        
        logger.debug("Flattened Map='{}' into Map='{}'", multivaluedUserAttributes, userAttributes);

        return userAttributes;
    }
}
