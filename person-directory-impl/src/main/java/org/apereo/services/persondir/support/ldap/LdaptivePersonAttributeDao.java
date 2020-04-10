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
package org.apereo.services.persondir.support.ldap;

import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.apereo.services.persondir.support.CaseInsensitiveAttributeNamedPersonImpl;
import org.apereo.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.directory.SearchControls;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Person directory <code>IPersonAttribute</code> implementation that queries an LDAP directory
 * with ldaptive components to populate person attributes.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class LdaptivePersonAttributeDao extends AbstractQueryPersonAttributeDao<FilterTemplate> {

    /**
     * Logger instance.
     **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Search base DN.
     */
    private String baseDN;

    /**
     * Search controls.
     */
    private SearchControls searchControls;

    /**
     * LDAP connection factory.
     */
    private ConnectionFactory connectionFactory;

    /**
     * LDAP search filter.
     */
    private String searchFilter;

    public LdaptivePersonAttributeDao() {
        super();
    }

    /**
     * Sets the base DN of the LDAP search for attributes.
     *
     * @param dn LDAP base DN of search.
     */
    public void setBaseDN(final String dn) {
        this.baseDN = dn;
    }

    /**
     * Sets the LDAP search filter used to query for person attributes.
     *
     * @param filter Search filter of the form "(usernameAttribute={0})" where {0} and similar ordinal placeholders
     *               are replaced with query parameters.
     */
    public void setSearchFilter(final String filter) {
        this.searchFilter = filter;
    }

    /**
     * Sets a number of parameters that control LDAP search semantics including search scope,
     * maximum number of results retrieved, and search timeout.
     *
     * @param searchControls LDAP search controls.
     */
    public void setSearchControls(final SearchControls searchControls) {
        this.searchControls = searchControls;
    }

    /**
     * Sets the connection factory that produces LDAP connections on which searches occur. It is strongly recommended
     * that this be a <code>PooledConnecitonFactory</code> object.
     *
     * @param connectionFactory LDAP connection factory.
     */
    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /*
     * Closes the underlying connectionFactory.
    */
    public void close() {
        this.connectionFactory.close();
    }

    @Override
    protected List<IPersonAttributes> getPeopleForQuery(final FilterTemplate filter, final String userName) {
        final SearchResponse response;
        try {
            response = new SearchOperation(this.connectionFactory).execute(createRequest(filter));
        } catch (final LdapException e) {
            throw new RuntimeException("Failed executing LDAP query " + filter, e);
        }
        final List<IPersonAttributes> peopleAttributes = new ArrayList<>(response.entrySize());
        for (final LdapEntry entry : response.getEntries()) {
            final IPersonAttributes person;
            final String userNameAttribute = this.getConfiguredUserNameAttribute();
            final Map<String, List<Object>> attributes = convertLdapEntryToMap(entry);
            if (attributes.containsKey(userNameAttribute)) {
                person = new CaseInsensitiveAttributeNamedPersonImpl(userNameAttribute, attributes);
            } else {
                person = new CaseInsensitiveNamedPersonImpl(userName, attributes);
            }
            peopleAttributes.add(person);
        }
        return peopleAttributes;

    }

    @Override
    protected FilterTemplate appendAttributeToQuery(final FilterTemplate filter, final String attribute, final List<Object> values) {
        final FilterTemplate query;
        if (filter == null) {
            query = new FilterTemplate(this.searchFilter);
        } else {
            query = filter;
        }

        if (this.isUseAllQueryAttributes() &&
            values.size() > 1 &&
            (this.searchFilter.contains("{0}") ||
             this.searchFilter.contains("{user}"))) {
            logger.warn("Query value will be indeterminate due to multiple attributes and no username indicator. Use attribute [{}] in query instead of {0} or {user}",
                attribute);
        }

        if (values.size() > 0) {
            if (this.searchFilter.contains("{0}")) {
                query.setParameter(0, values.get(0).toString());
            } else if (this.searchFilter.contains("{user}")) {
                query.setParameter("user", values.get(0).toString());
            } else if (this.searchFilter.contains("{" + attribute + "}")) {
                query.setParameter(attribute, values.get(0).toString());
            }
            logger.debug("Constructed LDAP search query [{}]", query.format());
        }
        return query;
    }

    /**
     * Creates a search request from a search filter.
     *
     * @param filter LDAP search filter.
     * @return ldaptive search request.
     */
    private SearchRequest createRequest(final FilterTemplate filter) {
        final SearchRequest request = new SearchRequest();
        request.setBaseDn(this.baseDN);
        request.setFilter(filter);

        /** LDAP attributes to fetch from search results. */
        if (getResultAttributeMapping() != null && !getResultAttributeMapping().isEmpty()) {
            final String[] attributes = getResultAttributeMapping().keySet().toArray(new String[getResultAttributeMapping().size()]);
            request.setReturnAttributes(attributes);
        } else if (searchControls.getReturningAttributes() != null && searchControls.getReturningAttributes().length > 0) {
            request.setReturnAttributes(searchControls.getReturningAttributes());
        } else {
            request.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        }

        SearchScope searchScope = SearchScope.SUBTREE;
        for (final SearchScope scope : SearchScope.values()) {
            if (scope.ordinal() == this.searchControls.getSearchScope()) {
                searchScope = scope;
            }
        }
        request.setSearchScope(searchScope);
        request.setSizeLimit(Long.valueOf(this.searchControls.getCountLimit()).intValue());
        request.setTimeLimit(Duration.ofSeconds(searchControls.getTimeLimit()));
        return request;
    }

    /**
     * Converts an ldaptive <code>LdapEntry</code> containing result entry attributes into an attribute map as needed
     * by Person Directory components.
     *
     * @param entry Ldap entry.
     * @return Attribute map.
     */
    private Map<String, List<Object>> convertLdapEntryToMap(final LdapEntry entry) {
        final Map<String, List<Object>> attributeMap = new LinkedHashMap<>(entry.size());
        for (final LdapAttribute attr : entry.getAttributes()) {
            attributeMap.put(attr.getName(), new ArrayList<Object>(attr.getStringValues()));
        }
        logger.debug("Converted ldap DN entry [{}] to attribute map {}", entry.getDn(), attributeMap.toString());
        return attributeMap;
    }
}
