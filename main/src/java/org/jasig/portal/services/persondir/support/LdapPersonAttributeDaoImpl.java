/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.services.persondir.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.SearchControls;

import net.sf.ldaptemplate.CollectingSearchResultCallbackHandler;
import net.sf.ldaptemplate.ContextSource;
import net.sf.ldaptemplate.LdapTemplate;
import net.sf.ldaptemplate.SearchExecutor;

import org.jasig.portal.services.persondir.support.ldap.PersonAttributesMapper;
import org.jasig.portal.services.persondir.support.ldap.QuerySearchExecutor;
import org.springframework.dao.support.DataAccessUtils;

/**
 * LDAP implementation of {@link org.jasig.portal.services.persondir.IPersonAttributeDao}. This is code copied from
 * uPortal 2.4 org.jasig.portal.services.PersonDirectory and made to
 * implement this DAO interface. Dependent upon JNDI.
 * 
 * In the case of multi valued attributes, now stores a
 * {@link java.util.ArrayList} rather than a {@link java.util.Vector}.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class LdapPersonAttributeDaoImpl extends AbstractQueryPersonAttributeDao {
    /**
     * Time limit, in milliseconds, for LDAP query. Zero means wait
     * indefinitely.
     */
    private int timeLimit = 0;

    /**
     * The query we should execute.
     */
    private String query;

    /**
     * Class for mapping LDAP Attributes to a person attribute Map using the LdapTemplate.
     */
    private PersonAttributesMapper attributesMapper = new PersonAttributesMapper(Collections.EMPTY_MAP);

    /**
     * {@link Set} of attributes this DAO may provide when queried.
     */
    private Set possibleUserAttributeNames = Collections.EMPTY_SET;

    /**
     * The base distinguished name to use for queries.
     */
    private String baseDN = "";

    /**
     * The ContextSource to get DirContext objects for queries from.
     */
    private ContextSource contextSource = null;
    
    /**
     * The LdapTemplate to use to execute queries on the DirContext
     */
    private LdapTemplate ldapTemplate = null;
    
    /**
     * Search controls to use for LDAP queries
     */
    final private SearchControls searchControls;
    
    
    public LdapPersonAttributeDaoImpl() {
        this.searchControls = new SearchControls();
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    /**
     * Checks for valid query and context source objects.<br>
     * Executes the search.<br>
     * Returns the attribute map results from the query.<br>
     * 
     * @see org.jasig.portal.services.persondir.support.AbstractQueryPersonAttributeDao#getUserAttributesIfNeeded(java.lang.Object[])
     */
    protected Map getUserAttributesIfNeeded(final Object[] args) {
        if (this.contextSource == null)
            throw new IllegalStateException("ContextSource is null");

        if (this.query == null)
            throw new IllegalStateException("query is null");

        final SearchExecutor se = new QuerySearchExecutor(this.baseDN, this.query, args, this.searchControls);
        final CollectingSearchResultCallbackHandler srch = this.ldapTemplate.new AttributesMapperCallbackHandler(this.attributesMapper);
        
        this.ldapTemplate.search(se, srch);
        
        final List results = srch.getList();
        return (Map)DataAccessUtils.uniqueResult(results);
    }

    /*
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.possibleUserAttributeNames;
    }

    /**
     * Get the mapping from LDAP attribute names to uPortal attribute names.
     * Mapping type is from String to [String | Set of String].
     * 
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map getLdapAttributesToPortalAttributes() {
        return this.attributesMapper.getLdapAttributesToPortalAttributes();
    }

    /**
     * Set the {@link Map} to use for mapping from a ldap attribute name to a
     * portal attribute name or {@link Set} of portal attribute names. Ldap
     * attribute names that are specified but have null mappings will use the
     * ldap attribute name for the portal attribute name. Ldap attribute names
     * that are not specified as keys in this {@link Map} will be ignored. <br>
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param ldapAttributesToPortalAttributesArg
     *            {@link Map} from ldap attribute names to portal attribute
     *            names.
     * @throws IllegalArgumentException
     *             If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setLdapAttributesToPortalAttributes(final Map ldapAttributesToPortalAttributesArg) {
        final Map ldapAttributesToPortalAttributes = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(ldapAttributesToPortalAttributesArg);
        this.attributesMapper = new PersonAttributesMapper(ldapAttributesToPortalAttributes);
        final Collection userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(ldapAttributesToPortalAttributes.values());

        this.possibleUserAttributeNames = Collections.unmodifiableSet(new HashSet(userAttributeCol));
    }

    /**
     * @return Returns the timeLimit.
     */
    public int getTimeLimit() {
        return this.timeLimit;
    }

    /**
     * @param timeLimit The timeLimit to set.
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
        this.searchControls.setTimeLimit(this.timeLimit);
    }

    /**
     * @return Returns the query.
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * @param uidQuery The query to set.
     */
    public void setQuery(String uidQuery) {
        this.query = uidQuery;
    }

    /**
     * @return Returns the ldapServer.
     */
    public String getBaseDN() {
        return this.baseDN;
    }

    /**
     * @param baseDN The ldapServer to set.
     */
    public void setBaseDN(String baseDN) {
        if (baseDN == null)
            baseDN = "";
        this.baseDN = baseDN;
    }

    /**
     * @return Returns the contextSource.
     */
    public ContextSource getContextSource() {
        return this.contextSource;
    }
    
    /**
     * @param contextSource The contextSource to set.
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
        this.ldapTemplate = new LdapTemplate(this.contextSource);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("[");
        sb.append("contextSource=").append(this.contextSource);
        sb.append(", timeLimit=").append(this.timeLimit);
        sb.append(", baseDN=").append(this.baseDN);
        sb.append(", query=").append(this.query);
        sb.append(", ldapAttributesToPortalAttributes=").append(this.getLdapAttributesToPortalAttributes());
        sb.append("]");

        return sb.toString();
    }
}
