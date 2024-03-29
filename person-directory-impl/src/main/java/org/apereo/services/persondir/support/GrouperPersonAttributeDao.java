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

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class implementing a minimal <code>IPersonAttributeDao</code> API only used by CAS which simply reads all
 * the groups from Grouper repository
 * for a given principal and adopts them to <code>IPersonAttributes</code> instance.
 * All other unimplemented methods throw <code>UnsupportedOperationException</code>
 * <br>
 * This implementation uses Grouper's <i>grouperClient</i> library to query Grouper's back-end repository.
 * <br>
 * <p>
 * Note: All the Grouper server connection configuration for grouperClient is defined in
 * <i>grouper.client.properties</i> file and must be available
 * in client application's (CAS web application) classpath.
 *
 * @author Dmitriy Kopylenko
 */
public class GrouperPersonAttributeDao extends BasePersonAttributeDao {
    public static final String DEFAULT_GROUPER_ATTRIBUTES_KEY = "grouperGroups";

    private IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private Map<String, String> parameters = new LinkedHashMap<>();

    private GrouperSubjectType subjectType = GrouperSubjectType.SUBJECT_ID;

    private GroupAttributeValueType groupAttributeValueType = GroupAttributeValueType.NAME;

    public GroupAttributeValueType getGroupAttributeValueType() {
        return groupAttributeValueType;
    }

    public void setGroupAttributeValueType(final GroupAttributeValueType groupAttributeValueType) {
        this.groupAttributeValueType = groupAttributeValueType;
    }

    public IUsernameAttributeProvider getUsernameAttributeProvider() {
        return usernameAttributeProvider;
    }

    public void setUsernameAttributeProvider(final IUsernameAttributeProvider usernameAttributeProvider) {
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    public GrouperSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(final GrouperSubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public IPersonAttributes getPerson(final String subjectId, final Set<IPersonAttributes> resultPeople, final IPersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        Objects.requireNonNull(subjectId, "username cannot be null");

        var groupsClient = getGroupsClient();
        switch (this.subjectType) {
            case SUBJECT_IDENTIFIER -> groupsClient.addSubjectIdentifier(subjectId);
            case SUBJECT_ATTRIBUTE_NAME -> groupsClient.addSubjectAttributeName(subjectId);
            case SUBJECT_ID -> groupsClient.addSubjectId(subjectId);
        }

        parameters.forEach(groupsClient::addParam);
        var grouperGroupsAsAttributesMap = new HashMap<String, List<Object>>(1);
        var groupsList = retrieveAttributesFromGrouper(groupsClient);
        grouperGroupsAsAttributesMap.put("grouperGroups", groupsList);
        return new NamedPersonImpl(subjectId, grouperGroupsAsAttributesMap);
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
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query,
                                            final IPersonAttributeDaoFilter filter,
                                            final Set<IPersonAttributes> resultPeople) {
        return getPeopleWithMultivaluedAttributes(MultivaluedPersonAttributeUtils.stuffAttributesIntoListValues(query, filter), filter, resultPeople);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter,
                                                                     final Set<IPersonAttributes> resultPeople) {
        var people = new LinkedHashSet<IPersonAttributes>();
        var username = usernameAttributeProvider.getUsernameFromQuery(query);
        var person = getPerson(username, resultPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    protected List<Object> retrieveAttributesFromGrouper(final GcGetGroups groupsClient) {
        var groupsList = new ArrayList<>();
        try {
            for (final WsGetGroupsResult groupsResult : groupsClient.execute().getResults()) {
                var wsGroups = groupsResult.getWsGroups();
                if (wsGroups != null) {
                    for (final WsGroup group : wsGroups) {
                        switch (groupAttributeValueType) {
                            case NAME -> groupsList.add(group.getName());
                            case EXTENSION -> groupsList.add(group.getExtension());
                            case DISPLAY_EXTENSION -> groupsList.add(group.getDisplayExtension());
                            case DISPLAY_NAME -> groupsList.add(group.getDisplayName());
                            case UUID -> groupsList.add(group.getUuid());
                            case ALTERNATE_NAME -> groupsList.add(group.getAlternateName());
                        }
                    }
                }
            }
            return groupsList;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return groupsList;
    }

    protected GcGetGroups getGroupsClient() {
        return new GcGetGroups();
    }

    public enum GroupAttributeValueType {
        DISPLAY_EXTENSION,
        DISPLAY_NAME,
        UUID,
        ALTERNATE_NAME,
        NAME,
        EXTENSION,
    }
    public enum GrouperSubjectType {
        SUBJECT_ID,
        SUBJECT_IDENTIFIER,
        SUBJECT_ATTRIBUTE_NAME
    }
}
