/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.ldap;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public class QuerySearchExecutorTest extends TestCase {
    public void testNullConstructorArguments() {
        try {
            new QuerySearchExecutor(null, null, null, null);
            fail("new QuerySearchExecutor(null, null, null, null) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }

        try {
            new QuerySearchExecutor("baseDn", null, null, null);
            fail("new QuerySearchExecutor(String, null, null, null) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }

        try {
            new QuerySearchExecutor("baseDn", "query", null, null);
            fail("new QuerySearchExecutor(String, String, null, null) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }

        try {
            new QuerySearchExecutor("baseDn", "query", new Object[0], null);
            fail("new QuerySearchExecutor(String, String, Object[], null) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }
    }
}
