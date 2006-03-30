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

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * An {@link org.jasig.portal.services.persondir.IPersonAttributeDao}
 * implementation that maps from column names in the result of a SQL query
 * to attribute names. <br>
 * You must set a Map from column names to attribute names and only column names
 * appearing as keys in that map will be used.
 * <br>
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class JdbcPersonAttributeDaoImpl extends AbstractJdbcPersonAttributeDao {
    /**
     * {@link Map} from column names to attribute names.
     */
    private Map attributeMappings = Collections.EMPTY_MAP;
    
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
    public JdbcPersonAttributeDaoImpl(DataSource ds, List attrList, String sql) {
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
        final Map uniqueResult = (Map)DataAccessUtils.uniqueResult(queryResults);

        //If it's null no user was found, correct behavior is to return null
        return uniqueResult;
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
    public Map getColumnsToAttributes() {
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
    public void setColumnsToAttributes(final Map columnsToAttributesMap) {
        if (columnsToAttributesMap == null) {
            throw new IllegalArgumentException("columnsToAttributesMap may not be null");
        }
        
        this.attributeMappings = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(columnsToAttributesMap);
        
        if (this.attributeMappings.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        
        final Collection userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.attributeMappings.values()); 
        
        this.userAttributes = Collections.unmodifiableSet(new HashSet(userAttributeCol));
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
            for (final Iterator columnNameItr = attributeMappings.keySet().iterator(); columnNameItr.hasNext(); ) {
                final String columnName = (String)columnNameItr.next();
                
                this.addMappedAttributes(rs, columnName, rowResults);
            }

            return rowResults;
        }


        /**
         * Tries to get the attributes specified for the column, determin the
         * mapping for the column and add it to the rowResults {@link Map}.
         * 
         * @param rs The {@link ResultSet} to get the attribute value from.
         * @param columnName The name of the column to get the attribute value from.
         * @param rowResults The {@link Map} to add the mapped attribute to.
         * @throws SQLException If there is a problem retrieving the value from the {@link ResultSet}.
         */
        private void addMappedAttributes(final ResultSet rs, final String columnName, final Map rowResults) throws SQLException {
            if (columnName == null || columnName.length() <= 0)
                throw new IllegalArgumentException("columnName cannot be null and must have length >= 0");
            
            String attributeValue = null;
            
            //Get the database value
            try {
                attributeValue = rs.getString(columnName);
            }
            catch (SQLException sqle) {
                super.logger.error("Was unable to read attribute for column [" + columnName + "]");
                throw sqle;
            }
            
            //See if the column is mapped
            Set attributeNames = (Set)attributeMappings.get(columnName);
            
            //No mapping was found, just use the column name
            if (attributeNames == null)
                attributeNames = Collections.singleton(columnName);
            
            //Run through the mapped attribute names
            for (final Iterator attrNameItr = attributeNames.iterator(); attrNameItr.hasNext();){
                final String attributeName = (String)attrNameItr.next();

                MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
            }
        }
    }
}