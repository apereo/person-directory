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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Provides the username attribute based on a pre-configured string. Determines the username from a query Map based
 * on the configured attribute, {@link StringUtils#trimToNull(String)}, and if the username value does not contain a
 * wildcard.
 *
 * @author Eric Dalquist

 */
public class SimpleUsernameAttributeProvider implements IUsernameAttributeProvider {
    private static final Logger logger = LoggerFactory.getLogger(SimpleUsernameAttributeProvider.class);

    private static final String DEFAULT_USERNAME_ATTRIBUTE = "username";

    private String usernameAttribute = DEFAULT_USERNAME_ATTRIBUTE;

    public SimpleUsernameAttributeProvider() {
    }

    public SimpleUsernameAttributeProvider(final String usernameAttribute) {
        this.setUsernameAttribute(usernameAttribute);
    }

    /**
     * The usernameAttribute to use .
     *
     * @param usernameAttribute name of the username attribute
     */
    public void setUsernameAttribute(final String usernameAttribute) {
        Validate.notNull(usernameAttribute);
        this.usernameAttribute = usernameAttribute;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IUsernameAttributeProvider#getUsernameAttribute()
     */
    @Override
    public String getUsernameAttribute() {
        return this.usernameAttribute;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IUsernameAttributeProvider#getUsernameFromQuery(java.util.Map)
     */
    @Override
    public String getUsernameFromQuery(final Map<String, List<Object>> query) {
        var usernameAttributeValues = getUsernameAttributeValues(query);
        logger.debug("Username attribute value found from the query map is {}", usernameAttributeValues);
        
        if (usernameAttributeValues == null || usernameAttributeValues.size() == 0) {
            return null;
        }

        var firstValue = usernameAttributeValues.get(0);
        if (firstValue == null) {
            return null;
        }

        var username = StringUtils.trimToNull(String.valueOf(firstValue));
        if (username == null || username.contains(IPersonAttributeDao.WILDCARD)) {
            return null;
        }

        return username;
    }

    private List<Object> getUsernameAttributeValues(final Map<String, List<Object>> query) {
        if (query.containsKey(this.usernameAttribute)) {
            List usernameAttributeValues = query.get(this.usernameAttribute);
            logger.debug("Using {} attribute to get username from the query map", this.usernameAttribute);
            return usernameAttributeValues;
        }
        List usernameAttributeValues = query.get(DEFAULT_USERNAME_ATTRIBUTE);
        logger.debug("Using {} attribute to get username from the query map", DEFAULT_USERNAME_ATTRIBUTE);
        return usernameAttributeValues;
    }
}
