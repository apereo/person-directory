/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.ListOrderedMap;

/**
 * Custom IPersonAttributes that uses a case insensitive Map to hide attribute name case
 */
public class CaseInsensitiveNamedPersonImpl extends NamedPersonImpl {
    private static final long serialVersionUID = 1L;

    public CaseInsensitiveNamedPersonImpl(String userName, Map<String, List<Object>> attributes) {
        super(userName, attributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.BasePersonImpl#createImmutableAttributeMap(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Object>> createImmutableAttributeMap(int size) {
        return ListOrderedMap.decorate(new CaseInsensitiveMap(size > 0 ? size : 1));
    }
}