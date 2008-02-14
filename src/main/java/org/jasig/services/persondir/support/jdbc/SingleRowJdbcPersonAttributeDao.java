/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * An {@link org.jasig.services.persondir.IPersonAttributeDao}
 * implementation that maps from column names in the result of a SQL query
 * to attribute names. <br>
 * You must set a Map from column names to attribute names and only column names
 * appearing as keys in that map will be used.
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
 *         <td align="right" valign="top">columnsToAttributes</td>
 *         <td>
 *             The {@link Map} of {@link String} columns names to {@link String} or {@link Set}s
 *             of {@link String}s to use as attribute names in the returned Map. If a column name
 *             is not in the map the column name will be used in as the returned attribute name.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link java.util.Collections#EMPTY_MAP}</td>
 *     </tr>
 * </table>
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class SingleRowJdbcPersonAttributeDao extends AbstractJdbcPersonAttributeDao {
    /**
     * {@link Map} from column names to attribute names.
     */
    private Map<String, Set<String>> attributeMappings = Collections.emptyMap();
    
    /**
     * {@link Set} of attributes that may be provided for a user.
     */
    private Set<String> userAttributes = Collections.emptySet();
    
        

    /**
     * Creates a new MultiRowJdbcPersonAttributeDao specifying the DataSource and SQL to use.
     * 
     * @param ds The DataSource to get connections from for executing queries, may not be null.
     * @param attrList Sets the query attribute list
     * @param sql The SQL to execute for user attributes, may not be null.
     */
    public SingleRowJdbcPersonAttributeDao(DataSource ds, List<String> attrList, String sql) {
        super(ds, sql);
        this.setQueryAttributes(attrList);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao#parseAttributeMapFromResults(java.util.List)
     */
    @Override
    protected Map<String, List<Object>> parseAttributeMapFromResults(List<Map<String, Object>> queryResults) {
        if (queryResults.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, queryResults.size());
        }
        
        final Map<String, List<Object>> attributeMap = new HashMap<String, List<Object>>();
        
        //Even though there should only be 0 or 1 items in the List a for is the easiest way to use that 1 if it is there
        for (final Map<String, Object> result : queryResults) {
            for (final String columnName : this.attributeMappings.keySet()) {
                this.addMappedAttributes(result, columnName, attributeMap);
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning attribute Map '" + attributeMap + "' from query results '" + queryResults + "'");
        }

        return attributeMap;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.userAttributes;
    }
    
    /**
     * Tries to get the attributes specified for the column, determin the
     * mapping for the column and add it to the rowResults {@link Map}.
     * 
     * @param rs The {@link Map} of results to get the attribute value from.
     * @param columnName The name of the column to get the attribute value from.
     * @param rowResults The {@link Map} to add the mapped attribute to.
     */
    protected void addMappedAttributes(final Map<String, Object> rs, final String columnName, final Map<String, List<Object>> rowResults) {
        Validate.notEmpty(columnName, "columnName cannot be null and must have length >= 0");
        
        final Object attributeValue = rs.get(columnName);
        if (attributeValue == null && !rs.containsKey(columnName)) {
            throw new BadSqlGrammarException("No column named '" + columnName + "' exists in result set", this.getSql(), null);
        }
        
        //See if the column is mapped
        Set<String> attributeNames = this.attributeMappings.get(columnName);
        
        //No mapping was found, just use the column name
        if (attributeNames == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No mapped attribute name for column='" + columnName + "', defaulting to the column name.");
            }
            
            attributeNames = Collections.singleton(columnName);
        }
        
        //Run through the mapped attribute names
        for (final String attributeName : attributeNames) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding mapped attribute '" + attributeName + "' for source column '" + columnName + "'");
            }

            MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
        }
    }


    /**
     * Get the Map from non-null String column names to Sets of non-null Strings
     * representing the names of the uPortal attributes to be initialized from
     * the specified column.
     * @return Returns the attributeMappings mapping.
     */
    public Map<String, Set<String>> getColumnsToAttributes() {
        return this.attributeMappings;
    }

    /**
     * Set the {@link Map} to use for mapping from a column name to a attribute
     * name or {@link Set} of attribute names. Column names that are specified
     * but have null mappings will use the column name for the attribute name.
     * Column names that are not specified as keys in this {@link Map} will be
     * ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param columnsToAttributesMap {@link Map} from column names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setColumnsToAttributes(final Map<String, Object> columnsToAttributesMap) {
        if (columnsToAttributesMap == null) {
            throw new IllegalArgumentException("columnsToAttributesMap may not be null");
        }
        
        this.attributeMappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(columnsToAttributesMap);
        
        if (this.attributeMappings.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection<String> userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.attributeMappings.values()); 
        
        this.userAttributes = Collections.unmodifiableSet(new HashSet<String>(userAttributeCol));
    }
}