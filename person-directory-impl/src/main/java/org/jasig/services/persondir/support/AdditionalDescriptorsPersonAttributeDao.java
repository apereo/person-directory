/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 *         <td align="right" valign="top">usernameAttributeProvider</td>
 *         <td>
 *             The provider used to determine the username attribute to use when no attribute is specified in the query. This
 *             is primarily used for calls to {@link #getPerson(String)}.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">descriptors</td>
 *         <td>
 *             The {@link AdditionalDescriptors} object that models the collection 
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
public class AdditionalDescriptorsPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
        
    // Instance Members.
    private IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();
    private AdditionalDescriptors descriptors;
    private final Log log = LogFactory.getLog(getClass());
    
    /*
     * Public API.
     */
    
    /**
     * Called by Spring DI to let us know what the username attribute is.
     */
    @Required
    public void setUsernameAttributeProvider(IUsernameAttributeProvider usernameAttributeProvider) {

        // Assertions.
        if (usernameAttributeProvider == null) {
            String msg = "Argument 'usernameAttributeProvider' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        this.usernameAttributeProvider = usernameAttributeProvider;

    }

    /**
     * Called by Spring DI to inject the collection of additional descriptors.  
     * Descriptors are user specific, and (therefore) the <code>Map</code> must 
     * be a session-scoped bean.
     */
    @Required
    public void setDescriptors(AdditionalDescriptors descriptors) {
        
        // Assertions.
        if (descriptors == null) {
            String msg = "Argument 'descriptors' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        this.descriptors = descriptors;

        if (log.isDebugEnabled()) {
            log.debug("invoking setDescriptors()");
        }

    }

    /**
     * Returns an empty <code>Set</code>, per the API documentation, because we 
     * don't use any attributes in queries.
     */
    public Set<String> getAvailableQueryAttributes() {
        return Collections.emptySet();
    }

    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {

        final Set<IPersonAttributes> rslt = new HashSet<IPersonAttributes>();
        
        if (log.isDebugEnabled()) {
            log.debug("invoking getPeopleWithMultivaluedAttributes()");
        }
        
        final String usernameAttribute = this.usernameAttributeProvider.getUsernameAttribute();
        final String name = query.containsKey(usernameAttribute) 
                                    ? (String) query.get(usernameAttribute).get(0)
                                    : usernameAttribute;

        final Map<String, List<Object>> attributes = descriptors.getAttributes();
        if (!attributes.isEmpty() && name.equals(descriptors.getName())) {
            rslt.add(new CaseInsensitiveNamedPersonImpl(name, attributes));

            if (log.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Adding additional descriptors [name=" + name + "]...");
                for (Map.Entry<String, List<Object>> y : attributes.entrySet()) {
                    msg.append("\tname=").append(y.getKey()).append(",value=")
                                            .append(y.getValue().toString());
                }
                log.debug(msg.toString());
            }
        
        }
        
        return rslt;
    }

    public IPersonAttributes getPerson(final String uid) {

        if (log.isDebugEnabled()) {
            log.debug("invoking getPerson():  uid=" + uid);
        }
        
        IPersonAttributes rslt = null;
        
        final Map<String, List<Object>> attributes = descriptors.getAttributes();
        if (!attributes.isEmpty() && descriptors.getName().equals(uid)) {
            rslt = new CaseInsensitiveNamedPersonImpl(uid, attributes);
        }
        
        return rslt;

    }

    /**
     * Returns <code>null</code>, per the API documentation, because we don't 
     * know what attributes may be available.
     */
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }

}
