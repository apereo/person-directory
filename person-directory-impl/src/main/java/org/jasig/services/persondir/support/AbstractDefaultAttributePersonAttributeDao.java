/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;


/**
 * Abstract class implementing the IPersonAttributeDao method  {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(String)}
 * and {@link org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(String)} methods by
 * delegation to {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(Map)} or
 * {@link org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(Map)} using a configurable
 * default attribute name.
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
 *         <td align="right" valign="top">defaultAttribute</td>
 *         <td>
 *             The attribute to use for the key in the {@link Map} passed to {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}
 *             or {@link org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(Map)} when
 *             {@link #getMultivaluedUserAttributes(String)} is called. The value is the uid passed to the method.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">"username"</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public abstract class AbstractDefaultAttributePersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
    /**
     * Defaults attribute to use for a simple query
     */
    private String defaultAttribute = "username";
    
    /**
     * Implements this interface method by creating a seed Map from the uid argument and delegating to
     * {@link #getMultivaluedUserAttributes(Map)} using the created seed Map.
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(String)
     */
    public final Map<String, List<Object>> getMultivaluedUserAttributes(String uid) {
        Validate.notNull(uid, "uid may not be null.");
        
        final List<Object> values = Collections.singletonList((Object)uid);
        final Map<String, List<Object>> seed = Collections.singletonMap(this.getDefaultAttributeName(), values);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Created seed map='" + seed + "' for uid='" + uid + "'");
        }
        
        return this.getMultivaluedUserAttributes(seed);
    }


    /**
     * Returns the attribute set by {@link #setDefaultAttributeName(String)} or
     * if it has not been called the default value "uid" is returned.
     * 
     * @return The default single string query attribute, will never be null.
     */
    public final String getDefaultAttributeName() {
        return this.defaultAttribute;
    }
    
    /**
     * Sets the attribute to use for {@link #getUserAttributes(String)} queries.
     * It cannot be <code>null</code>.
     * 
     * @param name The attribute name to set as default.
     * @throws IllegalArgumentException if <code>name</code> is <code>null</code>.
     */
    public final void setDefaultAttributeName(final String name) {
        Validate.notNull(name, "The default attribute name may not be null");
        this.defaultAttribute = name;
    }
}
