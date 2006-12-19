package org.jasig.services.persondir.support.jdbc;

import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * An object which will execute a SQL query with the expectation
 * of yielding a ResultSet with zero or one rows, which it maps
 * to null or to a Map from uPortal attribute names to values.
 */
public abstract class AbstractPersonAttributeMappingQuery extends MappingSqlQuery {

    /**
     * Instantiate the query, providing a DataSource against which the query
     * will run and the SQL representing the query, which should take exactly
     * one parameter: the unique ID of the user.
     * <br>
     * The {@link org.jasig.portal.services.persondir.support.AbstractQueryPersonAttributeDao#getQueryAttributes()}
     * method on the passed {@link AbstractJdbcPersonAttributeDao} must be initialized when
     * the object is passed in.
     * 
     * @param ds The data source to use for running the query against.
     * @param sql The SQL to run against the data source.
     * @param queryAttributes The List of query attributes to declare bind variables for
     */
    public AbstractPersonAttributeMappingQuery(final DataSource ds, final String sql, final List queryAttributes) {
        super(ds, sql);
        
        //Configures the SQL parameters, everything is assumed to be VARCHAR
        for (final Iterator attrNames = queryAttributes.iterator(); attrNames.hasNext(); ) {
            final String attrName = (String)attrNames.next();
            this.declareParameter(new SqlParameter(attrName, Types.VARCHAR));
        }

        //One time compilation of the query
        this.compile();
    }
}