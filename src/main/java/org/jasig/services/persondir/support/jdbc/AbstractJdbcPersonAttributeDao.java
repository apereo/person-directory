/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Provides common logic for executing a JDBC based attribute query.
 * 
 * 
 * @author Eric Dalquist 
 * @version $Revision$
 */
public abstract class AbstractJdbcPersonAttributeDao extends AbstractQueryPersonAttributeDao {
    private final SimpleJdbcTemplate simpleJdbcTemplate;
    private final String sql;
    
    /**
     * @param ds The DataSource to use for queries
     * @param sql The SQL to execute
     */
    public AbstractJdbcPersonAttributeDao(DataSource ds, String sql) {
        Validate.notNull(ds, "DataSource can not be null");
        Validate.notNull(sql, "sql can not be null");
        
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
        this.sql = sql;
    }
    
    /**
     * @return the sql that is used for the query
     */
    protected String getSql() {
        return sql;
    }


    /**
     * Takes the {@link List} from the {@link AbstractPersonAttributeMappingQuery} implementation
     * and passes it to the implementing the class for parsing into the returned user attribute Map.
     * 
     * @param queryResults Results from the query done using the {@link AbstractPersonAttributeMappingQuery} returned by {@link #getAttributeQuery()}
     * @return The results of the query, as specified by {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(Map)} 
     */
    protected abstract Map<String, List<Object>> parseAttributeMapFromResults(final List<Map<String, Object>> queryResults);
    
    /***
     * Gets the query from the {@link #getAttributeQuery()} method.<br>
     * Runs the query.<br>
     * Calls {@link #parseAttributeMapFromResults(List)} with the query results.<br>
     * Returns results from {@link #parseAttributeMapFromResults(List)} link.<br>
     */
    @Override
    protected Map<String, List<Object>> getUserAttributesIfNeeded(final Object[] args) {
        final List<Map<String, Object>> queryResults = this.simpleJdbcTemplate.queryForList(this.sql, args);
        
        return this.parseAttributeMapFromResults(queryResults);
    }
}
