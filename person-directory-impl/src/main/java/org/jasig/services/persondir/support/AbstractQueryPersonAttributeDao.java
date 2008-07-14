/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPerson;

/**
 * Provides common functionality for DAOs using a set of attribute values from the seed to
 * perform a query. Ensures the nessesary attributes to run the query exist on the seed and
 * organizes the values into an argument array.
 * 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">queryAttributeMapping</td>
 *         <td>
 *             A {@link Map} from attribute names used in the query {@link Map} to attribute names to use in the SQL.
 *             The values can be either {@link String} or {@link Collection<String>} to use a single Map attribute under
 *             multiple names as in the SQL. If set only {@link Map} attributes listed will be used in the SQL. If not
 *             set all {@link Map} attributes are used as-is in the SQL.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">resultAttributeMapping</td>
 *         <td>
 *             A {@link Map} from SQL result names to returned attribute names. The values can be either {@link String} 
 *             or {@link Collection<String>} to use a single SQL result under multiple returned attributes. If set only
 *             SQL attributes listed will be returned. If not set all SQL attributes will be returned.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">requireAllQueryAttributes</td>
 *         <td>
 *             If the SQL should only be run if all attributes listed in the queryAttributeMapping exist in the query
 *             {@link Map}. Ignored if queryAttributeMapping is null
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">userNameAttribute</td>
 *         <td>
 *             The name of the returned attribute to use as the username for the resulting IPersons. If null the value
 *             of defaultAttribute is used.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist 
 * @version $Revision$
 */
public abstract class AbstractQueryPersonAttributeDao<QB> extends AbstractDefaultAttributePersonAttributeDao {
    private Map<String, Set<String>> queryAttributeMapping;
    private Map<String, Set<String>> resultAttributeMapping;
    private Set<String> possibleUserAttributes;
    private boolean requireAllQueryAttributes = false;
    private String userNameAttribute = null;
    

    /**
     * @return the queryAttributeMapping
     */
    public Map<String, Set<String>> getQueryAttributeMapping() {
        return queryAttributeMapping;
    }
    /**
     * Map from query attribute names to data-layer attribute names to use when building the query. If an ordered Map is
     * passed in the order of the attributes will be honored when building the query.
     *  
     * If not set query attributes will be used directly from the query Map.
     * 
     * @param queryAttributeMapping the queryAttributeMapping to set
     */
    public void setQueryAttributeMapping(Map<String, ?> queryAttributeMapping) {
        this.queryAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(queryAttributeMapping);
        
        if (this.queryAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
    }

    /**
     * @return the resultAttributeMapping
     */
    public Map<String, Set<String>> getResultAttributeMapping() {
        return resultAttributeMapping;
    }
    /**
     * Set the {@link Map} to use for mapping from a data layer name to an attribute name or {@link Set} of attribute
     * names. Data layer names that are specified but have null mappings will use the column name for the attribute
     * name. Data layer names that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set} 
     * of {@link String}.
     * 
     * @param resultAttributeMapping {@link Map} from column names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setResultAttributeMapping(Map<String, ?> resultAttributeMapping) {
        this.resultAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(resultAttributeMapping);
        
        if (this.resultAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection<String> userAttributes = MultivaluedPersonAttributeUtils.flattenCollection(this.resultAttributeMapping.values()); 
        this.possibleUserAttributes = Collections.unmodifiableSet(new LinkedHashSet<String>(userAttributes));
    }
    
    /**
     * @return the requireAllQueryAttributes
     */
    public boolean isRequireAllQueryAttributes() {
        return requireAllQueryAttributes;
    }
    /**
     * If all attributes specified in the queryAttributeMapping keySet must be present to actually run the query
     * 
     * @param requireAllQueryAttributes the requireAllQueryAttributes to set
     */
    public void setRequireAllQueryAttributes(boolean requireAllQueryAttributes) {
        this.requireAllQueryAttributes = requireAllQueryAttributes;
    }
    
    /**
     * @return the userNameAttribute
     */
    public String getUserNameAttribute() {
        return userNameAttribute;
    }
    /**
     * The returned attribute to use as the userName for the mapped IPersons. If null the {@link #setDefaultAttributeName(String)}
     * value will be used and if that is null the {@link AttributeNamedPersonImpl#DEFAULT_USER_NAME_ATTRIBUTE} value is
     * used. 
     * 
     * @param userNameAttribute the userNameAttribute to set
     */
    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public final Set<IPerson> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        Validate.notNull(query, "query may not be null.");
        
        //Generate the query to pass to the subclass
        final QB queryBuilder = this.generateQuery(query);
        if (queryBuilder == null) {
            this.logger.info("No queryBuilder was generated for query " + query + ", null will be returned");
            
            return null;
        }
        
        //Execute the query in the subclass
        final List<IPerson> unmappedPeople = this.getPeopleForQuery(queryBuilder);
        if (unmappedPeople == null) {
            return null;
        }

        //Map the attributes of the found people according to resultAttributeMapping if it is set
        final Set<IPerson> mappedPeople = new LinkedHashSet<IPerson>();
        for (final IPerson unmappedPerson : unmappedPeople) {
            final IPerson mappedPerson = this.mapPersonAttributes(unmappedPerson);
            mappedPeople.add(mappedPerson);
        }
        
        return Collections.unmodifiableSet(mappedPeople);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    public Set<String> getAvailableQueryAttributes() {
        return Collections.unmodifiableSet(this.queryAttributeMapping.keySet());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.possibleUserAttributes;
    }

    
    /**
     * Executes the query for the generated queryBuilder object and returns a list where each entry is a Map of
     * attributes for a single IPerson.
     * 
     * @param queryBuilder The query generated by calls to {@link #appendAttributeToQuery(Object, String, List)}
     * @return The list of IPersons found by the query. The user attributes should be using the raw names from the data layer.
     */
    protected abstract List<IPerson> getPeopleForQuery(QB queryBuilder);

    /**
     * Append the attribute and value to the queryBuilder.
     * 
     * @param queryBuilder The sub-class specific query builder object
     * @param dataAttribute The full attribute name to append
     * @param queryValues The values for the data attribute
     * @return An updated queryBuiler
     */
    protected abstract QB appendAttributeToQuery(QB queryBuilder, String dataAttribute, List<Object> queryValues);
    
    /**
     * Generates a query using the queryBuilder object passed by the subclass. Attribute/Value pairs are added to the
     * queryBuilder by calling {@link #appendAttributeToQuery(Object, String, String)}. Attributes are only added if
     * the value is not blank {@link StringUtils#isBlank(String)} and there is an attributed mapped in the queryAttributeMapping.
     * 
     * @param queryBuilder The sub-class specific object to pass to {@link #appendAttributeToQuery(Object, String, String)} when building the query
     * @param query The query Map to populate the queryBuilder with.
     * @return The fully populated query builder.
     */
    protected final QB generateQuery(Map<String, List<Object>> query) {
        QB queryBuilder = null;

        if (this.queryAttributeMapping != null) {
            for (final Map.Entry<String, Set<String>> queryAttrEntry : this.queryAttributeMapping.entrySet()) {
                final String queryAttr = queryAttrEntry.getKey();
                final List<Object> queryValues = query.get(queryAttr);
                if (queryValues != null ) {
                    final Set<String> dataAttributes = queryAttrEntry.getValue();
                    if (dataAttributes == null) {
                        if (this.logger.isInfoEnabled()) {
                            this.logger.info("Adding attribute '" + queryAttr + "' with value '" + queryValues + "' to query builder '" + queryBuilder + "'");
                        }
                        
                        queryBuilder = this.appendAttributeToQuery(queryBuilder, queryAttr, queryValues); 
                    }
                    else {
                        for (final String dataAttribute : dataAttributes) {
                            if (this.logger.isInfoEnabled()) {
                                this.logger.info("Adding attribute '" + dataAttribute + "' with value '" + queryValues + "' to query builder '" + queryBuilder + "'");
                            }
                            
                            queryBuilder = this.appendAttributeToQuery(queryBuilder, dataAttribute, queryValues); 
                        }
                    }
                }
                else if (this.requireAllQueryAttributes) {
                    this.logger.info("Query " + query + " does not contain all nessesary attributes as specified by queryAttributeMapping " + this.queryAttributeMapping + ", null will be returned for the queryBuilder");
                    return null;
                }
            }
        }
        else {
            for (final Map.Entry<String, List<Object>> queryAttrEntry : query.entrySet()) {
                final String queryKey = queryAttrEntry.getKey();
                final List<Object> queryValues = queryAttrEntry.getValue();
                
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Adding attribute '" + queryKey + "' with value '" + queryValues + "' to query builder '" + queryBuilder + "'");
                }
                
                queryBuilder = this.appendAttributeToQuery(queryBuilder, queryKey, queryValues); 
            }
        }
        
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Generated query builder '" + queryBuilder + "' from query Map " + query + ".");
        }
        
