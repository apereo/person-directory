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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link IPersonAttributeDao} that allows other subsystems 
 * and components to <i>push</i> attributes to the <code>IPersonAttributeDao</code> 
 * stack.  The collection of pushed attributes is represented by the 
 * <code>descriptors</code> property and backed by an instance of 
 * {@link AdditionalDescriptors}.  In most cases this property should be 
 * configured as a Session-Scoped Proxy Bean.
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th>Property</th>
 *         <th>Description</th>
 *         <th>Required</th>
 *         <th>Default</th>
 *     </tr>
 *     <tr>
 *         <td  valign="top">descriptors</td>
 *         <td>
 *             The {@link IPersonAttributes} object that models the collection 
 *             of pushed attributes.  In most cases this property should be configured 
 *             as a Session-Scoped Proxy Bean. 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 *
 * @author awills
 */
public class AdditionalDescriptorsPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    // Instance Members.
    private IPersonAttributes descriptors;
    private ICurrentUserProvider currentUserProvider;
    private CaseCanonicalizationMode usernameCaseCanonicalizationMode = AbstractQueryPersonAttributeDao.DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
    private Locale usernameCaseCanonicalizationLocale = Locale.getDefault();
    private Set<String> possibleUserAttributeNames = null;  // default

    /*
     * Public API.
     */

    /**
     * Called by Spring DI to inject the collection of additional descriptors.  
     * Descriptors are user specific, and (therefore) the <code>Map</code> must 
     * be a session-scoped bean.
     *
     * @param descriptors Additional descriptors
     */
    @Required
    public void setDescriptors(final IPersonAttributes descriptors) {

        // Assertions.
        if (descriptors == null) {
            var msg = "Argument 'descriptors' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        this.descriptors = descriptors;

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("invoking setDescriptors(" + descriptors + ")");
        }

    }

    public ICurrentUserProvider getCurrentUserProvider() {
        return currentUserProvider;
    }

    /**
     * Sets the {@link ICurrentUserProvider} to use when determining if the
     * additional attributes should be returned.
     *
     * @param currentUserProvider current user provider
     */
    public void setCurrentUserProvider(final ICurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Returns a <code>Set</code> containing only the configured username attribute.
     */
    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        var usernameAttributeProvider = super.getUsernameAttributeProvider();
        return Collections.singleton(usernameAttributeProvider.getUsernameAttribute());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    @Override
    @JsonIgnore
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("invoking getPeopleWithMultivaluedAttributes(" + query + ")");
        }

        var usernameAttributeProvider = super.getUsernameAttributeProvider();
        var uid = usernameAttributeProvider.getUsernameFromQuery(query);
        if (uid == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No username attribute found in query, returning null");
            }

            return null;
        }
        uid = usernameCaseCanonicalizationMode.canonicalize(uid, usernameCaseCanonicalizationLocale);

        var targetName = this.descriptors.getName();
        if (targetName == null) {
            if (this.currentUserProvider != null) {
                targetName = this.currentUserProvider.getCurrentUserName();
            }

            if (targetName == null) {
                this.logger.warn("AdditionalDescriptors has a null name and a null name was returned by the currentUserProvider, returning null. " + this.descriptors);
                return null;
            }
        }

        targetName = usernameCaseCanonicalizationMode.canonicalize(targetName, usernameCaseCanonicalizationLocale);
        if (uid.equals(targetName)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding additional descriptors " + this.descriptors);
            }

            var personAttributes = new CaseInsensitiveNamedPersonImpl(targetName, this.descriptors.getAttributes());
            return Collections.singleton(personAttributes);
        }

        return null;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return possibleUserAttributeNames;
    }

    /**
     * Allows the developer to configure the set of possible attribute names in
     * the Spring application context.  Some downstream clients of
     * person-directory need to know what attributes _may_ be present.  The only
     * way for this bean to know that is to tell it in config.
     *
     * @param possibleUserAttributeNames Set of possible attribute names.
     * @since 1.6.2
     */
    public void setPossibleUserAttributeNames(final Set<String> possibleUserAttributeNames) {
        this.possibleUserAttributeNames = possibleUserAttributeNames;
    }

    public CaseCanonicalizationMode getUsernameCaseCanonicalizationMode() {
        return usernameCaseCanonicalizationMode;
    }

    public void setUsernameCaseCanonicalizationMode(final CaseCanonicalizationMode usernameCaseCanonicalizationMode) {
        if (usernameCaseCanonicalizationMode == null) {
            this.usernameCaseCanonicalizationMode = AbstractQueryPersonAttributeDao.DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
        } else {
            this.usernameCaseCanonicalizationMode = usernameCaseCanonicalizationMode;
        }
    }

    public Locale getUsernameCaseCanonicalizationLocale() {
        return usernameCaseCanonicalizationLocale;
    }

    public void setUsernameCaseCanonicalizationLocale(final Locale usernameCaseCanonicalizationLocale) {
        if (usernameCaseCanonicalizationLocale == null) {
            this.usernameCaseCanonicalizationLocale = Locale.getDefault();
        } else {
            this.usernameCaseCanonicalizationLocale = usernameCaseCanonicalizationLocale;
        }
    }

}
