/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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