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
package org.apereo.services.persondir.support.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.apereo.services.persondir.util.CollectionsUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that evaluates
 * person directory information based on configurable rules.  You may chain as 
 * many rules as you like, but this DAO will apply <b>at most</b> one rule, the
 * first that triggers.
 *
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
 *         <td  valign="top">rules</td>
 *         <td>
 *             The array of {@link AttributeRule}s to use when 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 */
@Deprecated
public final class DeclaredRulePersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * List of {@link AttributeRule} objects.
     */
    private List<AttributeRule> rules;

    public DeclaredRulePersonAttributeDao() {
        super();
    }

    /**
     * Creates a new DeclaredRulePersonAttributeDao specifying the attributeName to pass to
     * {@link #getUsernameAttributeProvider()} and the {@link List} of {@link AttributeRule}s
     * to pass to {@link #setRules(List)}
     *
     * @param attributeName attribute name
     * @param rules rules
     */
    public DeclaredRulePersonAttributeDao(final String attributeName, final List<AttributeRule> rules) {
        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Creating DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }

        // Instance Members.
        var usernameAttributeProvider = new SimpleUsernameAttributeProvider(attributeName);
        this.setUsernameAttributeProvider(usernameAttributeProvider);
        this.setRules(rules);

        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Created DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }
    }

    /**
     * @return the rules
     */
    public List<AttributeRule> getRules() {
        return this.rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(final List<AttributeRule> rules) {
        Validate.notEmpty(rules, "Argument 'rules' cannot be null or empty.");

        this.rules = CollectionsUtil.safelyWrapAsUnmodifiableList(new ArrayList<>(rules));
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> seed,
                                                                     final IPersonAttributeDaoFilter filter) {
        Validate.notNull(seed, "Argument 'seed' cannot be null.");

        for (var rule : this.rules) {
            if (rule.appliesTo(seed)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Evaluating rule='" + rule + "' from the rules List");
                }

                return rule.evaluate(seed);
            }
        }

        return null;
    }

    /**
     * Aggregates the results of calling {@link AttributeRule#getPossibleUserAttributeNames()}
     * on each {@link AttributeRule} instance in the rules array.
     *
     * @see IPersonAttributeDao#getPossibleUserAttributeNames(org.apereo.services.persondir.IPersonAttributeDaoFilter)
     */
    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        final Set<String> rslt = new LinkedHashSet<>();

        for (var rule : this.rules) {
            var possibleUserAttributeNames = rule.getPossibleUserAttributeNames();
            rslt.addAll(possibleUserAttributeNames);
        }

        return rslt;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        final Set<String> rslt = new LinkedHashSet<>();

        for (var rule : this.rules) {
            var possibleUserAttributeNames = rule.getAvailableQueryAttributes();
            rslt.addAll(possibleUserAttributeNames);
        }

        return rslt;
    }
}
