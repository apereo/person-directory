/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class NamedPersonImpl extends BasePersonImpl {
    private static final long serialVersionUID = 1L;

    private final String userName;

    public NamedPersonImpl(String userName, Map<String, List<Object>> attributes) {
        super(attributes);
        
        this.userName = userName;
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    public String getName() {
        return this.userName;
    }
}
