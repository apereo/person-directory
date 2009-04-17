/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.services.persondir.support;

/**
 * Optional interface that can be implemented by users of person directory to tell attribute sources the userName of the
 * current user. This is useful for sources such as {@link AdditionalDescriptorsPersonAttributeDao} where the additional
 * attributes may only be applicable for queries related to the current user of the system, and not for other users the
 * current user is getting attribute information for. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ICurrentUserProvider {
    /**
     * @return The userName of the user calling the {@link org.jasig.services.persondir.IPersonAttributeDao} API
     */
    public String getCurrentUserName();
}
