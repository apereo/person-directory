/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Required;

/**
/**
 * Implementation of {@link IPersonAttributeDao} that allows other subsystems 
 * and components to <i>push</i> attributes to the <code>IPersonAttributeDao</code> 
 * stack.  The collection of pushed attributes is represented by the 
 * <code>descriptors</code> property and backed by an instance of 
 * {@link AdditionalDescriptors}.  In most cases this property should be 
 * configured as a Session-Scoped Proxy Bean.   
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
 *         <td align="right" valign="top">descriptors</td>
 *         <td>
 *             The {@link IPersonAttributes} object that models the collection 
 *             of pushed attributes.  In most cases this property should be configured 
 *             as a Session-Scoped Proxy Bean. 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * @author awills
 */
public class AdditionalDescriptorsPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    // Instance Members.
    private IPersonAttributes descriptors;
    
    /*
     * Public API.
     */

    /**
     * Called by Spring DI to inject the collection of additional descriptors.  
     * Descriptors are user specific, and (therefore) the <code>Map</code> must 
     * be a session-scoped bean.
     */
    @Required
    public void setDescriptors(IPersonAttributes descriptors) {
        
        // Assertions.
        if (descriptors == null) {
            String msg = "Argument 'descriptors' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        this.descriptors = descriptors;

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("invoking setDescriptors(" + descriptors + ")");
        }

    }

    /**
     * Returns an empty <code>Set</code>, per the API documentation, because we 
     * don't use any attributes in queries.
     */
    public Set<String> getAvailableQueryAttributes() {
        final IUsernameAttributeProvider usernameAttributeProvider = super.getUsernameAttributeProvider();
        return Collections.singleton(usernameAttributeProvider.getUsernameAttribute());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("invoking getPeopleWithMultivaluedAttributes(" + query + ")");
        }
        
        final IUsernameAttributeProvider usernameAttributeProvider = super.getUsernameAttributeProvider();
        final String uid = usernameAttributeProvider.getUsernameFromQuery(query);
        if (uid == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No username attribute found in query, returning null");
            }
            
            return null;
        }
        
        final String descriptorsName = this.descriptors.getName();
        if (descriptorsName == null) {
            this.logger.warn("AdditionalDescriptors has a null name, returning null. " + this.descriptors);
            return null;
        }
        
        if (uid.equals(descriptorsName)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding additional descriptors " + this.descriptors);
            }
            
            final IPersonAttributes personAttributes = new CaseInsensitiveAttributeNamedPersonImpl(this.descriptors);
            return Collections.singleton(personAttributes);
        }
        
        return null;
    }

    /**
     * Returns <code>null</code>, per the API documentation, because we don't 
     * know what attributes may be available.
     */
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }

}
