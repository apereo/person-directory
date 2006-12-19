/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;

/**
 * Provides common logic for executing a JDBC based attribute query.
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
 *         <td align="right" valign="top">queryAttributes</td>
 *         <td>
 *             This class overrides the {@link AbstractQueryPersonAttributeDao#setQueryAttributes(List)} requiring
 *             a non-null List and enforcing that it can only be set once.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public abstract class AbstractJdbcPersonAttributeDao extends AbstractQueryPersonAttributeDao {
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
    protected final Map getUserAttributesIfNeeded(final Object[] args) {
        final AbstractPersonAttributeMappingQuery query = this.getAttributeQuery();
        final List queryResults = query.execute(args);
        final Map userAttributes = this.parseAttributeMapFromResults(queryResults);
        return userAttributes;
    }

    /**
     * JDBC DAOs require a non-null queryAttribute list. This setter also only allows the queryAttributes List to be
     * set once.
     * 
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#setQueryAttributes(java.util.List)
     */
    public void setQueryAttributes(List queryAttributes) {
        if (queryAttributes == null) {
            throw new IllegalArgumentException("queryAttributes may not be null");
        }
        if (this.getQueryAttributes() != null) {
            throw new IllegalStateException("The queryAttributes List is already set, it may not be changed.");
        }
        
        super.setQueryAttributes(queryAttributes);
    }
}
