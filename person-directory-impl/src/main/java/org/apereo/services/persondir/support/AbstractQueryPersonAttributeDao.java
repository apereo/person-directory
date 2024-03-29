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
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.apereo.services.persondir.util.CollectionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides common functionality for DAOs using a set of attribute values from the seed to
 * perform a query. Ensures the necessary attributes to run the query exist on the seed and
 * organizes the values into an argument array.
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
 *         <td  valign="top">queryAttributeMapping</td>
 *         <td>
 *             A {@link Map} from attribute names used in the query {@link Map} to attribute names to use in the SQL.
 *             The values can be either {@link String} or {@link Collection} of String to use a single Map attribute under
 *             multiple names as in the SQL. If set only {@link Map} attributes listed will be used in the SQL. If not
 *             set all {@link Map} attributes are used as-is in the SQL.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td  valign="top">resultAttributeMapping</td>
 *         <td>
 *             A {@link Map} from SQL result names to returned attribute names. The values can be either {@link String}
 *             or {@link Collection} of String to use a single SQL result under multiple returned attributes. If set only
 *             SQL attributes listed will be returned. If not set all SQL attributes will be returned.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td  valign="top">requireAllQueryAttributes</td>
 *         <td>
 *             If the SQL should only be run if all attributes listed in the queryAttributeMapping exist in the query
 *             {@link Map}. Ignored if queryAttributeMapping is null
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 *     <tr>
 *         <td  valign="top">unmappedUsernameAttribute</td>
 *         <td>
 *             The unmapped username attribute returned by the query. If null the value returned by the configured
 *             {@link IUsernameAttributeProvider} is used.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 *
 * @author Eric Dalquist 

 */
public abstract class AbstractQueryPersonAttributeDao<QB> extends AbstractDefaultAttributePersonAttributeDao {
    // DEFAULT_CASE_CANONICALIZATION_MODE is default for canonicalizing the values
    public static final CaseCanonicalizationMode DEFAULT_CASE_CANONICALIZATION_MODE = CaseCanonicalizationMode.LOWER;
    public static final CaseCanonicalizationMode DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE = CaseCanonicalizationMode.NONE;
    private Map<String, Set<String>> queryAttributeMapping;
    private Map<String, Set<String>> resultAttributeMapping;
    private Map<String, CaseCanonicalizationMode> caseInsensitiveResultAttributes;
    private Map<String, CaseCanonicalizationMode> caseInsensitiveQueryAttributes;
    private CaseCanonicalizationMode defaultCaseCanonicalizationMode = DEFAULT_CASE_CANONICALIZATION_MODE;
    private CaseCanonicalizationMode usernameCaseCanonicalizationMode = DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
    private Locale caseCanonicalizationLocale = Locale.getDefault();
    private Set<String> possibleUserAttributes;
    private boolean requireAllQueryAttributes = false;
    private boolean useAllQueryAttributes = true;
    private String unmappedUsernameAttribute = null;


    public AbstractQueryPersonAttributeDao() {
        super();
    }

    public boolean isUseAllQueryAttributes() {
        return this.useAllQueryAttributes;
    }

