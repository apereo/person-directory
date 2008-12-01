/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SimpleUsernameAttributeProvider implements IUsernameAttributeProvider {
    private String usernameAttribute = "username";
    
    public SimpleUsernameAttributeProvider() {
    }
    
    public SimpleUsernameAttributeProvider(String usernameAttribute) {
        this.setUsernameAttribute(usernameAttribute);
    }
    
    /**
     * The usernameAttribute to use
     */
    public void setUsernameAttribute(String usernameAttribute) {
        Validate.notNull(usernameAttribute);
        this.usernameAttribute = usernameAttribute;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IUsernameAttributeProvider#getUsernameAttribute()
     */
    public String getUsernameAttribute() {
        return this.usernameAttribute;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.IUsernameAttributeProvider#getUsernameFromQuery(java.util.Map)
     */
    public String getUsernameFromQuery(Map<String, List<Object>> query) {
        final List<Object> usernameAttributeValues = query.get(this.usernameAttribute);
        
        if (usernameAttributeValues == null || usernameAttributeValues.size() == 0) {
            return null;
        }

        final Object firstValue = usernameAttributeValues.get(0);
        if (firstValue == null) {
            return null;
        }
        
        return StringUtils.trimToNull(String.valueOf(firstValue));
    }
}
