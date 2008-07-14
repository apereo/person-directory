/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support.ldap;

import org.jasig.services.persondir.support.QueryType;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.OrFilter;

/**
 * Wrapper class to allow the And and Or fitlers to be treated the same way.
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
class LogicalFilterWrapper implements Filter {
    private final QueryType queryType;
    private final AndFilter andFilter;
    private final OrFilter orFilter;
    private final Filter delegateFilter;
    
    public LogicalFilterWrapper(QueryType queryType) {
        this.queryType = queryType;

        switch (this.queryType) {
            case OR: {
                this.andFilter = null;
                this.orFilter = new OrFilter();
                
                this.delegateFilter = this.orFilter;
            } break;
            
            default:
            case AND: {
                this.andFilter = new AndFilter();
                this.orFilter = null;
                
                this.delegateFilter = this.andFilter;
            } break;
        }
    }
    
    /**
     * Append the query Filter to the underlying logical Filter
     */
    public void append(Filter query) {
        switch (this.queryType) {
            case OR: {
                this.orFilter.or(query);
            } break;
            
            default:
            case AND: {
                this.andFilter.and(query);
            } break;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.filter.Filter#encode()
     */
    public String encode() {
        return this.delegateFilter.encode();
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.filter.Filter#encode(java.lang.StringBuffer)
     */
    public StringBuffer encode(StringBuffer buf) {
        return this.delegateFilter.encode(buf);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return this.delegateFilter.equals(o);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.delegateFilter.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.delegateFilter.toString();
    }
}