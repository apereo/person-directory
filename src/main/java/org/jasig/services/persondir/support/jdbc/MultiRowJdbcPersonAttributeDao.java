/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * An {@link org.jasig.services.persondir.IPersonAttributeDao}
 * implementation that maps attribute names and values from name and value column
 * pairs. This is usefull if user attributes are stored in a table like:<br>
 * <table border="1">
 *  <tr>
 *      <th>USER_NM</th>
 *      <th>ATTR_NM</th>
 *      <th>ATTR_VL</th>
 *  </tr>
 *  <tr>
 *      <td>jstudent</td>
 *      <td>name.given</td>
 *      <td>joe</td>
 *  </tr>
 *  <tr>
 *      <td>jstudent</td>
 *      <td>name.family</td>
 *      <td>student</td>
 *  </tr>
 *  <tr>
 *      <td>badvisor</td>
 *      <td>name.given</td>
 *      <td>bob</td>
 *  </tr>
 *  <tr>
 *      <td>badvisor</td>
 *      <td>name.family</td>
 *      <td>advisor</td>
 *  </tr>
 * </table>
 * 
 * <br>
 * 
 * This class expects 1 to N row results for a query, with each row containing 1 to N name
 * value attribute mappings. This contrasts {@link org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao}
 * which expects a single row result for a user query. <br>
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
 *         <td align="right" valign="top">attributeNameMappings</td>
 *         <td>
 *             Maps attribute names as defined in the database to attribute names to be exposed
 *             to the client code. The keys of the Map must be Strings, the values may be
 *             <code>null</code>, String or a Set of Strings. The keySet of this Map is returned
 *             as the possibleUserAttributeNames property. If an attribute name is not in
 *             the map the attribute name will be used in as the returned attribute name.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link java.util.Collections#EMPTY_MAP}</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">nameValueColumnMappings</td>
 *         <td>
 *             The {@link Map} of columns from a name column to value columns. Keys are Strings,
 *             Values are Strings or {@link java.util.List} of Strings 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class MultiRowJdbcPersonAttributeDao extends AbstractJdbcPersonAttributeDao {
    /**
     * {@link Map} from stored names to attribute names.
     * Keys are Strings, Values are null, Strings or List of Strings 
     */
    private Map<String, Set<String>> attributeNameMappings = Collections.emptyMap();
    
    /**
     * {@link Map} of columns from a name column to value columns.
     * Keys are Strings, Values are Strings or List of Strings 
     */
    private Map<String, Set<String>> nameValueColumnMappings = null;
    
    /**
     * {@link Set} of attributes that may be provided for a user.
     */
    private Set<String> userAttributes = Collections.emptySet();
    
    /**
     * Creates a new MultiRowJdbcPersonAttributeDao specifying the DataSource and SQL to use.
     * 
     * @param ds The DataSource to get connections from for executing queries, may not be null.
     * @param attrList Sets the query attribute list to pass to {@link AbstractJdbcPersonAttributeDao#AbstractJdbcPersonAttributeDao(DataSource, String)} and {@link AbstractJdbcPersonAttributeDao#setQueryAttributes(List)}
     * @param sql The SQL to execute for user attributes, may not be null.
     */
    public MultiRowJdbcPersonAttributeDao(DataSource ds, List<String> attrList, String sql) {
        super(ds, sql);

        this.setQueryAttributes(attrList);
    }
    

    /**
     * Returned {@link Map} will have values of {@link String} or a
     * {@link List} of {@link String}.
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    @Override
    protected Map<String, List<Object>> parseAttributeMapFromResults(List<Map<String, Object>> queryResults) {
        final Map<String, List<Object>> results = new HashMap<String, List<Object>>();
        
        //Iterate over each row in the result set
        for (final Map<String, Object> rowResult : queryResults) {

            //Iterate over each attribute column mapping to get the data from the row
            for (final Map.Entry<String, Set<String>> columnMapping : this.nameValueColumnMappings.entrySet()) {
                final String keyColumn = columnMapping.getKey();
                final Object attrNameObj = rowResult.get(keyColumn);
                if (attrNameObj == null && !rowResult.containsKey(keyColumn)) {
                    throw new BadSqlGrammarException("No column named '" + keyColumn + "' exists in result set", this.getSql(), null);
                }
                
                final String attrName = String.valueOf(attrNameObj);
                
                final Set<String> valueColumns = columnMapping.getValue();
                final List<Object> attrValues = new ArrayList<Object>(valueColumns.size());
                for (final String valueColumn : valueColumns) {
                    final Object attrValue = rowResult.get(valueColumn);
                    if (attrValue == null && !rowResult.containsKey(valueColumn)) {
                        throw new BadSqlGrammarException("No column named '" + valueColumn + "' exists in result set", this.getSql(), null);
                    }
                    
                    attrValues.add(attrValue);
                }

                final Set<String> mappedNames = this.attributeNameMappings.get(attrName);
                
                //If no portal user attribute names are mapped add the value(s) with the attribute name from the result set
                if (mappedNames == null) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Adding un-mapped attribute '" + attrName + "'");
                    }
                    
                    MultivaluedPersonAttributeUtils.addResult(results, attrName, attrValues);
                }
                else {
                    //For each mapped portlet attribute name store the value(s)
                    for (final String portalAttrName : mappedNames) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("Adding mapped attribute '" + portalAttrName + "' for source attribute '" + attrName + "'");
                        }

                        MultivaluedPersonAttributeUtils.addResult(results, portalAttrName, attrValues);
                    }
                }
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning attribute Map '" + results + "' from query results '" + queryResults + "'");
        }
        
        return results;
    }
    
    /* 
     * @see org.jasig.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.userAttributes;
    }

    /**
     * Get the Map from non-null String column names to Sets of non-null Strings
     * representing the names of the uPortal attributes to be initialized from
     * the specified column.
     * @return Returns the attributeMappings mapping.
     */
    public Map<String, Set<String>> getAttributeNameMappings() {
        return this.attributeNameMappings;
    }

    /**
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param attributeNameMap {@link Map} from column names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setAttributeNameMappings(final Map<String, Object> attributeNameMap) {
        Validate.notNull(attributeNameMap, "columnsToAttributesMap may not be null");
        
        this.attributeNameMappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(attributeNameMap);
        
        if (this.attributeNameMappings.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection<String> userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.attributeNameMappings.values()); 
        
        this.userAttributes = Collections.unmodifiableSet(new HashSet<String>(userAttributeCol));
    }


    /**
     * @return The Map of name column to value column(s). 
     */
    public Map<String, Set<String>> getNameValueColumnMappings() {
        return this.nameValueColumnMappings;
    }
    
    /**
     * The {@link Map} of columns from a name column to value columns. Keys are Strings,
     * Values are Strings or {@link java.util.List} of Strings.
     * 
     * @param nameValueColumnMap The Map of name column to value column(s). 
     */
    public void setNameValueColumnMappings(final Map<String, ? extends Object> nameValueColumnMap) {
        if (nameValueColumnMap == null) {
            this.nameValueColumnMappings = null;
        }
        else {
            final Map<String, Set<String>> mappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nameValueColumnMap);
            
            if (mappings.containsValue(null)) {
                throw new IllegalArgumentException("nameValueColumnMap may not have null values");
            }
            
            this.nameValueColumnMappings = mappings;
        }
    }
}