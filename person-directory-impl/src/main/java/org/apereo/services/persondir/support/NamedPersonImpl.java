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

import java.util.List;
import java.util.Map;

/**
 * @author Eric Dalquist

 */
public class NamedPersonImpl extends BasePersonImpl {
    private static final long serialVersionUID = 1L;

    private final String userName;

    public NamedPersonImpl(final String userName, final Map<String, List<Object>> attributes) {
        super(attributes);

        this.userName = userName;
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return this.userName;
    }
}
