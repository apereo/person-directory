/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * Provides the username attribute based on a pre-configured string. Determines the username from a query Map based
 * on the configured attribute, {@link StringUtils#trimToNull(String)}, and if the username value does not contain a
 * wildcard.
 * 
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
        
        final String username = StringUtils.trimToNull(String.valueOf(firstValue));
        if (username == null || username.contains(IPersonAttributeDao.WILDCARD)) {
            return null;
        }
        
        return username;
    }
}
