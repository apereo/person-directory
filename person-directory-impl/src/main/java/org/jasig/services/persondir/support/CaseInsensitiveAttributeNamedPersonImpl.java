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

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jasig.services.persondir.IPersonAttributes;

import java.util.List;
import java.util.Map;

/**
 * Custom IPersonAttributes that uses a case insensitive Map to hide attribute name case
 */
public class CaseInsensitiveAttributeNamedPersonImpl extends AttributeNamedPersonImpl {
    private static final long serialVersionUID = 1L;

    public CaseInsensitiveAttributeNamedPersonImpl(final Map<String, List<Object>> attributes) {
        super(attributes);
    }

    public CaseInsensitiveAttributeNamedPersonImpl(final String userNameAttribute, final Map<String, List<Object>> attributes) {
        super(userNameAttribute, attributes);
    }

    public CaseInsensitiveAttributeNamedPersonImpl(final IPersonAttributes personAttributes) {
        super(personAttributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.BasePersonImpl#createImmutableAttributeMap(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Object>> createImmutableAttributeMap(final int size) {
        return new CaseInsensitiveMap(size > 0 ? size : 1);
    }
}
