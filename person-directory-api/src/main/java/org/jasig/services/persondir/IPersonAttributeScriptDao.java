/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir;

import java.util.List;
import java.util.Map;

/**
 * Simplified DAO interface for use by Groovy scripts that provide user attributes.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public interface IPersonAttributeScriptDao {
    /**
     * Given a username, provide additional attributes.
     * @param username username
     * @return Map of attributes to add for the user
     */
    Map<String, Object> getAttributesForUser(String username);

    /**
     * Given a set of attributes, return additional attributes to add to the user's attributes.  Implementations
     * determine whether to return a super-set that includes the original attributes plus additional attributes, or
     * just those additional attributes to add.
     * @param attributes Map of user's attributes
     * @return Map of attributes to add to the user (implementations may return super-set that includes original
     *         attributes or just additional attributes to add).
     */
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes);
}
