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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.util.CollectionsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IPersonAttributes} for use with 
 * {@link AdditionalDescriptorsPersonAttributeDao}.  Unlike most other 
 * implementations, <code>AdditionalDescriptors</code> is mutable.  An instance 
 * of this class typically lives for the duration of a session or application, 
 * and external components are expected to add attributes to the collection 
 * after creation. 
 *
 * @author awills
 */
public class AdditionalDescriptors implements IAdditionalDescriptors {

    // Static Members.
    private static final long serialVersionUID = 1L;

    // Private Members.
    private String name = null;
    private Map<String, List<Object>> attributes = new ConcurrentHashMap<>();

    /*
     * Public API.
     */

    @Override
    public Object getAttributeValue(final String name) {
        final List<Object> values = attributes.get(name);
        return values == null || values.size() == 0 ? null : values.get(0);
    }

    @Override
    public List<Object> getAttributeValues(final String name) {
        final List<Object> values = this.attributes.get(name);
        if (values == null) {
            return null;
        }
        return CollectionsUtil.safelyWrapAsUnmodifiableList(values);
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return CollectionsUtil.safelyWrapAsUnmodifiableMap(this.attributes);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void addAttributes(final Map<String, List<Object>> attributes) {
        for (final Map.Entry<String, List<Object>> newAttribute : attributes.entrySet()) {
            final String name = newAttribute.getKey();
            final List<Object> values = newAttribute.getValue();

            if (values == null) {
                this.attributes.put(name, null);
            } else {
                this.attributes.put(name, new ArrayList<>(values));
            }
        }
    }

    @Override
    public void setAttributes(final Map<String, List<Object>> attributes) {
        Validate.notNull(attributes, "Argument 'attributes' cannot be null");
        final Map<String, List<Object>> newAttributes = new ConcurrentHashMap<>();

        for (final Map.Entry<String, List<Object>> newAttribute : attributes.entrySet()) {
            final String name = newAttribute.getKey();
            final List<Object> values = newAttribute.getValue();

            if (values == null) {
                newAttributes.put(name, null);
            } else {
                newAttributes.put(name, new ArrayList<>(values));
            }
        }

        this.attributes = newAttributes;
    }

    @Override
    public List<Object> setAttributeValues(final String name, final List<Object> values) {
        // Assertions.
        if (name == null) {
            final String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (values == null) {
            return this.attributes.put(name, null);
        }

        return this.attributes.put(name, new ArrayList<>(values));
    }

    @Override
    public List<Object> removeAttribute(final String name) {
        Validate.notNull(name, "Argument 'name' cannot be null");
        return this.attributes.remove(name);
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
        final IPersonAttributes rhs = (IPersonAttributes) object;
        return new EqualsBuilder()
                .append(this.name, rhs.getName())
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1574945487, 827742191)
                .append(this.name)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", this.name)
                .append("attributes", this.attributes)
                .toString();
    }
}
