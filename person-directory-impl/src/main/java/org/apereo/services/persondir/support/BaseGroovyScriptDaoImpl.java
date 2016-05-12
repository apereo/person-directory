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

import org.apereo.services.persondir.IPersonAttributeScriptDao;

import java.util.List;
import java.util.Map;

/**
 * Base implementation that allows Groovy script implementations to extend this class and only implement the methods
 * they provide results for to keep the groovy script simpler.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public abstract class BaseGroovyScriptDaoImpl implements IPersonAttributeScriptDao {

    /* Javadoc inherited. See interface. */
    @Override
    public Map<String, Object> getAttributesForUser(final String username) {
        throw new UnsupportedOperationException();
    }

    /* Javadoc inherited. See interface. */
    @Override
    public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(final Map<String, List<Object>> attributes) {
        throw new UnsupportedOperationException();
    }
}
