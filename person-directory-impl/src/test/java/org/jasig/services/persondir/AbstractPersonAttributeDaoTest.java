/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test conformance to IPersonAttributeDao interface specified 
 * IllegalArgumentException throwing for illegal invocations of interface methods.
 * @version $Revision: 43106 $ $Date: 2008-02-14 11:22:40 -0600 (Thu, 14 Feb 2008) $
 */
@SuppressWarnings("deprecation")
public abstract class AbstractPersonAttributeDaoTest extends TestCase {
    
    /**
     * Get an instance of the type of IPersonAttributeDao the implementing
     * testcase is intended to test.
     * 
     * This method will be invoked exactly once per invocation of each test method
     * implemented in this abstract class.
     
     * @return an IPersonAttributeDao instance for us to test
     */
    protected abstract IPersonAttributeDao getPersonAttributeDaoInstance();

    /**
     * Test that invocation of getMultivaluedUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's 
     * interface declaration.
     */
    public void testNullSeed() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        Map<String, List<Object>> nullMap = null;
        try {
            dao.getMultivaluedUserAttributes(nullMap);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getMultivaluedUserAttributes((Map)null)");

    }

    /**
     * Test that invocation of getUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's 
     * interface declaration.
     */
    public void testMultiNullSeed() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        Map<String, Object> nullMap = null;
        try {
            dao.getUserAttributes(nullMap);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes((Map)null)");

    }
    
    /**
     * Test that invocation of getMultivaluedUserAttributes(String null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    public void testMultiNullUid() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        String nullString = null;
        try {
            dao.getMultivaluedUserAttributes(nullString);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getMultivaluedUserAttributes((String)null)");
    }
    
    /**
     * Test that invocation of getUserAttributes(String null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    public void testNullUid() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        String nullString = null;
        try {
            dao.getUserAttributes(nullString);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes((String)null)");
    }
    
    /**
     * Test that invocation of getPersonAttributeDaoInstance() is not
     * null and immutable
     */
    public void testPossibleSetConstraints() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        Set<String> possibleNames = dao.getPossibleUserAttributeNames();
        
        if (possibleNames != null) {
            try {
                final int originalSize = possibleNames.size();
                
                final String newObj = new String();
                possibleNames.add(newObj);
                
                assertEquals(originalSize, possibleNames.size());
            }
            catch (Exception e) {
                //An exception may be thrown since the Set should be immutable.
            }
        }
    }
}

