/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

import static junit.framework.TestCase.*;


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
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", null));
        
        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("unknown"));
        
        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertNull(attribs);
        }
        catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test for a query with a single attribute. 
     */
    public void testSingleAttrQuery() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
        }
        catch (final DataAccessResourceFailureException darfe) {
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
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        final Set<String> portalAttributes = new HashSet<>();
        portalAttributes.add("email");
        portalAttributes.add("work.email");
        ldapAttribsToPortalAttribs.put("mail", portalAttributes);
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("work.email"));
        }
        catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testInvalidAttrMap() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("email", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));
        
        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertNull(attribs.get("email"));
        }
        catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    public void testDefaultAttrMap() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", null);
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));
        
        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("mail"));
        }
        catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        impl.setContextSource(this.getContextSource());
        
        final Map<String, String> queryAttrs = new HashMap<>();
        queryAttrs.put("uid", "uid");
        queryAttrs.put("alias", "alias");
        impl.setQueryAttributeMapping(queryAttrs);

        impl.afterPropertiesSet();
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));
        queryMap.put("givenname", Util.list("Eric"));
        queryMap.put("email", Util.list("edalquist@unicon.net"));
        
        try {
            final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.get("email"));
        }
        catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }
    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        impl.setContextSource(this.getContextSource());
        
        final Map<String, String> queryAttrs = new HashMap<>();
        queryAttrs.put("uid", null);
        queryAttrs.put("alias", null);
        impl.setQueryAttributeMapping(queryAttrs);
        impl.setRequireAllQueryAttributes(true);
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));
        queryMap.put("email", Util.list("edalquist@example.net"));
        
        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test proper reporting of declared attribute names.
     */
    public void testAttributeNames() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");
        ldapAttribsToPortalAttribs.put("shirtColor", "dressShirtColor");
        
        final Set<String> surNameAttributeNames = new HashSet<>();
        surNameAttributeNames.add("surName");
        surNameAttributeNames.add("lastName");
        surNameAttributeNames.add("familyName");
        surNameAttributeNames.add("thirdName");
        ldapAttribsToPortalAttribs.put("lastName", surNameAttributeNames);
        
        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);
        
        final Set<String> expectedAttributeNames = new HashSet<>();
        expectedAttributeNames.addAll(surNameAttributeNames);
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("dressShirtColor");
        
        assertEquals(expectedAttributeNames, impl.getPossibleUserAttributeNames());
    }
    
    public void testProperties() throws Exception {
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        assertEquals("", impl.getBaseDN());
        impl.setBaseDN("BaseDN");
        assertEquals("BaseDN", impl.getBaseDN());
        impl.setBaseDN(null);
        assertEquals("", impl.getBaseDN());
        
        
        assertNull(impl.getResultAttributeMapping());
        final Map<String, Object> attrMap = new HashMap<>();
        attrMap.put("mail", "email");
        impl.setResultAttributeMapping(attrMap);
        
        final Map<String, Set<String>> expectedAttrMap = new HashMap<>();
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
        final LdapPersonAttributeDao impl = new LdapPersonAttributeDao();
        
        try {
            impl.afterPropertiesSet();
            fail("BeanCreationException should have been thrown with no context configured");
        }
        catch (final BeanCreationException ise) {
            //expected
        }
    }
}
