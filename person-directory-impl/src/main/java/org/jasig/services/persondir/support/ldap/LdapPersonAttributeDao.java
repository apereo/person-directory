/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.services.persondir.support.ldap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.util.Assert;

/**
 * LDAP implementation of {@link org.jasig.services.persondir.IPersonAttributeDao}.
 * 
 * In the case of multi valued attributes a {@link java.util.List} is set as the value.
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
 *         <td align="right" valign="top">timeLimit</td>
 *         <td>
 *             Sets a time limit for LDAP Query execution time. See {@link SearchControls#setTimeLimit(int)}
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">0</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">ldapAttributesToPortalAttributes</td>
 *         <td>
 *             The {@link Map} of {@link String} ldap attribute names to {@link String} or
 *             {@link Set}s of {@link String}s to use as attribute names in the returned Map.
 *             If a ldap attribute name is not in the map the ldap attribute name will be
 *             used in as the returned attribute name.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link Collections#EMPTY_MAP}</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">query</td>
 *         <td>
 *             The LDAP filter query to use when finding the user attributes.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">baseDN</td>
 *         <td>
 *             The base DistinguishedName to use when executing the query filter.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">""</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">contextSource</td>
 *         <td>
 *             A {@link ContextSource} from the Spring-LDAP framework. Provides a DataSource
 *             style object that this DAO can retrieve LDAP connections from.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class LdapPersonAttributeDao extends AbstractQueryPersonAttributeDao implements InitializingBean {
        /**
     * Class for mapping LDAP Attributes to a person attribute Map using the LdapTemplate.
     */
    @SuppressWarnings("unchecked")
    private PersonAttributesMapper attributesMapper = new PersonAttributesMapper(Collections.EMPTY_MAP);

    /**
     * {@link Set} of attributes this DAO may provide when queried.
     */
    private Set<String> possibleUserAttributeNames = Collections.emptySet();

    /**
     * The LdapTemplate to use to execute queries on the DirContext
     */
    private LdapTemplate ldapTemplate = null;

    private String query = null;
    private String baseDN = "";
    private ContextSource contextSource = null;
    private SearchControls searchControls = new SearchControls();
    private boolean setReturningAttributes = true;
    
    
    public LdapPersonAttributeDao() {
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.setReturningAttributes) {
            final Map<String, Set<String>> ldapAttributesToPortalAttributes = this.attributesMapper.getLdapAttributesToPortalAttributes();
            final Set<String> ldapAttributes = ldapAttributesToPortalAttributes.keySet();
            this.searchControls.setReturningAttributes(ldapAttributes.toArray(new String[ldapAttributes.size()]));
        }
        
        if (this.contextSource == null) {
            throw new BeanCreationException("contextSource must be set");
        }
        
        if (this.query == null) {
            throw new BeanCreationException("query must be set");
        }
    }

    /**
     * Checks for valid query and context source objects.<br>
     * Executes the search.<br>
     * Returns the attribute map results from the query.<br>
     * 
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getUserAttributesIfNeeded(java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Object>> getUserAttributesIfNeeded(Object[] args) {
        final SearchExecutor se = new QuerySearchExecutor(this.baseDN, this.query, args, this.searchControls);
        final CollectingNameClassPairCallbackHandler attributesMapperCallbackHandler = new AttributesMapperCallbackHandler(this.attributesMapper);
        
        this.ldapTemplate.search(se, attributesMapperCallbackHandler);
        
        final List<Map<String, List<Object>>> results = attributesMapperCallbackHandler.getList();
        return (Map<String, List<Object>>)DataAccessUtils.uniqueResult(results);
    }

    /*
     * @see org.jasig.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.possibleUserAttributeNames;
    }

    /**
     * Get the mapping from LDAP attribute names to uPortal attribute names.
     * Mapping type is from String to [String | Set of String].
     * 
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map<String, Set<String>> getLdapAttributesToPortalAttributes() {
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
    public void setLdapAttributesToPortalAttributes(final Map<String, Object> ldapAttributesToPortalAttributesArg) {
        final Map<String, Set<String>> ldapAttributesToPortalAttributes = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(ldapAttributesToPortalAttributesArg);
        this.attributesMapper = new PersonAttributesMapper(ldapAttributesToPortalAttributes);
        final Collection<String> userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(ldapAttributesToPortalAttributes.values());

        this.possibleUserAttributeNames = Collections.unmodifiableSet(new HashSet<String>(userAttributeCol));
    }

    /**
     * @see javax.naming.directory.SearchControls#getTimeLimit()
     * @deprecated Set the property on the {@link SearchControls} and set that via {@link #setSearchControls(SearchControls)}
     */
    @Deprecated
    public int getTimeLimit() {
        return this.searchControls.getTimeLimit();
    }

    /**
     * @see javax.naming.directory.SearchControls#setTimeLimit(int)
     * @deprecated
     */
    @Deprecated
    public void setTimeLimit(int ms) {
        this.searchControls.setTimeLimit(ms);
    }
    
    /**
     * @return The query to be executed.
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * @param uidQuery The query to be executed.
     */
    public void setQuery(String uidQuery) {
        Assert.notNull(uidQuery, "uidQuery can not be null");
        this.query = uidQuery;
    }

    /**
     * @return The base distinguished name to use for queries.
     */
    public String getBaseDN() {
        return this.baseDN;
    }

    /**
     * @param baseDN The base distinguished name to use for queries.
     */
    public void setBaseDN(String baseDN) {
        if (baseDN == null) {
            baseDN = "";
        }

        this.baseDN = baseDN;
    }

    /**
     * @return The ContextSource to get DirContext objects for queries from.
     */
    public ContextSource getContextSource() {
        return this.contextSource;
    }
    
    /**
     * @param contextSource The ContextSource to get DirContext objects for queries from.
     */
    public void setContextSource(ContextSource contextSource) {
        Assert.notNull(contextSource, "contextSource can not be null");
        this.contextSource = contextSource;
        this.ldapTemplate = new LdapTemplate(this.contextSource);
    }

    /**
     * @return Search controls to use for LDAP queries
     */
    public SearchControls getSearchControls() {
        return this.searchControls;
    }
    /**
     * @param searchControls Search controls to use for LDAP queries
     */
    public void setSearchControls(SearchControls searchControls) {
        Assert.notNull(searchControls, "searchControls can not be null");
        this.searchControls = searchControls;
    }
}
