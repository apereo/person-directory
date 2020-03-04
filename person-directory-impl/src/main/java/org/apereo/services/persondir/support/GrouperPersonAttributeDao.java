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

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class implementing a minimal <code>IPersonAttributeDao</code> API only used by CAS which simply reads all
 * the groups from Grouper repository
 * for a given principal and adopts them to <code>IPersonAttributes</code> instance. 
 * All other unimplemented methods throw <code>UnsupportedOperationException</code>
 * <br>
 * This implementation uses Grouper's <i>grouperClient</i> library to query Grouper's back-end repository.
 * <br>
 *
 * Note: All the Grouper server connection configuration for grouperClient is defined in
 * <i>grouper.client.properties</i> file and must be available
 * in client application's (CAS web application) classpath.
 *
 * @author Dmitriy Kopylenko
 */
public class GrouperPersonAttributeDao extends BasePersonAttributeDao {

    public static final String DEFAULT_GROUPER_ATTRIBUTES_KEY = "grouperGroups";

    @Override
    public IPersonAttributes getPerson(final String subjectId, final IPersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        final GcGetGroups groupsClient = new GcGetGroups().addSubjectId(subjectId);
        final Map<String, List<Object>> grouperGroupsAsAttributesMap = new HashMap<>(1);
        final List<Object> groupsList = new ArrayList<>();
        grouperGroupsAsAttributesMap.put("grouperGroups", groupsList);
        final IPersonAttributes personAttributes = new AttributeNamedPersonImpl(grouperGroupsAsAttributesMap);

        //Now retrieve and populate the attributes (groups from Grouper)
        for (final WsGetGroupsResult groupsResult : groupsClient.execute().getResults()) {
            for (final WsGroup group : groupsResult.getWsGroups()) {
                groupsList.add(group.getName());
            }
        }
        return personAttributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> stringObjectMap,
                                            final IPersonAttributeDaoFilter filter) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> stringListMap,
                                                                     final IPersonAttributeDaoFilter filter) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }
}
