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
package org.apereo.services.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Defines methods for finding a {@link IPersonAttributes} or Set of IPersons based on a user ID or a Map of user attributes to
 * query with.
 *
 * @author andrew.petro @yale.edu
 * @author Eric Dalquist
 */
public interface IPersonAttributeDao extends Comparable<IPersonAttributeDao> {
    /**
     * The constant WILDCARD.
     */
    String WILDCARD = "*";
    /**
     * The constant WILDCARD_PATTERN.
     */
    Pattern WILDCARD_PATTERN = Pattern.compile(Pattern.quote(IPersonAttributeDao.WILDCARD));

    /**
     * Searches for a single {@link IPersonAttributes} using the specified uid (userName).<br>
     * <p>
     * This method returns according to the following rules:<br>
     * <ul>
     * <li>If the user exists and has attributes a populated {@link IPersonAttributes} is returned.</li>
     * <li>If the user exists and has no attributes an {@link IPersonAttributes} with an empty attributes Map is returned.</li>
     * <li>If the user doesn't exist <code>null</code> is returned.</li>
     * <li>If an error occurs while find the person an appropriate exception will be thrown.</li>
     * </ul>
     *
     * @param uid    The userName of the person to find.
     * @param filter the filter
     * @return The populated {@link IPersonAttributes} for the specified uid, null if no person could be found for the uid.
     * @throws IllegalArgumentException If <code>uid</code> is <code>null.</code>
     */
    IPersonAttributes getPerson(String uid,
                                IPersonAttributeDaoFilter filter);

    default IPersonAttributes getPerson(final String uid) {
        return getPerson(uid, IPersonAttributeDaoFilter.alwaysChoose());
    }

    /**
     * Searches for {@link IPersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     *
     * @param query  A {@link Map} of name/value pair attributes to use in searching for {@link IPersonAttributes}s
     * @param filter the filter
     * @return A {@link Set} of {@link IPersonAttributes}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    Set<IPersonAttributes> getPeople(Map<String, Object> query,
                                     IPersonAttributeDaoFilter filter);

    default Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        return getPeople(query, IPersonAttributeDaoFilter.alwaysChoose());
    }

    /**
     * Searches for {@link IPersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     *
     * @param query  A {@link Map} of name/value pair attributes to use in searching for {@link IPersonAttributes}s
     * @param filter the filter
     * @return A {@link Set} of {@link IPersonAttributes}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query,
                                                              IPersonAttributeDaoFilter filter);

    default Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        return getPeopleWithMultivaluedAttributes(query, IPersonAttributeDaoFilter.alwaysChoose());
    }

    /**
     * Gets a {@link Set} of attribute names that may be returned for an IPersonAttributes. The names returned represent all
     * possible attributes names for the {@link IPersonAttributes} objects returned by the get methods. If the dao doesn't have a
     * way to know all possible attribute names this method should return <code>null</code>.
     * <br>
     * Returns an immutable {@link Set}.
     *
     * @param filter the filter
     * @return A {@link Set} of possible attribute names for user queries.
     */
    Set<String> getPossibleUserAttributeNames(IPersonAttributeDaoFilter filter);

    /**
     * Gets a {@link Set} of attribute names that this implementation knows how to use in a query. The names returned
     * represent all possible names for query attributes for this implmenentation. If the dao doesn't have a way to know
     * all possible query attribute names this method should return <code>null</code>
     * <br>
     * Returns an immutable {@link Set}.
     *
     * @param filter the filter
     * @return The set of attributes that can be used to query for user ids in this dao, null if the set is unknown.
     */
    Set<String> getAvailableQueryAttributes(IPersonAttributeDaoFilter filter);

    /**
     * Describes the order by which this DAO may be sorted
     * and put into an ordered collection.
     *
     * @return the numeric order.
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Gets the unique identifier for this dao.
     *
     * @return the id
     */
    default String[] getId() {
        return new String[]{this.getClass().getSimpleName()};
    }

    /**
     * Is this dao enabled?
     *
     * @return the boolean
     */
    default boolean isEnabled() {
        return true;
    }
}
