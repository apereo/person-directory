/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.ldap;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist 
 * @version $Revision$
 */
public class PersonAttributesMapperTest extends TestCase {
    public void testNullConstructorArgument() {
        try {
            new PersonAttributesMapper(null);
            fail("new PersonAttributesMapper(null) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }
    }
}