    /**
     * If {@link #setQueryAttributeMapping(Map)} is null this determines if no parameters should be specified 
     * or if all query attributes should be used as parameters. Defaults to true.
     *
     * @param useAllQueryAttributes True to use all query attributes as parameters
     */
    public void setUseAllQueryAttributes(final boolean useAllQueryAttributes) {
        this.useAllQueryAttributes = useAllQueryAttributes;
    }


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
    public void setQueryAttributeMapping(final Map<String, ?> queryAttributeMapping) {
        var parsedQueryAttributeMapping =
                MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(queryAttributeMapping);

        if (parsedQueryAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }

        this.queryAttributeMapping = parsedQueryAttributeMapping;
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
    public void setResultAttributeMapping(final Map<String, ?> resultAttributeMapping) {
        var parsedResultAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(resultAttributeMapping);

        if (parsedResultAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }

        final Collection<String> userAttributes = MultivaluedPersonAttributeUtils.flattenCollection(parsedResultAttributeMapping.values());

        this.resultAttributeMapping = parsedResultAttributeMapping;
        this.possibleUserAttributes = new LinkedHashSet<>(userAttributes);
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
    public void setRequireAllQueryAttributes(final boolean requireAllQueryAttributes) {
        this.requireAllQueryAttributes = requireAllQueryAttributes;
    }

    /**
     * Returns the userNameAttribute
     * @return the userNameAttribute
     */
    public String getUnmappedUsernameAttribute() {
        return unmappedUsernameAttribute;
    }

    /**
     * The returned attribute to use as the userName for the mapped IPersons. If null the {@link #getUsernameAttributeProvider()}
     * value will be used and if that is null the {@link AttributeNamedPersonImpl#DEFAULT_USER_NAME_ATTRIBUTE} value is
     * used. 
     *
     * @param userNameAttribute the userNameAttribute to set
     */
    public void setUnmappedUsernameAttribute(final String userNameAttribute) {
        this.unmappedUsernameAttribute = userNameAttribute;
    }
    
    @Override
    public final Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                           final IPersonAttributeDaoFilter filter,
                                                                           final Set<IPersonAttributes> resultPeople) {
        Validate.notNull(query, "query may not be null.");

        //Generate the query to pass to the subclass
        var queryBuilder = this.generateQuery(query);
        if (queryBuilder == null && (this.queryAttributeMapping != null || this.useAllQueryAttributes == true)) {
            this.logger.debug("No queryBuilder was generated for query " + query + ", null will be returned");

            return null;
        }

        //Get the username from the query, if specified
        var usernameAttributeProvider = this.getUsernameAttributeProvider();
        var username = usernameAttributeProvider.getUsernameFromQuery(query);

        //Execute the query in the subclass
        var unmappedPeople = this.getPeopleForQuery(queryBuilder, username);
        if (unmappedPeople == null) {
            return null;
        }

        //Map the attributes of the found people according to resultAttributeMapping if it is set
        final Set<IPersonAttributes> mappedPeople = new LinkedHashSet<>();
        for (var unmappedPerson : unmappedPeople) {
            var mappedPerson = this.mapPersonAttributes(unmappedPerson);
            mappedPeople.add(mappedPerson);
        }

        return CollectionsUtil.safelyWrapAsUnmodifiableSet(mappedPeople);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        if (this.queryAttributeMapping == null) {
            return new HashSet<>();
        }

        return new HashSet<>(this.queryAttributeMapping.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return this.possibleUserAttributes;
    }


    /**
     * Executes the query for the generated queryBuilder object and returns a list where each entry is a Map of
     * attributes for a single IPersonAttributes.
     *
     * @param queryBuilder The query generated by calls to {@link #appendAttributeToQuery(Object, String, List)}
     * @param queryUserName The username passed in the query map, if no username attribute existed in the query Map null is provided.
     * @return The list of IPersons found by the query. The user attributes should be using the raw names from the data layer.
     */
    protected abstract List<IPersonAttributes> getPeopleForQuery(QB queryBuilder, String queryUserName);

    /**
     * Append the attribute and its canonicalized value/s to the
     * {@code queryBuilder}. Uses {@code queryAttribute} to determine whether or
     * not the value/s should be canonicalized. I.e. the behavior is controlled
     * by {@link #setCaseInsensitiveQueryAttributes(java.util.Map)}.
     *
     * <p>This method is only concerned with canonicalizing the query attribute
     * value. It is still up to the subclass to canonicalize the data-layer
     * attribute value prior to comparison, if necessary. For example, if
     * the data layer is a case-sensitive relational database and attributes
     * therein are stored in mixed case, but comparison should be
     * case-insensitive, the relational column reference would need to be
     * wrapped in a {@code lower()} or {@code upper()} function. (This, of
     * course, needs to be handled with care, since it can lead to table
     * scanning if the store does not support function-based indexes.) Such
     * data-layer canonicalization would be unnecessary if the data layer is
     * case-insensitive or stores values in the same canonicalized form as has
     * been configured for the app-layer attribute.
     * See {@link AbstractJdbcPersonAttributeDao#setCaseInsensitiveDataAttributes(java.util.Map)}</p>
     *
     * @param queryBuilder The sub-class specific query builder object
     * @param queryAttribute The full attribute name to append
     * @param dataAttribute The full attribute name to append
     * @param queryValues The values for the data attribute
     * @return An updated queryBuilder
     */
    protected QB appendCanonicalizedAttributeToQuery(final QB queryBuilder, final String queryAttribute, final String dataAttribute, final List<Object> queryValues) {
        // All logging messages were previously in generateQuery() and were
        // copy/pasted verbatim
        var canonicalizedQueryValues = this.canonicalizeAttribute(queryAttribute, queryValues, caseInsensitiveQueryAttributes);
        if (dataAttribute == null) {
            // preserved from historical versions which just pass queryValues through without any association to a dataAttribute,
            // and a slightly different log message
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding attribute '" + queryAttribute + "' with value '" + queryValues + "' to query builder '" + queryBuilder + "'");
            }
            return appendAttributeToQuery(queryBuilder, dataAttribute, canonicalizedQueryValues);
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Adding attribute '" + dataAttribute + "' with value '" + queryValues + "' to query builder '" + queryBuilder + "'");
        }
        return appendAttributeToQuery(queryBuilder, dataAttribute, canonicalizedQueryValues);
    }

    /**
     * Append the attribute and value to the queryBuilder.
     *
     * @param queryBuilder The sub-class specific query builder object
     * @param dataAttribute The full attribute name to append
     * @param queryValues The values for the data attribute
     * @return An updated queryBuilder
     */
    protected abstract QB appendAttributeToQuery(QB queryBuilder, String dataAttribute, List<Object> queryValues);

    /**
     * Generates a query using the queryBuilder object passed by the subclass. Attribute/Value pairs are added to the
     * queryBuilder by calling {@link #appendCanonicalizedAttributeToQuery(Object, String, String, java.util.List)} which calls
     * {@link #appendAttributeToQuery(Object, String, java.util.List)}. Attributes are only added if
     * there is an attributed mapped in the queryAttributeMapping.
     *
     * @param query The query Map to populate the queryBuilder with.
     * @return The fully populated query builder.
     */
    protected final QB generateQuery(final Map<String, List<Object>> query) {
        QB queryBuilder = null;

        if (this.queryAttributeMapping != null && !queryAttributeMapping.isEmpty()) {
            for (var queryAttrEntry : this.queryAttributeMapping.entrySet()) {
                var queryAttr = queryAttrEntry.getKey();
                var queryValues = query.get(queryAttr);
                if (queryValues != null) {
                    var dataAttributes = queryAttrEntry.getValue();
                    if (dataAttributes == null) {
                        queryBuilder = this.appendCanonicalizedAttributeToQuery(queryBuilder, queryAttr, null, queryValues);
                    } else {
                        for (var dataAttribute : dataAttributes) {
                            queryBuilder = this.appendCanonicalizedAttributeToQuery(queryBuilder, queryAttr, dataAttribute, queryValues);
                        }
                    }
                } else if (this.requireAllQueryAttributes) {
                    this.logger.debug("Query " + query + " does not contain all nessesary attributes as specified by queryAttributeMapping " + this.queryAttributeMapping + ", null will be returned for the queryBuilder");
                    return null;
                }
            }
        } else if (this.useAllQueryAttributes) {
            for (var queryAttrEntry : query.entrySet()) {
                var queryKey = queryAttrEntry.getKey();
                var queryValues = queryAttrEntry.getValue();

                queryBuilder = this.appendCanonicalizedAttributeToQuery(queryBuilder, queryKey, queryKey, queryValues);
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated query builder '" + queryBuilder + "' from query Map " + query + ".");
        }

        return queryBuilder;
    }

    /**
     * Uses resultAttributeMapping to return a copy of the IPersonAttributes with only the attributes specified in
     * resultAttributeMapping mapped to their result attribute names.
     *
     * @param person The IPersonAttributes to map attributes for
     * @return A copy of the IPersonAttributes with mapped attributes, the original IPersonAttributes if resultAttributeMapping is null.
     */
    protected final IPersonAttributes mapPersonAttributes(final IPersonAttributes person) {
        var personAttributes = person.getAttributes();

        final Map<String, List<Object>> mappedAttributes;
        //If no mapping just use the attributes as-is
        if (this.resultAttributeMapping == null) {
            if (caseInsensitiveResultAttributes != null && !(caseInsensitiveResultAttributes.isEmpty())) {
                mappedAttributes = new LinkedHashMap<>();
                for (var attribute : personAttributes.entrySet()) {
                    var attributeName = attribute.getKey();
                    mappedAttributes.put(attributeName, canonicalizeAttribute(attributeName, attribute.getValue(), caseInsensitiveResultAttributes));
                }
            } else {
                mappedAttributes = personAttributes;
            }
        }
        //Map the attribute names via the resultAttributeMapping
        else {
            mappedAttributes = new LinkedHashMap<>();

            for (var resultAttrEntry : this.resultAttributeMapping.entrySet()) {
                var dataKey = resultAttrEntry.getKey();
                var resultKeys = resultAttrEntry.getValue();

                //If dataKey has no mapped resultKeys just use the dataKey
                if (resultKeys == null) {
                    resultKeys = ImmutableSet.of(dataKey);
                }
                if (resultKeys.size() == 1 && resultKeys.stream().allMatch(s -> s.endsWith(";"))) {
                    var allKeys = personAttributes.keySet().stream().filter(name -> name.startsWith(dataKey + ";")).collect(Collectors.toList());
                    for (var resultKey : allKeys) {
                        var value = personAttributes.get(resultKey);
                        value = canonicalizeAttribute(resultKey, value, caseInsensitiveResultAttributes);
                        mappedAttributes.put(resultKey, value);
                    }
                } else if (personAttributes.containsKey(dataKey)) {

                    // Only map found data attributes.
                    // .  See https://issues.jasig.org/browse/PERSONDIR-89
                    // Currently respects CaseInsensitive*NamedPersonImpl because BasePersonImpl's constructor

                    //Add the value to the mapped attributes for each mapped key,
                    //possibly canonicalizing casing for each value
                    var value = personAttributes.get(dataKey);
                    for (var resultKey : resultKeys) {
                        value = canonicalizeAttribute(resultKey, value, caseInsensitiveResultAttributes);
                        if (resultKey == null) {
                            //TODO is this possible?
                            mappedAttributes.put(dataKey, value);
                        } else {
                            mappedAttributes.put(resultKey, value);
                        }
                    }
                }
            }
        }

        final IPersonAttributes newPerson;

        var name = person.getName();
        if (name != null) {
            newPerson = new NamedPersonImpl(usernameCaseCanonicalizationMode.canonicalize(name), mappedAttributes);
        } else {
            var userNameAttribute = this.getConfiguredUserNameAttribute();
            var tmpNewPerson = new AttributeNamedPersonImpl(userNameAttribute, mappedAttributes);
            newPerson = new NamedPersonImpl(usernameCaseCanonicalizationMode.canonicalize(tmpNewPerson.getName()), mappedAttributes);
        }

        return newPerson;
    }

    /**
     * Canonicalize the attribute values if they are present in the config map.
     * @param key attribute key
     * @param value list of attribute values
     * @param config map of attribute names to canonicalization key for the attribute
     * @return if configured to do so, returns a canonicalized list of values.
     */
    protected List<Object> canonicalizeAttribute(final String key, final List<Object> value, final Map<String, CaseCanonicalizationMode> config) {
        if (value == null || value.isEmpty() || config == null || !(config.containsKey(key))) {
            return value;
        }
        var canonicalizationMode = config.get(key);
        if (canonicalizationMode == null) {
            // Intentionally late binding of the default to
            // avoid unexpected behavior if you wait to assign
            // the default until after you've injected the list
            // of case-insensitive fields
            canonicalizationMode = defaultCaseCanonicalizationMode;
        }
        final List<Object> canonicalizedValues = new ArrayList<>(value.size());
        for (var origValue : value) {
            if (origValue instanceof String) {
                canonicalizedValues.add(canonicalizationMode.canonicalize((String) origValue, caseCanonicalizationLocale));
            } else {
                canonicalizedValues.add(origValue);
            }
        }
        return canonicalizedValues;
    }

    /**
     * Indicates which attribute found by the subclass should be taken as the 
     * 'username' attribute.  (E.g. 'uid' or 'sAMAccountName')  NOTE:  Any two 
     * instances if BasePersonImpl with the same username are considered 
     * equal.  Since {@link #getUsernameAttributeProvider()} should never return
     * null, this method should never return null either.
     *
     * @return The name of the attribute corresponding to the  user's username. 
     */
    @JsonIgnore
    protected String getConfiguredUserNameAttribute() {
        //If configured explicitly use it
        if (this.unmappedUsernameAttribute != null) {
            return this.unmappedUsernameAttribute;
        }

        var usernameAttributeProvider = this.getUsernameAttributeProvider();
        return usernameAttributeProvider.getUsernameAttribute();
    }

    /**
     * Indicates whether the value from {@link #getConfiguredUserNameAttribute()}
     * was configured explicitly.  A return value of <code>false</code> means 
     * that the value from {@link #getConfiguredUserNameAttribute()} is a 
     * default, and should not be used over a username passed in the query.
     *
     * @return <code>true</code> If the 'unmappedUsernameAttribute' property was 
     * set explicitly, otherwise <code>false</code>
     */
    @JsonIgnore
    protected boolean isUserNameAttributeConfigured() {
        return this.unmappedUsernameAttribute != null;
    }

    /**
     * @see #setCaseInsensitiveResultAttributes(java.util.Map)
     *
     * @return Map of attribute names and casing canonicalization modes
     */
    public Map<String, CaseCanonicalizationMode> getCaseInsensitiveResultAttributes() {
        return caseInsensitiveResultAttributes;
    }

    /**
     * Keys are app-layer attributes, values are the casing canonicalization
     * modes for each, as applied when mapping from data-layer to application
     * attributes. {@code null} values treated as
     * {@link #DEFAULT_CASE_CANONICALIZATION_MODE} unless that default mode is
     * overridden with
     * {@link #setDefaultCaseCanonicalizationMode(CaseCanonicalizationMode)}.
     *
     * <p>Most commonly used for canonicalizing attributes used as unique
     * identifiers, usually username. In that case it's a good idea to
     * set that configuration here as well as via
     * {@link #setUsernameCaseCanonicalizationMode(CaseCanonicalizationMode)}
     * to ensure you don't end up with different results from
     * {@link IPersonAttributes#getName()} and
     * {@link IPersonAttributes#getAttributeValue(String)}.</p>
     *
     * <p>This config is separate from {@link #setCaseInsensitiveQueryAttributes(java.util.Map)}
     * because you may want to support case-insensitive searching on data
     * layer attributes, but respond with attribute values which preserve
     * the original data layer casing.</p>
     *
     * @param caseInsensitiveResultAttributes Map of result attribute names and casing canonicalization modes
     */
    public void setCaseInsensitiveResultAttributes(final Map<String, CaseCanonicalizationMode> caseInsensitiveResultAttributes) {
        this.caseInsensitiveResultAttributes = caseInsensitiveResultAttributes;
    }

    /**
     * Configuration convenience same as passing the given {@code Set} as the
     * keys in the {@link #setCaseInsensitiveResultAttributes(java.util.Map)}
     * {@code Map} and implicitly accepting the default canonicalization mode
     * for each. Note that this setter will not assign canonicalization modes,
     * meaning that you needn't ensure
     * {@link #setDefaultCaseCanonicalizationMode(CaseCanonicalizationMode)}
     * has already been called.
     *
     * @param caseInsensitiveResultAttributes Set of result attribute names
     */
    public void setCaseInsensitiveResultAttributesAsCollection(final Collection<String> caseInsensitiveResultAttributes) {
        if (caseInsensitiveResultAttributes == null || caseInsensitiveResultAttributes.isEmpty()) {
            setCaseInsensitiveResultAttributes(null);
        } else {
            final Map<String, CaseCanonicalizationMode> asMap = new HashMap<>();
            for (var attrib : caseInsensitiveResultAttributes) {
                asMap.put(attrib, null);
            }
            setCaseInsensitiveResultAttributes(asMap);
        }
    }

    /**
     * Configuration convenience same as passing the given {@code Set} as the
     * keys in the {@link #setCaseInsensitiveQueryAttributes(java.util.Map)}
     * {@code Map} and implicitly accepting the default canonicalization mode
     * for each. Note that this setter will not assign canonicalization modes,
     * meaning that you needn't ensure
     * {@link #setDefaultCaseCanonicalizationMode(CaseCanonicalizationMode)}
     * has already been called.
     *
     * @param caseInsensitiveQueryAttributes Set of query attribute names
     */
    public void setCaseInsensitiveQueryAttributesAsCollection(final Collection<String> caseInsensitiveQueryAttributes) {
        if (caseInsensitiveQueryAttributes == null || caseInsensitiveQueryAttributes.isEmpty()) {
            setCaseInsensitiveQueryAttributes(null);
        } else {
            final Map<String, CaseCanonicalizationMode> asMap = new HashMap<>();
            for (var attrib : caseInsensitiveQueryAttributes) {
                asMap.put(attrib, null);
            }
            setCaseInsensitiveQueryAttributes(asMap);
        }
    }

    /**
     * Keys are app-layer attributes, values are the casing canonicalization
     * modes for each, as applied when mapping from an application layer
     * query to a data layer query. {@code null} values treated as
     * {@link #DEFAULT_CASE_CANONICALIZATION_MODE} unless that default mode is
     * overridden with
     * {@link #setDefaultCaseCanonicalizationMode(CaseCanonicalizationMode)}.
     *
     * <p>Use this for any attribute for which you'd like to support
     * case-insensitive search and where the underlying data layer is
     * case-sensitive. Of course, if the data layer does not store these
     * attributes in the canonical casing, the data layer itself would also need
     * to be canonicalized in a subclass-specific fashion. For example, see
     * {@link AbstractJdbcPersonAttributeDao#setCaseInsensitiveDataAttributes(java.util.Map)}.</p>
     *
     * <p>This config is separate from {@link #setCaseInsensitiveResultAttributes(java.util.Map)}
     * because you may want to support case-insensitive searching on data
     * layer attributes, but respond with attribute values which preserve
     * the original data layer casing.</p>
     *
     * @param caseInsensitiveQueryAttributes Map of query attribute names and casing canonicalization modes
     */
    public void setCaseInsensitiveQueryAttributes(final Map<String, CaseCanonicalizationMode> caseInsensitiveQueryAttributes) {
        this.caseInsensitiveQueryAttributes = caseInsensitiveQueryAttributes;
    }

    /**
     * @see #setCaseInsensitiveQueryAttributes(java.util.Map)
     * @return Map of query attribute names and casing canonicalization modes
     */
    public Map<String, CaseCanonicalizationMode> getCaseInsensitiveQueryAttributes() {
        return caseInsensitiveQueryAttributes;
    }

    /**
     * Assign the {@link Locale} in which all casing canonicalizations will occur.
     * A {@code null} will be treated as {@link java.util.Locale#getDefault()}.
     *
     * @param caseCanonicalizationLocale Case-canonicalization locale
     */
    public void setCaseCanonicalizationLocale(final Locale caseCanonicalizationLocale) {
        if (caseCanonicalizationLocale == null) {
            this.caseCanonicalizationLocale = Locale.getDefault();
        } else {
            this.caseCanonicalizationLocale = caseCanonicalizationLocale;
        }
    }

    /**
     * Returns the {@link Locale} in which all casing canonicalizations will occur.
     * @return Case-canonicalization locale
     */
    public Locale getCaseCanonicalizationLocale() {
        return caseCanonicalizationLocale;
    }

    /**
     * Override the default {@link CaseCanonicalizationMode}
     * ({@link #DEFAULT_CASE_CANONICALIZATION_MODE}). Cannot be unset. A
     * null will have the same effect as reverting to the default.
     *
     * @param defaultCaseCanonicalizationMode Set the default CaseCanonicalizationMode
     */
    public void setDefaultCaseCanonicalizationMode(final CaseCanonicalizationMode defaultCaseCanonicalizationMode) {
        if (defaultCaseCanonicalizationMode == null) {
            this.defaultCaseCanonicalizationMode = DEFAULT_CASE_CANONICALIZATION_MODE;
        } else {
            this.defaultCaseCanonicalizationMode = defaultCaseCanonicalizationMode;
        }
    }

    /**
     * Returns the default {@link CaseCanonicalizationMode}.
     * @return Default CaseCanonicalizationMode
     */
    public CaseCanonicalizationMode getDefaultCaseCanonicalizationMode() {
        return defaultCaseCanonicalizationMode;
    }

    /**
     * Username canonicalization is a special case because
     * {@link #mapPersonAttributes(IPersonAttributes)}
     * doesn't know where it came from. It might have come from a data layer
     * attribute. Or it might have been derived from the original query itself.
     * There is no general way to query the {@link IPersonAttributes} to
     * determine exactly what happened.
     *
     * <p>If you need to guarantee case-insensitive usernames
     * <em>in {@link IPersonAttributes#getName()}</em>,
     * set this property to the same mode that you set for your app-layer
     * username attribute(s) via
     * {@link #setCaseInsensitiveResultAttributes(java.util.Map)}.</p>
     *
     * <p>Otherwise, leave this property alone and no attempt will be made
     * to canonicalize the value returned from
     * {@link IPersonAttributes#getName()}</p>
     *
     * <p>That default behavior is consistent with the legacy behavior of
     * being case sensitive w/r/t usernames.</p>
     *
     * @param usernameCaseCanonicalizationMode {@link CaseCanonicalizationMode} to use for the username
     */
    public void setUsernameCaseCanonicalizationMode(final CaseCanonicalizationMode usernameCaseCanonicalizationMode) {
        if (usernameCaseCanonicalizationMode == null) {
            this.usernameCaseCanonicalizationMode = DEFAULT_USERNAME_CASE_CANONICALIZATION_MODE;
        } else {
            this.usernameCaseCanonicalizationMode = usernameCaseCanonicalizationMode;
        }
    }

    /**
     * @see #setUsernameCaseCanonicalizationMode(CaseCanonicalizationMode)
     * @return The {@link CaseCanonicalizationMode} to use for the username
     */
    public CaseCanonicalizationMode getUsernameCaseCanonicalizationMode() {
        return this.usernameCaseCanonicalizationMode;
    }

}
