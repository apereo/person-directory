/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.ArrayUtils;

import net.sf.ldaptemplate.SearchExecutor;

/**
 * Executes a LDAP search using the {@link javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, java.lang.Object[], javax.naming.directory.SearchControls)}
 * method.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public class QuerySearchExecutor implements SearchExecutor {
    final String baseDn;
    final String query;
    final Object[] args;
    final SearchControls controls;
    
    /**
     * Creates a new query search executor with the parameters for the call to 
     * {@link javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, java.lang.Object[], javax.naming.directory.SearchControls)}
     */
    public QuerySearchExecutor(String baseDn, String query, Object[] args, SearchControls controls) {
        if (baseDn == null) {
            throw new IllegalArgumentException("baseDn may not be null");
        }
        if (query == null) {
            throw new IllegalArgumentException("query may not be null");
        }
        if (args == null) {
            throw new IllegalArgumentException("args may not be null");
        }
        if (controls == null) {
            throw new IllegalArgumentException("controls may not be null");
        }
        
        this.baseDn = baseDn;
        this.query = query;
        this.args = ArrayUtils.clone(args);
        this.controls = controls;
    }
    
    /**
     * @see javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, java.lang.Object[], javax.naming.directory.SearchControls)
     * @see net.sf.ldaptemplate.SearchExecutor#executeSearch(javax.naming.directory.DirContext)
     */
    public NamingEnumeration executeSearch(DirContext ctx) throws NamingException {
        return ctx.search(this.baseDn, this.query, this.args, this.controls);
    }
}
