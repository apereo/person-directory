/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support;

import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base {@link IPersonAttributeDao} that provides implementations of the deprecated methods.
 *
 * @author Eric Dalquist
 */
public abstract class BasePersonAttributeDao implements IPersonAttributeDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private int order;
    private String[] id = new String[]{getClass().getSimpleName()};
    private boolean enabled = true;
    private IPersonAttributeDaoFilter personAttributeDaoFilter;

    public BasePersonAttributeDao() {
        super();
    }


    @Override
    public final Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed) {
        if (!this.enabled) {
            return null;
        }
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


    @Override
    public final Map<String, List<Object>> getMultivaluedUserAttributes(final String uid) {
        if (!this.enabled) {
            return null;
        }
        final IPersonAttributes person = this.getPerson(uid);

        if (person == null) {
            return null;
        }

        //Make a mutable copy of the person's attributes
        return new LinkedHashMap<>(person.getAttributes());
    }

    @Override
    public final Map<String, Object> getUserAttributes(final Map<String, Object> seed) {
        if (!this.enabled) {
            return null;
        }
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
    @Override
    public final Map<String, Object> getUserAttributes(final String uid) {
        if (!this.enabled) {
            return null;
        }
        Validate.notNull(uid, "uid may not be null.");

        //Get the attributes from the subclass
        final Map<String, List<Object>> multivaluedUserAttributes = this.getMultivaluedUserAttributes(uid);

        return this.flattenResults(multivaluedUserAttributes);
    }

    /**
     * Takes a &lt;String, List&lt;Object&gt;&gt; Map and coverts it to a &lt;String, Object&gt; Map. This implementation takes
     * the first value of each List to use as the value for the new Map.
     *
     * @param multivaluedUserAttributes The attribute map to flatten.
     * @return A flattened version of the Map, null if the argument was null.
     * @deprecated This method is just used internally and will be removed with this class in 1.6
     */
    @Deprecated
    protected Map<String, Object> flattenResults(final Map<String, List<Object>> multivaluedUserAttributes) {
        if (!this.enabled) {
            return null;
        }

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
            } else {
                value = attrValues.get(0);
            }

            userAttributes.put(attrName, value);
        }

        logger.debug("Flattened Map='{}' into Map='{}'", multivaluedUserAttributes, userAttributes);

        return userAttributes;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @Override
    public int compareTo(final IPersonAttributeDao o) {
        return this.order;
    }

    @Override
    public String[] getId() {
        return this.id;
    }

    public void setId(final String... id) {
        this.id = id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public IPersonAttributeDaoFilter getPersonAttributeDaoFilter() {
        return personAttributeDaoFilter;
    }

    @Override
    public void setPersonAttributeDaoFilter(final IPersonAttributeDaoFilter personAttributeDaoFilter) {
        this.personAttributeDaoFilter = personAttributeDaoFilter;
    }
}
