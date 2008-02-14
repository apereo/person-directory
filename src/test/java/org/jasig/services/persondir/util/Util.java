/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
