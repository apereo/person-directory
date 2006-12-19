package org.jasig.services.persondir.support.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
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
class MultiRowPersonAttributeMappingQuery extends AbstractPersonAttributeMappingQuery {
    /**
     * The parent DAO to retrieve mapping information from during queries.
     */
    private final MultiRowJdbcPersonAttributeDao parentMultiRowPersonAttributeDao;


    /**
     * @see AbstractPersonAttributeMappingQuery#AbstractPersonAttributeMappingQuery(DataSource, String, List)
     */
    public MultiRowPersonAttributeMappingQuery(DataSource ds, String sql, List queryAttributes, MultiRowJdbcPersonAttributeDao parentMultiRowPersonAttributeDao) {
        super(ds, sql, queryAttributes);
        
        if (parentMultiRowPersonAttributeDao == null) {
            throw new IllegalArgumentException("parentMultiRowPersonAttributeDao may not be null");
        }
        
        this.parentMultiRowPersonAttributeDao = parentMultiRowPersonAttributeDao;
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
        final Map nameValueColumnMappings = this.parentMultiRowPersonAttributeDao.getNameValueColumnMappings();
        if (nameValueColumnMappings == null) {
            throw new IllegalStateException("Property nameValueColumnMappings on MultiRowJdbcPersonAttributeDao='" + this.parentMultiRowPersonAttributeDao + "' can not be null");
        }

        final Set colNames = nameValueColumnMappings.keySet();
        for (final Iterator columnNameItr = colNames.iterator(); columnNameItr.hasNext(); ) {
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
        final Map nameValueColumnMappings = this.parentMultiRowPersonAttributeDao.getNameValueColumnMappings();
        final Set valueCols = (Set)nameValueColumnMappings.get(nameColumn);
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
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved " + valueCols.size() + " values for name column '" + nameColumn + "'");
        }
    }
}