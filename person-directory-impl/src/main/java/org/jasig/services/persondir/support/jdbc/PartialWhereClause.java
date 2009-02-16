/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.jdbc;

import java.util.LinkedList;
import java.util.List;

class PartialWhereClause {
    public final StringBuilder sql = new StringBuilder();
    public final List<String> arguments = new LinkedList<String>();
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "sql=[" + this.sql + "] args=" + this.arguments;
    }
}