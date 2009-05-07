/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.util.Util;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.test.AbstractDirContextTest;


/**
 * Testcase for LdapPersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class LdapPersonAttributeDaoTest extends AbstractDirContextTest {
    /* (non-Javadoc)
     * @see org.springframework.ldap.test.AbstractDirContextTest#getPartitionName()
     */
    @Override
    protected String getPartitionName() {
        return "personDirectory";
    }
    
    /* (non-Javadoc)
     * @see org.springframework.ldap.test.AbstractDirContextTest#getBaseDn()
     */
    @Override
    protected String getBaseDn() {
        return "ou=people,o=personDirectory";
    }


    /* (non-Javadoc)
     * @see org.springframework.ldap.test.AbstractDirContextTest#initializationData()
     */
    @Override
    protected Resource[] initializationData() {
        final ClassPathResource ldapPersonInfo = new ClassPathResource("/ldapPersonInfo.ldif");
        return new Resource[] { ldapPersonInfo };
    }
    
    public void testNotFoundQuery() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", null));
        
        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("unknown"));
        
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
     */
    public void testSingleAttrQuery() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test for a query with a single attribute. 
     * 
     * This testcase will cease to work on that fateful day when edalquist
     * no longer appears in Yale University LDAP.
     */
    public void testMultipleMappings() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        Set<String> portalAttributes = new HashSet<String>();
        portalAttributes.add("email");
        portalAttributes.add("work.email");
        ldapAttribsToPortalAttribs.put("mail", portalAttributes);
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("work.email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testInvalidAttrMap() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("email", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertNull(attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testDefaultAttrMap() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", null);
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("mail"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        Map<String, String> queryAttrs = new HashMap<String, String>();
        queryAttrs.put("uid", "uid");
        queryAttrs.put("alias", "alias");
        impl.setQueryAttributeMapping(queryAttrs);

        impl.afterPropertiesSet();
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));
        queryMap.put("givenname", Util.list("Eric"));
        queryMap.put("email", Util.list("edalquist@unicon.net"));
        
        try {
            Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
        }
        catch (DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<String, Object>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        impl.setContextSource(this.getContextSource());
        
        Map<String, String> queryAttrs = new HashMap<String, String>();
        queryAttrs.put("uid", null);
        queryAttrs.put("alias", null);
        impl.setQueryAttributeMapping(queryAttrs);
        impl.setRequireAllQueryAttributes(true);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("edalquist"));
        queryMap.put("email", Util.list("edalquist@example.net"));
        
        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testAttributeNames() throws Exception {
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
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        Set<String> expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.addAll(surNameAttributeNames);
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("dressShirtColor");
        
        assertEquals(expectedAttributeNames, impl.getPossibleUserAttributeNames());
    }
    
    public void testProperties() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        assertEquals("", impl.getBaseDN());
        impl.setBaseDN("BaseDN");
        assertEquals("BaseDN", impl.getBaseDN());
        impl.setBaseDN(null);
        assertEquals("", impl.getBaseDN());
        
        
        assertNull(impl.getResultAttributeMapping());
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("mail", "email");
        impl.setResultAttributeMapping(attrMap);
        
        Map<String, Set<String>> expectedAttrMap = new HashMap<String, Set<String>>();
        expectedAttrMap.put("mail", Collections.singleton("email"));
        assertEquals(expectedAttrMap, impl.getResultAttributeMapping());
        
        
        assertNull(impl.getContextSource());
        impl.setContextSource(this.getContextSource());
        assertEquals(this.getContextSource(), impl.getContextSource());
        
        
        impl.setResultAttributeMapping(null);
        assertEquals(Collections.EMPTY_SET, impl.getPossibleUserAttributeNames());
        impl.setResultAttributeMapping(attrMap);
        assertEquals(Collections.singleton("email"), impl.getPossibleUserAttributeNames());
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testNullContext() throws Exception {
        LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        try {
            impl.afterPropertiesSet();
            fail("BeanCreationException should have been thrown with no context configured");
        }
        catch (BeanCreationException ise) {
            //expected
        }
    }
}