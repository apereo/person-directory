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
package org.apereo.services.persondir.support.ldap;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creates a Map for each Attributes result with attribute names as keys
 * and Lists of Attribute values for values.
 *
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
class AttributeMapAttributesMapper implements AttributesMapper {
    private final boolean ignoreNull;

    public AttributeMapAttributesMapper() {
        this(false);
    }

    public AttributeMapAttributesMapper(final boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    @Override
    public Object mapFromAttributes(final Attributes attributes) throws NamingException {
        final var attributeCount = attributes.size();
        final var mapOfAttrValues = this.createAttributeMap(attributeCount);

        for (final var attributesEnum = attributes.getAll(); attributesEnum.hasMore(); ) {
            final var attribute = attributesEnum.next();

            if (!this.ignoreNull || attribute.size() > 0) {
                final var attrName = attribute.getID();
                final var key = this.getAttributeKey(attrName);

                final var valuesEnum = attribute.getAll();
                final var values = this.getAttributeValues(valuesEnum);

                mapOfAttrValues.put(key, values);
            }
        }

        return mapOfAttrValues;
    }

    /**
     * Create a Map instance to be used as attribute map.
     * <br>
     * By default, a linked case-insensitive Map will be created
     *
     * @param attributeCount the attribute count, to be used as initial capacity for the Map
     * @return the new Map instance
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> createAttributeMap(final int attributeCount) {
        return new TreeMap(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Determine the key to use for the given attribute in the attribute Map.
     *
     * @param attributeName the attribute name as returned by the Attributes
     * @return the attribute key to use
     */
    protected String getAttributeKey(final String attributeName) {
        return attributeName;
    }

    /**
     * Convert the Attribute's NamingEnumeration of values into a List
     *
     * @param values The enumeration of Attribute values
     * @return The List version of the values enumeration
     */
    protected List<?> getAttributeValues(final NamingEnumeration<?> values) {
        return Collections.list(values);
    }
}
