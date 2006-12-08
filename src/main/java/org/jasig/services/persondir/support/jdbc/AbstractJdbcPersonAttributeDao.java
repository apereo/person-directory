/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;

/**
 * Provides basic implementations for configuring query attributes, ensuring queries have the needed
 * attributes to execute, run the query via an abstract MappingSqlQuery stub.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public abstract class AbstractJdbcPersonAttributeDao extends AbstractQueryPersonAttributeDao {
    
    /***
     * Create the DAO, configured with the needed query information.
     *
     * @param ds The {@link DataSource} to run the queries against.
     * @param attrList The list of arguments for the query.
     * @param sql The SQL query to run.
     */
    public AbstractJdbcPersonAttributeDao(final DataSource ds, final List attrList, final String sql) {
        if (super.log.isTraceEnabled()) {
            log.trace("entering AbstractJdbcPersonAttributeDao(" + ds + ", " + attrList + ", " + sql + ")");
        }
        if (attrList == null)
            throw new IllegalArgumentException("attrList cannot be null");

        //Defensive copy of the query attribute list
        final List defensiveCopy = new ArrayList(attrList);
        final List queryAttributes = Collections.unmodifiableList(defensiveCopy);
        this.setQueryAttributes(queryAttributes);

        if (log.isTraceEnabled()) {
            log.trace("Constructed " + this);
        }
    }
    

    
    /**
     * Takes the {@link List} from the {@link AbstractPersonAttributeMappingQuery} implementation
     * and passes it to the implementing the class for parsing into the returned user attribute Map.
     * 
     * @param queryResults Results from the query done using the {@link AbstractPersonAttributeMappingQuery} returned by {@link #getAttributeQuery()}
     * @return The results of the query, as specified by {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(Map)} 
     */
    protected abstract Map parseAttributeMapFromResults(final List queryResults);
    
    /**
     * @return The subclasses implementation of the {@link AbstractPersonAttributeMappingQuery}.
     */
    protected abstract AbstractPersonAttributeMappingQuery getAttributeQuery();


    /***
     * Gets the query from the {@link #getAttributeQuery()} method.<br>
     * Runs the query.<br>
     * Calls {@link #parseAttributeMapFromResults(List)} with the query results.<br>
     * Returns results from {@link #parseAttributeMapFromResults(List)} link.<br>
     *
     * @see org.jasig.portal.services.persondir.support.AbstractQueryPersonAttributeDao#getUserAttributesIfNeeded(java.lang.Object[])
     */
    protected Map getUserAttributesIfNeeded(final Object[] args) {
        final AbstractPersonAttributeMappingQuery query = this.getAttributeQuery();
        final List queryResults = query.execute(args);
        final Map userAttributes = this.parseAttributeMapFromResults(queryResults);
        return userAttributes;
    }
}
