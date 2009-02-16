/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Defines methods for finding a {@link IPersonAttributes} or Set of IPersons based on a user ID or a Map of user attributes to
 * query with.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public interface IPersonAttributeDao {
    public static final String WILDCARD = "*";
    public static final Pattern WILDCARD_PATTERN = Pattern.compile(Pattern.quote(IPersonAttributeDao.WILDCARD));
    
    /**
     * Searches for a single {@link IPersonAttributes} using the specified uid (userName).<br>
     * 
     * This method returns according to the following rules:<br>
     * <ul>
     *  <li>If the user exists and has attributes a populated {@link IPersonAttributes} is returned.</li>
     *  <li>If the user exists and has no attributes an {@link IPersonAttributes} with an empty attributes Map is returned.</li>
     *  <li>If the user doesn't exist <code>null</code> is returned.</li>
     *  <li>If an error occurs while find the person an appropriate exception will be thrown.</li>
     * </ul>
     * 
     * @param uid The userName of the person to find.
     * @return The populated {@link IPersonAttributes} for the specified uid, null if no person could be found for the uid. 
     * @throws IllegalArgumentException If <code>uid</code> is <code>null.</code>
     */
    public IPersonAttributes getPerson(String uid);
    
    /**
     * Searches for {@link IPersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     * 
     * @param query A {@link Map} of name/value pair attributes to use in searching for {@link IPersonAttributes}s
     * @return A {@link Set} of {@link IPersonAttributes}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    public Set<IPersonAttributes> getPeople(Map<String, Object> query);
    
    /**
     * Searches for {@link IPersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     * 
     * @param query A {@link Map} of name/value pair attributes to use in searching for {@link IPersonAttributes}s
     * @return A {@link Set} of {@link IPersonAttributes}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query);

    /**
     * Gets a {@link Set} of attribute names that may be returned for an IPersonAttributes. The names returned represent all
     * possible attributes names for the {@link IPersonAttributes} objects returned by the get methods. If the dao doesn't have a
     * way to know all possible attribute names this method should return <code>null</code>.
     * <br>
     * Returns an immutable {@link Set}.
     * 
     * @return A {@link Set} of possible attribute names for user queries.
     */
    public Set<String> getPossibleUserAttributeNames();

    /**
     * Gets a {@link Set} of attribute names that this implementation knows how to use in a query. The names returned 
     * represent all possible names for query attributes for this implmenentation. If the dao doesn't have a way to know
     * all possible query attribute names this method should return <code>null</code>
     * <br>
     * Returns an immutable {@link Set}.
     * 
     * @return The set of attributes that can be used to query for user ids in this dao, null if the set is unknown.
     */
    public Set<String> getAvailableQueryAttributes();
    
    

    
    
    /**
     * Returns a mutable {@link Map} of the attributes of the first {@link IPersonAttributes} returned by calling
     * {@link #getPeople(Map)}
     * 
     * @deprecated Use {@link #getPeople(Map)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed);

    /**
     * Returns a mutable {@link Map} of the attributes of the {@link IPersonAttributes} returned by calling
     * {@link #getPerson(String)}
     * 
     * @deprecated Use {@link #getPerson(String)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, List<Object>> getMultivaluedUserAttributes(final String uid);
    
    /**
     * Returns a mutable {@link Map} of the single-valued attributes of the first {@link IPersonAttributes} returned by calling
     * {@link #getPeople(Map)}
     * 
     * @deprecated Use {@link #getPeople(Map)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, Object> getUserAttributes(final Map<String, Object> seed);

    /**
     * Returns a mutable {@link Map} of the single-valued attributes of the {@link IPersonAttributes} returned by calling
     * {@link #getPerson(String)}
     * 
     * @deprecated Use {@link #getPerson(String)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, Object> getUserAttributes(final String uid);
}
