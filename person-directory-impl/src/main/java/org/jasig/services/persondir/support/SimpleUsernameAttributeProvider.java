/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

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

}