        return queryBuilder;
    }
    
    /**
     * Uses resultAttributeMapping to return a copy of the IPerson with only the attributes specified in
     * resultAttributeMapping mapped to their result attribute names.
     * 
     * @param person The IPerson to map attributes for
     * @return A copy of the IPerson with mapped attributes, the original IPerson if resultAttributeMapping is null.
     */
    protected final IPerson mapPersonAttributes(final IPerson person) {
        final Map<String, List<Object>> personAttributes = person.getAttributes();
        
        final Map<String, List<Object>> mappedAttributes;
        //If no mapping just use the attributes as-is
        if (this.resultAttributeMapping == null) {
            mappedAttributes = personAttributes;
        }
        //Map the attribute names via the resultAttributeMapping
        else {
            mappedAttributes = new LinkedHashMap<String, List<Object>>(); 
            
            for (final Map.Entry<String, Set<String>> resultAttrEntry : this.resultAttributeMapping.entrySet()) {
                final String dataKey = resultAttrEntry.getKey();
                
                //Only map found data attributes
                if (personAttributes.containsKey(dataKey)) {
                    Set<String> resultKeys = resultAttrEntry.getValue();
                    
                    //If dataKey has no mapped resultKeys just use the dataKey
                    if (resultKeys == null) {
                        resultKeys = Collections.singleton(dataKey);
                    }
                    
                    //Add the value to the mapped attributes for each mapped key
                    final List<Object> value = personAttributes.get(dataKey);
                    for (final String resultKey : resultKeys) {
                        if (resultKey == null) {
                            //TODO is this possible?
                            mappedAttributes.put(dataKey, value);
                        }
                        else {
                            mappedAttributes.put(resultKey, value);
                        }
                    }
                }
            }
        }
        
        final IPerson newPerson;
        
        final String name = person.getName();
        if (name != null) {
            newPerson = new NamedPersonImpl(name, mappedAttributes);
        }
        else {
            final String userNameAttribute = this.getConfiguredUserNameAttribute();
            newPerson = new AttributeNamedPersonImpl(userNameAttribute, mappedAttributes);
        }
        
        return newPerson;
    }
    
    /**
     * @return The appropriate attribute to user for the user name. Since {@link #getDefaultAttributeName()} should
     * never return null this method should never return null either.
     */
    protected String getConfiguredUserNameAttribute() {
        //If configured explicitly use it
        if (this.userNameAttribute != null) {
            return this.userNameAttribute;
        }
        
        return this.getDefaultAttributeName();
    }
}
