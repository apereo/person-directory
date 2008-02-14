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

import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.jasig.services.persondir.util.Util;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.support.LdapContextSource;


/**
 * Testcase for LdapPersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class LdapPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {
    
    LdapContextSource contextSource;
    
    /*
     * @see TestCase#setUp()
     */
    @Override
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
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        this.contextSource = null;
    }

    public void testNotFoundQuery() {
        final String queryAttr = "uid";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("unknown"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
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
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("susan"));

        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("susan.bramhall@yale.edu"), attribs.get("email"));
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
    public void testMultipleMappings() {
        final String queryAttr = "uid";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        Set<String> portalAttributes = new HashSet<String>();
        portalAttributes.add("email");
        portalAttributes.add("work.email");
        ldapAttribsToPortalAttribs.put("mail", portalAttributes);
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("susan"));

        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("susan.bramhall@yale.edu"), attribs.get("email"));
            assertEquals(Util.list("susan.bramhall@yale.edu"), attribs.get("work.email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testInvalidAttrMap() {
        final String queryAttr = "uid";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("email", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("susan"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertNull(attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testDefaultAttrMap() {
        final String queryAttr = "uid";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", null);
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("susan"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("susan.bramhall@yale.edu"), attribs.get("mail"));
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
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(&(uid={0})(alias={1}))");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr1, Util.list("susan"));
        queryMap.put(queryAttr2, Util.list("susan.bramhall"));
        queryMap.put("email", Util.list("edalquist@unicon.net"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("susan.bramhall@yale.edu"), attribs.get("email"));
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
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(&(uid={0})(alias={1}))");
        
        impl.setQueryAttributes(queryAttrList);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr1, Util.list("susan"));
        queryMap.put("email", Util.list("edalquist@unicon.net"));
        
        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testAttributeNames() {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        ldapAttribsToPortalAttribs.put("shirtColor", "dressShirtColor");
        
        Set<String> surNameAttributeNames = new HashSet<String>();
        surNameAttributeNames.add("surName");
        surNameAttributeNames.add("lastName");
        surNameAttributeNames.add("familyName");
        surNameAttributeNames.add("thirdName");
        ldapAttribsToPortalAttribs.put("lastName", surNameAttributeNames);
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        Set<String> expectedAttributeNames = new HashSet<String>();
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
        
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("mail", "email");
        impl.setLdapAttributesToPortalAttributes(attrMap);
        
        Map<String, Set<String>> expectedAttrMap = new HashMap<String, Set<String>>();
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
        
        
        assertNull(impl.getQueryAttributes());
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
            impl.getMultivaluedUserAttributes(Collections.singletonMap("username", Util.list("seed")));
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
            impl.getMultivaluedUserAttributes(Collections.singletonMap("username", Util.list("seed")));
            fail("IllegalStateException should have been thrown with no context configured");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "uid";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setLdapAttributesToPortalAttributes(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.contextSource);
        
        impl.setQuery("(uid={0})");
        
        impl.setQueryAttributes(queryAttrList);
        
        return impl;
    }
}