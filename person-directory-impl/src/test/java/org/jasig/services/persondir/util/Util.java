/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Util {
    /**
     * Utility for making a mutable list of objects
     */
    public static List<Object> list(Object... objects) {
        if (objects == null) {
            final List<Object> list = new ArrayList<Object>(1);
            list.add(null);
            return list;
        }
        
        final List<Object> list = new ArrayList<Object>(objects.length);
        
        for (final Object obj : objects) {
            list.add(obj);
        }
        
        return list;
    }
}
