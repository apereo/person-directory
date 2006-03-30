/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * An {@link org.jasig.portal.services.persondir.IPersonAttributeDao}
 * implementation that maps attribute names and values from name and value column
 * pairs. <br>
 * 
 * This class expects 1-N row results for a query, with each row containing 1-N name
 * value attribute mappings. This contrasts {@link org.jasig.portal.services.persondir.support.JdbcPersonAttributeDaoImpl}
 * which expects a single row result for a user query. <br>
 * 
 *<br>
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
 *             as  
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">{@link java.util.Collections#EMPTY_MAP}</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">userInfoCache</td>
 *         <td>
 *             The {@link java.util.Map} to use for result caching. This class does no cache
 *             maintenence. It is assumed the underlying Map implementation will ensure the cache
 *             is in a good state at all times.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cacheKeyAttributes</td>
 *         <td>
 *             A Set of attribute names to use when building the cache key. The default
 *             implementation generates the key as a Map of attributeNames to values retrieved
 *             from the seed for the query. Zero length sets are treaded as null.
 *         </td>
 *         <td valign="top">No</td>
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
    private Map attributeNameMappings = Collections.EMPTY_MAP;
    
    /**
     * {@link Map} of columns from a name column to value columns.
     * Keys are Strings, Values are Strings or Lost of Strings 
     */
    private Map nameValueColumnMappings = Collections.EMPTY_MAP;
    
    /**
     * {@link Set} of attributes that may be provided for a user.
     */
    private Set userAttributes = Collections.EMPTY_SET;
    
    /**
     * The {@link MappingSqlQuery} to use to get attributes.
     */
    private PersonAttributeMappingQuery query;


    /**
     * @see AbstractJdbcPersonAttributeDao#AbstractJdbcPersonAttributeDao(DataSource, List, String)
     */
    public MultiRowJdbcPersonAttributeDao(DataSource ds, List attrList, String sql) {
        super(ds, attrList, sql);
        this.query = new PersonAttributeMappingQuery(ds, sql);
    }


    /**
     * Returned {@link Map} will have values of {@link String} or a
     * {@link List} of {@link String}.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map parseAttributeMapFromResults(final List queryResults) {
        final Map results = new HashMap();

        for (final Iterator rowItr = queryResults.iterator(); rowItr.hasNext();) {
            final Map rowResult = (Map)rowItr.next();
            
            for (final Iterator resultItr = rowResult.entrySet().iterator(); resultItr.hasNext();) {
                final Map.Entry entry = (Map.Entry)resultItr.next();
                final String srcAttrName = (String)entry.getKey();
                
                final Set upAttrNames = (Set)this.attributeNameMappings.get(srcAttrName);
                
                //TODO restriction on null passthrough
                if (upAttrNames == null) {
                    MultivaluedPersonAttributeUtils.addResult(results, srcAttrName, entry.getValue());
                }
                else {
                    for (final Iterator upAttrNameItr = upAttrNames.iterator(); upAttrNameItr.hasNext();) {
                        final String upAttrName = (String)upAttrNameItr.next();
                        MultivaluedPersonAttributeUtils.addResult(results, upAttrName, entry.getValue());
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractJdbcPersonAttributeDao#getAttributeQuery()
     */
    protected AbstractPersonAttributeMappingQuery getAttributeQuery() {
        return this.query;
    }
    
    /* 
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.userAttributes;
    }

    /**
     * Get the Map from non-null String column names to Sets of non-null Strings
     * representing the names of the uPortal attributes to be initialized from
     * the specified column.
     * @return Returns the attributeMappings mapping.
     */
    public Map getAttributeNameMappings() {
        return this.attributeNameMappings;
    }

    /**
     * TODO
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param attributeNameMap {@link Map} from column names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setAttributeNameMappings(final Map attributeNameMap) {
        if (attributeNameMap == null) {
            throw new IllegalArgumentException("columnsToAttributesMap may not be null");
        }
        
        this.attributeNameMappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(attributeNameMap);
        
        if (this.attributeNameMappings.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.attributeNameMappings.values()); 
        
        this.userAttributes = Collections.unmodifiableSet(new HashSet(userAttributeCol));
    }


    public Map getNameValueColumnMappings() {
        return this.nameValueColumnMappings;
    }
    
    public void setNameValueColumnMappings(final Map nameValueColumnMap) {
        if (nameValueColumnMap == null) {
            throw new IllegalArgumentException("nameValueColumnMap may not be null");
        }
        
        final Map mappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nameValueColumnMap);
        
        if (mappings.containsValue(null)) {
            throw new IllegalArgumentException("nameValueColumnMap may not have null values");
        }
        
        this.nameValueColumnMappings = mappings;
    }
    
    
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("MultiRowJdbcPersonAttributeDao ");
    	sb.append("query=").append(this.query);
    	sb.append(" attributeMappings=").append(this.attributeNameMappings);
    	return sb.toString();
    }

    /**
     * An object which will execute a SQL query with the expectation
     * of yielding a ResultSet with zero or one rows, which it maps
     * to null or to a Map from uPortal attribute names to values.
     */
    private class PersonAttributeMappingQuery extends AbstractPersonAttributeMappingQuery {
        /**
         * @see AbstractPersonAttributeMappingQuery#AbstractPersonAttributeMappingQuery(DataSource, String)
         */
        public PersonAttributeMappingQuery(DataSource ds, String sql) {
            super(ds, sql);
        }

        /**
         * How attribute name mapping works:
         * If the column is mapped use the mapped name(s)<br>
         * If the column is listed and not mapped use the column name<br>
         * 
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet, int)
         */
        protected Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Map rowResults = new HashMap();
            
            //Iterates through any mapped columns that did appear in the column list from the result set
            final Set colNames = MultiRowJdbcPersonAttributeDao.this.nameValueColumnMappings.keySet();
            for (final Iterator columnNameItr = colNames.iterator(); columnNameItr.hasNext(); ) {
                final String columnName = (String)columnNameItr.next();
                
                this.addMappedAttributes(rs, columnName, rowResults);
            }

            return rowResults;
        }


        /**
         * TODO comment
         * 
         * @param rs The {@link ResultSet} to get the attribute value from.
         * @param nameColumn The name of the column to get the attribute name from.
         * @param rowResults The {@link Map} to add the mapped attribute to.
         * @throws SQLException If there is a problem retrieving the value from the {@link ResultSet}.
         */
        private void addMappedAttributes(final ResultSet rs, final String nameColumn, final Map rowResults) throws SQLException {
            if (nameColumn == null || nameColumn.length() <= 0)
                throw new IllegalArgumentException("columnName cannot be null and must have length >= 0");
            
            //Get the attribute name from the name column
            final String attributeName;
            try {
                attributeName = rs.getString(nameColumn);
            }
            catch (SQLException sqle) {
                super.logger.error("Was unable to read attribute for column [" + nameColumn + "]");
                throw sqle;
            }
            
            //Get the associated attribute values from the value columns
            final Set valueCols = (Set)MultiRowJdbcPersonAttributeDao.this.nameValueColumnMappings.get(nameColumn);
            for (final Iterator valueColItr = valueCols.iterator(); valueColItr.hasNext();) {
                final String valueColumn = (String)valueColItr.next();
                
                //Get the each value from the database adding it to the result set
                try {
                    final String attributeValue = rs.getString(valueColumn);
                    MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
                }
                catch (SQLException sqle) {
                    super.logger.error("Was unable to read attribute for column [" + valueColumn + "]");
                    throw sqle;
                }
            }
        }
    }
}