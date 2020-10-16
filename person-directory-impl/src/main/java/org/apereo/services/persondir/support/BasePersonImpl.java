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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.util.CollectionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Eric Dalquist
 */
public abstract class BasePersonImpl implements IPersonAttributes {
    private static final long serialVersionUID = 1L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, List<Object>> attributes;

    public BasePersonImpl(final Map<String, List<Object>> attributes) {
        Validate.notNull(attributes, "attributes can not be null");

        final var immutableValuesBuilder = this.buildImmutableAttributeMap(attributes);

        // NOTE:  Do not return a copy of the map.  This must return the existing map or wrap the map with
        // an unmodifiable map so the underlying map still operates as case-insensitive for key comparison
        // in AbstractQueryPersonAttributeDao.mapPersonAttributes when the CaseInsensitive*Impl.java
        // subclasses are used.  James W 6/15
        // See https://issues.jasig.org/browse/PERSONDIR-89
        this.attributes = CollectionsUtil.safelyWrapAsUnmodifiableMap(immutableValuesBuilder);
    }

    /**
     * Take the constructor argument and convert the Map and List values into read-only form.
     *
     * @param attributes Map of attributes
     * @return Read-only map of attributes
     */
    protected Map<String, List<Object>> buildImmutableAttributeMap(final Map<String, List<Object>> attributes) {
        final var immutableValuesBuilder = this.createImmutableAttributeMap(attributes.size());
        final var arrayPattern = Pattern.compile("\\{(.*)\\}");
        for (final var attrEntry : attributes.entrySet()) {
            final var key = attrEntry.getKey();
            var value = attrEntry.getValue();

            if (value != null) {
                if (!value.isEmpty()) {
                    final var result = value.get(0);
                    if (result instanceof Array) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Column {} is classified as a SQL array", key);
                        }
                        final var values = result.toString();
                        if (logger.isTraceEnabled()) {
                            logger.trace("Converting SQL array values {} using pattern {}", values, arrayPattern.pattern());
                        }
                        final var matcher = arrayPattern.matcher(values);
                        if (matcher.matches()) {
                            final var groups = matcher.group(1).split(",");
                            value = Arrays.asList(groups);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Converted SQL array values {}", values);
                            }
                        }
                    }
                }
                value = CollectionsUtil.safelyWrapAsUnmodifiableList(value);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Collecting attribute {} with value(s) {}", key, value);
            }
            immutableValuesBuilder.put(key, value);
        }

        return immutableValuesBuilder;
    }

    /**
     * Create the Map used to store the attributes internally for this IPersonAttributes
     *
     * @param size size of map
     * @return Map to store attributes
     */
    protected Map<String, List<Object>> createImmutableAttributeMap(final int size) {
        return new LinkedHashMap<>(size > 0 ? size : 1);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValue(java.lang.String)
     */
    @Override
    public Object getAttributeValue(final String name) {
        final var values = this.attributes.get(name);
        if (values == null || values.size() == 0) {
            return null;
        }

        return values.get(0);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValues(java.lang.String)
     */
    @Override
    public List<Object> getAttributeValues(final String name) {
        return this.attributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributes()
     */
    @Override
    public Map<String, List<Object>> getAttributes() {
        return this.attributes;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPersonAttributes)) {
            return false;
        }
        final var rhs = (IPersonAttributes) object;
        return new EqualsBuilder()
            .append(this.getName(), rhs.getName())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1574945487, 827742191)
            .append(this.getName())
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", this.getName())
            .append("attributes", this.attributes)
            .toString();
    }
}
