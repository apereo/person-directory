/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data access object which, for a given {@link Map} of query
 * data, returns a {@link Map} from attribute names to attribute
 * values.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public interface IPersonAttributeDao {
    
    /**
     * Searches for a single {@link IPerson} using the specified uid (userName).<br>
     * 
     * This method returns according to the following rules:<br>
     * <ul>
     *  <li>If the user exists and has attributes a populated {@link IPerson} is returned.</li>
     *  <li>If the user exists and has no attributes an empty {@link IPerson} is returned.</li>
     *  <li>If the user doesn't exist <code>null</code> is returned.</li>
     *  <li>If an error occurs while find the person an appropriate exception will be thrown.</li>
     * </ul>
     * 
     * @param uid The userName of the person to find.
     * @return The populated {@link IPerson} for the specified uid, null if no person could be found for the uid. 
     * @throws IllegalArgumentException If <code>uid</code> is <code>null.</code>
     */
    public IPerson getPerson(String uid);
    
    /**
     * Searches for {@link IPerson}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what a 'match' is on its own. The provided query Map contains String attribute
     * names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     * 
     * @param query A {@link Map} of name/value pair attributes to use in searching for {@link IPerson}s
     * @return A {@link Set} of {@link IPerson}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    public Set<IPerson> getPeople(Map<String, Object> query);
    
    /**
     * Searches for {@link IPerson}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what a 'match' is on its own. The provided query Map contains String attribute
     * names and multiple values any of which may be null or the List itself may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     * 
     * @param query A {@link Map} of name/value pair attributes to use in searching for {@link IPerson}s
     * @return A {@link Set} of {@link IPerson}s that match the query {@link Map}. If no matches are found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If <code>query</code> is <code>null.</code>
     */
    public Set<IPerson> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query);

    /**
     * Gets a {@link Set} of attribute names that may be returned by the {@link #getUserAttributes(Map)}. The names
     * returned represent all possible attributes names for the {@link IPerson} objects returned by the get methods. If
     * the dao doesn't have a way to know all possible attribute names this method should return <code>null</code>.
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
     * Returns a mutable {@link Map} of the attributes of the first {@link IPerson} returned by calling
     * {@link #getPeople(Map)}
     * 
     * @deprecated Use {@link #getPeople(Map)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed);

    /**
     * Returns a mutable {@link Map} of the attributes of the {@link IPerson} returned by calling
     * {@link #getPerson(String)}
     * 
     * @deprecated Use {@link #getPerson(String)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, List<Object>> getMultivaluedUserAttributes(final String uid);
    
    /**
     * Returns a mutable {@link Map} of the single-valued attributes of the first {@link IPerson} returned by calling
     * {@link #getPeople(Map)}
     * 
     * @deprecated Use {@link #getPeople(Map)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, Object> getUserAttributes(final Map<String, Object> seed);

    /**
     * Returns a mutable {@link Map} of the single-valued attributes of the {@link IPerson} returned by calling
     * {@link #getPerson(String)}
     * 
     * @deprecated Use {@link #getPerson(String)} instead. This method will be removed in 1.6
     */
    @Deprecated
    public Map<String, Object> getUserAttributes(final String uid);
}
