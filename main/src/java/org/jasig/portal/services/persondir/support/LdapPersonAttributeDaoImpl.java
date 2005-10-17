/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.services.persondir.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * LDAP implementation of {@link IPersonAttributeDao}. This is code copied from
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
public class LdapPersonAttributeDaoImpl extends AbstractDefaultQueryPersonAttributeDao {
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
	 * Map from LDAP attribute names to uPortal attribute names.
	 */
	private Map ldapAttributesToPortalAttributes = Collections.EMPTY_MAP;

	/**
	 * {@link Set} of attributes this DAO may provide when queried.
	 */
	private Set possibleUserAttributeNames = Collections.EMPTY_SET;

	/**
	 * List of names of uPortal attributes the values of which will be used, in
	 * order, to populate the parameters of the LDAP query.
	 */
	private List queryAttributes = Collections.EMPTY_LIST;

	/**
	 * The base distinguished name to use for queries.
	 */
	private String baseDN = "";

	/**
	 * The LdapContext to use to make the queries against.
	 */
	private LdapContext ldapContext;

	/**
	 * Returned {@link Map} will have values of String or String[] or byte[]
	 * 
	 * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
	 */
	public Map getUserAttributes(final Map seed) {
		// Checks to make sure the argument & state is valid
		if (seed == null)
			throw new IllegalArgumentException("The query seed Map cannot be null.");

		if (this.ldapContext == null)
			throw new IllegalStateException("LDAP context is null");

		if (this.query == null)
			throw new IllegalStateException("query is null");

		// Ensure the data needed to run the query is avalable
		if (!((queryAttributes != null && seed.keySet().containsAll(queryAttributes)) || 
            (queryAttributes == null && seed.containsKey(this.getDefaultAttributeName())))) {
			return null;
		}

		try {

			if (this.ldapContext == null) {
				throw new IllegalStateException("No LdapContext specified.");
			}

			// Search for the userid in the usercontext subtree of the directory
			// Use the uidquery substituting username for {0}, {1}, ...
			final SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			sc.setTimeLimit(this.timeLimit);

			// Can't just to a toArray here since the order of the keys in the Map
			// may not match the order of the keys in the List and it is important to
			// the query.
			final Object[] args = new Object[this.queryAttributes.size()];
			for (int index = 0; index < args.length; index++) {
				final String attrName = (String) this.queryAttributes.get(index);
				args[index] = seed.get(attrName);
			}

			// Search the LDAP
			final NamingEnumeration userlist = this.ldapContext.search(this.baseDN, this.query, args, sc);
			try {
				if (userlist.hasMoreElements()) {
					final Map rowResults = new HashMap();

					final SearchResult result = (SearchResult) userlist.next();

					// Only allow one result for the query, do the check here to
					// save on attribute processing time.
					if (userlist.hasMoreElements()) {
						throw new IncorrectResultSizeDataAccessException("More than one result for ldap person attribute search.", 1, -1);
					}

					final Attributes ldapAttributes = result.getAttributes();

					// Iterate through the attributes
					for (final Iterator ldapAttrIter = this.ldapAttributesToPortalAttributes.keySet().iterator(); ldapAttrIter.hasNext();) {
						final String ldapAttributeName = (String) ldapAttrIter.next();

						final Attribute attribute = ldapAttributes.get(ldapAttributeName);

						// The attribute exists
						if (attribute != null) {
							for (final NamingEnumeration attrValueEnum = attribute.getAll(); attrValueEnum.hasMore();) {
								Object attributeValue = attrValueEnum.next();

								// Convert everything except byte[] to String
								// TODO should we be doing this conversion?
								if (!(attributeValue instanceof byte[])) {
									attributeValue = attributeValue.toString();
								}

								// See if the ldap attribute is mapped
								Set attributeNames = (Set) ldapAttributesToPortalAttributes.get(ldapAttributeName);

								// No mapping was found, just use the ldap attribute name
								if (attributeNames == null)
									attributeNames = Collections.singleton(ldapAttributeName);

								// Run through the mapped attribute names
								for (final Iterator attrNameItr = attributeNames .iterator(); attrNameItr.hasNext();) {
									final String attributeName = (String) attrNameItr .next();

									MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
								}
							}
						}
					}

					return rowResults;
				} 
                else {
					return null;
				}
			} 
            finally {
				try {
					userlist.close();
				} 
                catch (final NamingException ne) {
					log.warn("Error closing ldap person attribute search results.", ne);
				}
			}
		} 
        catch (final Throwable t) {
			throw new DataAccessResourceFailureException("LDAP person attribute lookup failure.", t);
		} 
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
		return this.ldapAttributesToPortalAttributes;
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
		this.ldapAttributesToPortalAttributes = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(ldapAttributesToPortalAttributesArg);
		final Collection userAttributeCol = MultivaluedPersonAttributeUtils.flattenCollection(this.ldapAttributesToPortalAttributes.values());

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
	 * @return Returns the queryAttributes.
	 */
	public List getQueryAttributes() {
		return this.queryAttributes;
	}

	/**
	 * @param queryAttributes The queryAttributes to set.
	 */
	public void setQueryAttributes(List queryAttributes) {
		this.queryAttributes = Collections.unmodifiableList(new LinkedList(queryAttributes));
	}

    /**
     * @return Returns the ldapContext.
     */
    public LdapContext getLdapContext() {
        return this.ldapContext;
    }
    
    /**
     * @param ldapContext The ldapContext to set.
     */
    public void setLdapContext(LdapContext ldapContext) {
        this.ldapContext = ldapContext;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
        sb.append("[");
        sb.append("ldapContext=").append(this.ldapContext);
        sb.append(", timeLimit=").append(this.timeLimit);
        sb.append(", baseDN=").append(this.baseDN);
        sb.append(", query=").append(this.query);
        sb.append(", queryAttributes=").append(this.queryAttributes);
        sb.append(", ldapAttributesToPortalAttributes=").append(this.ldapAttributesToPortalAttributes);
        sb.append("]");

		return sb.toString();
	}
}
