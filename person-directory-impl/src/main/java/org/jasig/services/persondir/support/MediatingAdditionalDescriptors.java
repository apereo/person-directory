/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.services.persondir.support;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Uses a List of {@link IAdditionalDescriptors} objects to delegate method calls to. For set/add/remove
 * operations all delegates are called. For get operations the first delegate to return a non-null/empty
 * result is used.
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MediatingAdditionalDescriptors implements IAdditionalDescriptors {
    private static final long serialVersionUID = 1L;

    private List<IAdditionalDescriptors> delegateDescriptors = Collections.emptyList();


    public void setDelegateDescriptors(final List<IAdditionalDescriptors> delegateDescriptors) {
        Validate.noNullElements(delegateDescriptors, "delegateDescriptors List cannot be null or contain null attributes");
        this.delegateDescriptors = new ArrayList<>(delegateDescriptors);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#addAttributes(java.util.Map)
     */
    @Override
    public void addAttributes(final Map<String, List<Object>> attributes) {
        for (final IAdditionalDescriptors additionalDescriptors : this.delegateDescriptors) {
            additionalDescriptors.addAttributes(attributes);
        }
    }

    /**
     * Returns list of all removed values
     *
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#removeAttribute(java.lang.String)
     */
    @Override
    public List<Object> removeAttribute(final String name) {
        List<Object> removedValues = null;

        for (final IAdditionalDescriptors additionalDescriptors : this.delegateDescriptors) {
            final List<Object> values = additionalDescriptors.removeAttribute(name);
            if (values != null) {
                if (removedValues == null) {
                    removedValues = new ArrayList<>(values);
                } else {
                    removedValues.addAll(values);
                }
            }
        }

        return removedValues;
    }

    /**
     * Returns list of all replaced values
     *
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setAttributeValues(java.lang.String, java.util.List)
     */
    @Override
    public List<Object> setAttributeValues(final String name, final List<Object> values) {
        List<Object> replacedValues = null;

        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            final List<Object> oldValues = additionalDescriptors.setAttributeValues(name, values);
            if (oldValues != null) {
                if (replacedValues == null) {
                    replacedValues = new ArrayList<>(oldValues);
                } else {
                    replacedValues.addAll(oldValues);
                }
            }
        }

        return replacedValues;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setAttributes(java.util.Map)
     */
    @Override
    public void setAttributes(final Map<String, List<Object>> attributes) {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            additionalDescriptors.setAttributes(attributes);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            additionalDescriptors.setName(name);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValue(java.lang.String)
     */
    @Override
    public Object getAttributeValue(final String name) {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            final Map<String, List<Object>> attributes = additionalDescriptors.getAttributes();
            if (attributes != null && attributes.containsKey(name)) {
                return additionalDescriptors.getAttributeValue(name);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValues(java.lang.String)
     */
    @Override
    public List<Object> getAttributeValues(final String name) {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            final Map<String, List<Object>> attributes = additionalDescriptors.getAttributes();
            if (attributes != null && attributes.containsKey(name)) {
                return additionalDescriptors.getAttributeValues(name);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributes()
     */
    @Override
    public Map<String, List<Object>> getAttributes() {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            final Map<String, List<Object>> attributes = additionalDescriptors.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                return attributes;
            }
        }

        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        for (final IAdditionalDescriptors additionalDescriptors : delegateDescriptors) {
            final String name = additionalDescriptors.getName();
            if (name != null) {
                return name;
            }
        }

        return null;
    }
}
