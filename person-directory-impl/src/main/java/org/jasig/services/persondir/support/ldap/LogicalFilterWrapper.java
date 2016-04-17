/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public LogicalFilterWrapper(final QueryType queryType) {
        this.queryType = queryType;

        switch (this.queryType) {
            case OR: {
                this.andFilter = null;
                this.orFilter = new OrFilter();

                this.delegateFilter = this.orFilter;
            }
            break;

            default:
            case AND: {
                this.andFilter = new AndFilter();
                this.orFilter = null;

                this.delegateFilter = this.andFilter;
            }
            break;
        }
    }

    /**
     * Append the query Filter to the underlying logical Filter
     */
    public void append(final Filter query) {
        switch (this.queryType) {
            case OR: {
                this.orFilter.or(query);
            }
            break;

            default:
            case AND: {
                this.andFilter.and(query);
            }
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.filter.Filter#encode()
     */
    @Override
    public String encode() {
        return this.delegateFilter.encode();
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.filter.Filter#encode(java.lang.StringBuffer)
     */
    @Override
    public StringBuffer encode(final StringBuffer buf) {
        return this.delegateFilter.encode(buf);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
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
