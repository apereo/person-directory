/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

/**
 * Provider for the username attribute to use when one is not otherwise provided.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUsernameAttributeProvider {
    /**
     * @return The username attribute to use when one is not otherwise provided, will never return null.
     */
    public String getUsernameAttribute();
}
