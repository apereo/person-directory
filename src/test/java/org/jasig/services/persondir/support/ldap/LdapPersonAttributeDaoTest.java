/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ldaptemplate.support.LdapContextSource;

import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.springframework.dao.DataAccessResourceFailureException;


/**
 * Testcase for LdapPersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class LdapPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {
    
    LdapContextSource contextSource;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.contextSource = new LdapContextSource();
        this.contextSource.setUrl("ldap://mrfrumble.its.yale.edu:389");
        this.contextSource.setBase("o=yale.edu");
        this.contextSource.afterPropertiesSet();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        this.contextSource = null;
    }

    public void testNotFoundQuery() {
        final String queryAttr = "uid";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "unknown");
        
        try {
            Map attribs = impl.getUserAttributes(queryMap);
            assertNull(attribs);
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test for a query with a single attribute. 
     * 
     * This testcase will cease to work on that fateful day when Susan
     * no longer appears in Yale University LDAP.
     */
    public void testSingleAttrQuery() {
        final String queryAttr = "uid";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "susan");

        try {
            Map attribs = impl.getUserAttributes(queryMap);
            assertEquals("susan.bramhall@yale.edu", attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testInvalidAttrMap() {
        final String queryAttr = "uid";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("email", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "susan");
        
        try {
            Map attribs = impl.getUserAttributes(queryMap);
            assertNull(attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testDefaultAttrMap() {
        final String queryAttr = "uid";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", null);
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "susan");
        
        try {
            Map attribs = impl.getUserAttributes(queryMap);
            assertEquals("susan.bramhall@yale.edu", attribs.get("mail"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "alias";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(&(uid={0})(alias={1}))");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "susan");
        queryMap.put(queryAttr2, "susan.bramhall");
        queryMap.put("email", "edalquist@unicon.net");
        
        try {
            Map attribs = impl.getUserAttributes(queryMap);
            assertEquals("susan.bramhall@yale.edu", attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "alias";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(&(uid={0})(alias={1}))");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "susan");
        queryMap.put("email", "edalquist@unicon.net");
        
        Map attribs = impl.getUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testAttributeNames() {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        ldapAttribsToPortalAttribs.put("shirtColor", "dressShirtColor");
        
        Set surNameAttributeNames = new HashSet();
        surNameAttributeNames.add("surName");
        surNameAttributeNames.add("lastName");
        surNameAttributeNames.add("familyName");
        surNameAttributeNames.add("thirdName");
        ldapAttribsToPortalAttribs.put("lastName", surNameAttributeNames);
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        Set expectedAttributeNames = new HashSet();
        expectedAttributeNames.addAll(surNameAttributeNames);
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("dressShirtColor");
        
        assertEquals(expectedAttributeNames, impl.getPossibleUserAttributeNames());
    }
    
    public void testProperties() {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        assertEquals("", impl.getBaseDN());
        impl.setBaseDN("BaseDN");
        assertEquals("BaseDN", impl.getBaseDN());
        impl.setBaseDN(null);
        assertEquals("", impl.getBaseDN());
        
        
        assertEquals(Collections.EMPTY_MAP, impl.getLdapAttributesToPortalAttributes());
        
        Map attrMap = new HashMap();
        attrMap.put("mail", "email");
        impl.setLdapAttributesToPortalAttributes(attrMap);
        
        Map expectedAttrMap = new HashMap(attrMap);
        expectedAttrMap.put("mail", Collections.singleton("email"));
        assertEquals(expectedAttrMap, impl.getLdapAttributesToPortalAttributes());
        
        
        assertNull(impl.getContextSource());
        impl.setContextSource(this.contextSource);
        assertEquals(contextSource, impl.getContextSource());
        
        
        impl.setLdapAttributesToPortalAttributes(null);
        assertEquals(Collections.EMPTY_SET, impl.getPossibleUserAttributeNames());
        impl.setLdapAttributesToPortalAttributes(attrMap);
        assertEquals(Collections.singleton("email"), impl.getPossibleUserAttributeNames());
        
        
        assertNull(impl.getQuery());
        impl.setQuery("QueryString");
        assertEquals("QueryString", impl.getQuery());
        
        
        assertEquals(Collections.EMPTY_LIST, impl.getQueryAttributes());
        impl.setQueryAttributes(Collections.singletonList("QAttr"));
        assertEquals(Collections.singletonList("QAttr"), impl.getQueryAttributes());
        
        
        assertEquals(0, impl.getTimeLimit());
        impl.setTimeLimit(1337);
        assertEquals(1337, impl.getTimeLimit());
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testNullContext() {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        impl.setContextSource(this.contextSource);
        
        try {
            impl.getUserAttributes(Collections.singletonMap("dummy", "seed"));
            fail("IllegalStateException should have been thrown with no query configured");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testNullQuery() {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        try {
            impl.getUserAttributes(Collections.singletonMap("dummy", "seed"));
            fail("IllegalStateException should have been thrown with no context configured");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }

    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "uid";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map ldapAttribsToPortalAttribs = new HashMap();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        return impl;
    }
}