/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * Implements the {@link #getUserAttributes(Map)} and {@link #getUserAttributes(String)} methods to
 * provide logic to flatten the attribute result maps from &lt;String, List&lt;Object>> to &lt;String, Object>.
 * The flattening logic resides in {@link #flattenResults(Map)} which sub-classes can override if needed.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractFlatteningPersonAttributeDao implements IPersonAttributeDao {
    protected final Log logger = LogFactory.getLog(getClass());

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public final Map<String, Object> getUserAttributes(Map<String, Object> seed) {
        Validate.notNull(seed, "seed may not be null.");
        
        //Convert the <String, Object> map to a <String, List<Object>> map
        final Map<String, List<Object>> multiSeed = new HashMap<String, List<Object>>(seed.size());
        for (final Map.Entry<String, Object> seedEntry : seed.entrySet()) {
            final String seedName = seedEntry.getKey();
            final Object seedValue = seedEntry.getValue();
            multiSeed.put(seedName, Collections.singletonList(seedValue));
        }
        
        //Get the attributes from the subclass
        final Map<String, List<Object>> multivaluedUserAttributes = this.getMultivaluedUserAttributes(multiSeed);
        
        return this.flattenResults(multivaluedUserAttributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public final Map<String, Object> getUserAttributes(String uid) {
        Validate.notNull(uid, "uid may not be null.");
        
        //Get the attributes from the subclass
        final Map<String, List<Object>> multivaluedUserAttributes = this.getMultivaluedUserAttributes(uid);
        
        return this.flattenResults(multivaluedUserAttributes);
    }

    /**
     * Takes a &lt;String, List&lt;Object>> Map and coverts it to a &lt;String, Object> Map. This implementation takes
     * the first value of each List to use as the value for the new Map.
     * 
     * @param multivaluedUserAttributes The attribute map to flatten.
     * @return A flattened version of the Map, null if the argument was null.
     */
    protected Map<String, Object> flattenResults(Map<String, List<Object>> multivaluedUserAttributes) {
        if (multivaluedUserAttributes == null) {
            return null;
        }
        
        //Convert the <String, List<Object> results map to a <String, Object> map using the first value of each List
        final Map<String, Object> userAttributes = new HashMap<String, Object>(multivaluedUserAttributes.size());
        
        for (final Map.Entry<String, List<Object>> attrEntry : multivaluedUserAttributes.entrySet()) {
            final String attrName = attrEntry.getKey();
            final List<Object> attrValues = attrEntry.getValue();
            
            final Object value;
            if (attrValues == null || attrValues.size() == 0) {
                value = null;
            }
            else {
                value = attrValues.get(0);
            }
            
            userAttributes.put(attrName, value);
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Flattened Map='" + multivaluedUserAttributes + "' into Map='" + userAttributes + "'");
        }
        
        return userAttributes;
    }
}
