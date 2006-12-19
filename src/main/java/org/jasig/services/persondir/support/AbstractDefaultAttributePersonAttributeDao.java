/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.services.persondir.IPersonAttributeDao;


/**
 * Abstract class implementing the IPersonAttributeDao method 
 * {@link IPersonAttributeDao#getUserAttributes(String)} by delegation to 
 * {@link IPersonAttributeDao#getUserAttributes(Map)} using a configurable
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
 *             The attribute to use for the key in the {@link Map} passed to {@link IPersonAttributeDao#getUserAttributes(Map)}
 *             when {@link #getUserAttributes(String)} is called. The value is the uid passed
 *             to the method.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">"username"</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public abstract class AbstractDefaultAttributePersonAttributeDao implements IPersonAttributeDao {
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * Defaults attribute to use for a simple query
     */
    private String defaultAttribute = "username";
    
    
    /**
     * Implements this interface method by creating a seed Map from the
     * uid argument and delegating to getUserAttributes() on that Map.
     * 
     * Uses {@link Collections#singletonMap(java.lang.Object, java.lang.Object)}
     * to create a seed with the value rerturned by 
     * {@link #getDefaultAttributeName()} as the key and <code>uid</code>
     * as the value. Returns the result of invoking
     * {@link IPersonAttributeDao#getUserAttributes(Map)} with the new
     *  {@link Map} as the argument.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public final Map getUserAttributes(final String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(String) with null argument.");
        }
        
        final Map seed = Collections.singletonMap(this.getDefaultAttributeName(), uid);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Created seed map='" + seed + "' for uid='" + uid + "'");
        }
        
        final Map userAttributes = this.getUserAttributes(seed);
        return userAttributes;
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
        if (name == null)
            throw new IllegalArgumentException("The default attribute name may not be null");

        this.defaultAttribute = name;
    }
}
