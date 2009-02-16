/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.merger;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Attribute merge strategy whereby considered attributes over-write
 * previously set values for attributes with colliding names.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ReplacingAttributeAdder extends BaseAdditiveAttributeMerger {

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.BaseAdditiveAttributeMerger#mergePersonAttributes(java.util.Map, java.util.Map)
     */
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");
        
        toModify.putAll(toConsider);
        
        return toModify;
    }
}
