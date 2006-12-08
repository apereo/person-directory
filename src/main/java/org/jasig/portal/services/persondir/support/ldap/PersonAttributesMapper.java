/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.jasig.portal.services.persondir.support.MultivaluedPersonAttributeUtils;


import net.sf.ldaptemplate.AttributesMapper;

/**
 * Provides {@link net.sf.ldaptemplate.AttributesMapper} for use with a {@link net.sf.ldaptemplate.LdapTemplate}
 * to parse ldap query results into the person attribute Map format.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
class PersonAttributesMapper implements AttributesMapper {
    private final Map ldapAttributesToPortalAttributes;
    
    /**
     * Create a mapper with the ldap to portal attribute mappings. Please read the
     * documentation for {@link org.jasig.portal.services.persondir.support.ldap.LdapPersonAttributeDao#setLdapAttributesToPortalAttributes(Map)}
     * 
     * @param ldapAttributesToPortalAttributes Map of ldap to portal attributes.
     * @see org.jasig.portal.services.persondir.support.ldap.LdapPersonAttributeDao#setLdapAttributesToPortalAttributes(Map)
     */
    public PersonAttributesMapper(Map ldapAttributesToPortalAttributes) {
        if (ldapAttributesToPortalAttributes == null) {
            throw new IllegalArgumentException("ldapAttributesToPortalAttributes may not be null");
        }
        
        this.ldapAttributesToPortalAttributes = ldapAttributesToPortalAttributes;
    }
    
    /**
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map getLdapAttributesToPortalAttributes() {
        return this.ldapAttributesToPortalAttributes;
    }

    /**
     * Performs mapping after an LDAP query for a set of user attributes. Takes each key in the ldap
     * to portal attribute Map and tries to find it in the returned Attributes set. For each found
     * Attribute the value is added to the attribute Map as the value or in the value Set with the
     * portal attribute name as the key. String and byte[] may be values.
     * 
     * @see net.sf.ldaptemplate.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        final Map rowResults = new HashMap();

        for (final Iterator ldapAttrIter = this.ldapAttributesToPortalAttributes.keySet().iterator(); ldapAttrIter.hasNext();) {
            final String ldapAttributeName = (String) ldapAttrIter.next();

            final Attribute attribute = attributes.get(ldapAttributeName);

            // The attribute exists
            if (attribute != null) {
                for (final NamingEnumeration attrValueEnum = attribute.getAll(); attrValueEnum.hasMore();) {
                    Object attributeValue = attrValueEnum.next();

                    // Convert everything except byte[] to String
                    if (!(attributeValue instanceof byte[])) {
                        attributeValue = attributeValue.toString();
                    }

                    // See if the ldap attribute is mapped
                    Set attributeNames = (Set)this.ldapAttributesToPortalAttributes.get(ldapAttributeName);

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
}