/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
public class ReplacingAttributeAdder implements IAttributeMerger {

    /**
     * Please note that the <code>toModify</code> map is modified.
     * 
     * @see org.jasig.portal.services.persondir.support.merger.IAttributeMerger#mergeAttributes(java.util.Map, java.util.Map)
     */
    public Map<String, List<Object>> mergeAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");
        
        toModify.putAll(toConsider);
        
        return toModify;
    }
}
