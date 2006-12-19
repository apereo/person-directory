package org.jasig.services.persondir.support.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;

/**
 * An object which will execute a SQL query with the expectation
 * of yielding a ResultSet with zero or one rows, which it maps
 * to null or to a Map from uPortal attribute names to values.
 */
class SingleRowPersonAttributeMappingQuery extends AbstractPersonAttributeMappingQuery {
    /**
     * The parent DAO to retrieve mapping information from during queries.
     */
    private final SingleRowJdbcPersonAttributeDao parentSingleRowPersonAttributeDao;


    /**
     * @see AbstractPersonAttributeMappingQuery#AbstractPersonAttributeMappingQuery(DataSource, String, List)
     */
    public SingleRowPersonAttributeMappingQuery(DataSource ds, String sql, List queryAttributes, SingleRowJdbcPersonAttributeDao parentSingleRowPersonAttributeDao) {
        super(ds, sql, queryAttributes);
        
        if (parentSingleRowPersonAttributeDao == null) {
            throw new IllegalArgumentException("parentSingleRowPersonAttributeDao may not be null");
        }
        
        this.parentSingleRowPersonAttributeDao = parentSingleRowPersonAttributeDao;
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
        final Map attributeMappings = this.parentSingleRowPersonAttributeDao.getColumnsToAttributes();
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
        final Map attributeMappings = this.parentSingleRowPersonAttributeDao.getColumnsToAttributes();
        Set attributeNames = (Set)attributeMappings.get(columnName);
        
        //No mapping was found, just use the column name
        if (attributeNames == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No mapped attribute name for column='" + columnName + "', defaulting to the column name.");
            }
            
            attributeNames = Collections.singleton(columnName);
        }
        
        //Run through the mapped attribute names
        for (final Iterator attrNameItr = attributeNames.iterator(); attrNameItr.hasNext();){
            final String attributeName = (String)attrNameItr.next();
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding mapped attribute '" + attributeName + "' for source column '" + columnName + "'");
            }

            MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
        }
    }
}