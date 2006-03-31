/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.portal.services.persondir.support.MultivaluedPersonAttributeUtils;
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
public class SingleRowJdbcPersonAttributeDao extends AbstractJdbcPersonAttributeDao {
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
    private SingleRowPersonAttributeMappingQuery query;
        

    /**
     * @see AbstractJdbcPersonAttributeDao#AbstractJdbcPersonAttributeDao(DataSource, List, String)
     */
    public SingleRowJdbcPersonAttributeDao(DataSource ds, List attrList, String sql) {
        super(ds, attrList, sql);
        this.query = new SingleRowPersonAttributeMappingQuery(ds, sql, this);
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
}