/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.util;

import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PatternHelperTest extends TestCase {
    public void testCompilePattern() {
        final Pattern pattern1 = PatternHelper.compilePattern("*f.oo*ba.r*");
        assertEquals(".*\\Qf.oo\\E.*\\Qba.r\\E.*", pattern1.pattern());

        final Pattern pattern2 = PatternHelper.compilePattern("f.oo*ba.r*");
        assertEquals("\\Qf.oo\\E.*\\Qba.r\\E.*", pattern2.pattern());
        
        final Pattern pattern3 = PatternHelper.compilePattern("*f.oo*ba.r");
        assertEquals(".*\\Qf.oo\\E.*\\Qba.r\\E", pattern3.pattern());
        
        final Pattern pattern4 = PatternHelper.compilePattern("f.oo*ba.r");
        assertEquals("\\Qf.oo\\E.*\\Qba.r\\E", pattern4.pattern());
        
        final Pattern pattern5 = PatternHelper.compilePattern("f.ooba.r");
        assertEquals("\\Qf.ooba.r\\E", pattern5.pattern());
    }
}
