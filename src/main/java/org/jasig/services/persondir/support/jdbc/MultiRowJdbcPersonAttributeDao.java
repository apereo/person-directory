/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * An {@link org.jasig.portal.services.persondir.IPersonAttributeDao}
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
 * value attribute mappings. This contrasts {@link org.jasig.portal.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao}
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
    private Map attributeNameMappings = Collections.EMPTY_MAP;
    
    /**
     * {@link Map} of columns from a name column to value columns.
     * Keys are Strings, Values are Strings or Lost of Strings 
     */
    private Map nameValueColumnMappings = null;
    
    /**
     * {@link Set} of attributes that may be provided for a user.
     */
    private Set userAttributes = Collections.EMPTY_SET;
    
    /**
     * The {@link MappingSqlQuery} to use to get attributes.
     */
    private final MultiRowPersonAttributeMappingQuery query;

    /**
     * Creates a new MultiRowJdbcPersonAttributeDao specifying the DataSource and SQL to use.
     * 
     * @param ds The DataSource to get connections from for executing queries, may not be null.
     * @param attrList Sets the query attribute list to pass to {@link AbstractJdbcPersonAttributeDao#setQueryAttributes(List)} and {@link MultiRowPersonAttributeMappingQuery#MultiRowPersonAttributeMappingQuery(DataSource, String, List, MultiRowJdbcPersonAttributeDao)}
     * @param sql The SQL to execute for user attributes, may not be null.
     */
    public MultiRowJdbcPersonAttributeDao(DataSource ds, List attrList, String sql) {
        if (ds == null) {
            throw new IllegalArgumentException("DataSource can not be null");
        }
        if (sql == null) {
            throw new IllegalArgumentException("The sql can not be null");
        }

        this.setQueryAttributes(attrList);
        final List queryAttributes = this.getQueryAttributes();
        this.query = new MultiRowPersonAttributeMappingQuery(ds, sql, queryAttributes, this);
    }
    

    /**
     * Returned {@link Map} will have values of {@link String} or a
     * {@link List} of {@link String}.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map parseAttributeMapFromResults(final List queryResults) {
        final Map results = new HashMap();

        //Iterate through the List of results, each item should be equal to a row from the ResultSet
        for (final Iterator rowItr = queryResults.iterator(); rowItr.hasNext();) {
            final Map rowResult = (Map)rowItr.next();
            
            //Iterate through the name/value(s) pairs for each row Map
            for (final Iterator resultItr = rowResult.entrySet().iterator(); resultItr.hasNext();) {
                final Map.Entry entry = (Map.Entry)resultItr.next();
                final String srcAttrName = (String)entry.getKey();
                
                //Get the Set of portal user attribute names the value(s) should be stored under.
                final Set portalAttrNames = (Set)this.attributeNameMappings.get(srcAttrName);
                
                //If no portal user attribute names are mapped add the value(s) with the attribute name from the result set
                if (portalAttrNames == null) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Adding un-mapped attribute '" + srcAttrName + "'");
                    }
                    
                    MultivaluedPersonAttributeUtils.addResult(results, srcAttrName, entry.getValue());
                }
                else {
                    //For each mapped portlet attribute name store the value(s)
                    for (final Iterator upAttrNameItr = portalAttrNames.iterator(); upAttrNameItr.hasNext();) {
                        final String portalAttrName = (String)upAttrNameItr.next();
                        
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("Adding mapped attribute '" + portalAttrName + "' for source attribute '" + srcAttrName + "'");
                        }

                        MultivaluedPersonAttributeUtils.addResult(results, portalAttrName, entry.getValue());
                    }
                }
            }
        }
        
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Mapped " + results.size() + " portal attributes from " + queryResults.size() + " source attributes");
        }
        
        return results;
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao#getAttributeQuery()
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


    /**
     * @return The Map of name column to value column(s). 
     */
    public Map getNameValueColumnMappings() {
        return this.nameValueColumnMappings;
    }
    
    /**
     * The {@link Map} of columns from a name column to value columns. Keys are Strings,
     * Values are Strings or {@link java.util.List} of Strings.
     * 
     * @param nameValueColumnMap The Map of name column to value column(s). 
     */
    public void setNameValueColumnMappings(final Map nameValueColumnMap) {
        if (nameValueColumnMap == null) {
            this.nameValueColumnMappings = null;
        }
        else {
            final Map mappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nameValueColumnMap);
            
            if (mappings.containsValue(null)) {
                throw new IllegalArgumentException("nameValueColumnMap may not have null values");
            }
            
            this.nameValueColumnMappings = mappings;
        }
    }
}