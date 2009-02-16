/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

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

    /**
     * @param query The query map of attributes
     * @return The username included in the query, determined using the username attribute. Returns null if no username attribute is included in the query.
     */
    public String getUsernameFromQuery(Map<String, List<Object>> query);
}
