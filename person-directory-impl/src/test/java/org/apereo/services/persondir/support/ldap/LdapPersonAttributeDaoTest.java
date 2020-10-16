/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support.ldap;

import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifs;
import org.apache.directory.server.core.annotations.ContextEntry;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreateIndex;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.fail;


/**
 * Testcase for LdapPersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 */
@RunWith(FrameworkRunner.class)
@CreateDS(name = "person-directory",
    partitions =
        {
            @CreatePartition(
                name = "example",
                suffix = "dc=example,dc=com",
                contextEntry = @ContextEntry(
                    entryLdif =
                        "dn: dc=example,dc=com\n" +
                            "dc: example\n" +
                            "objectClass: top\n" +
                            "objectClass: domain\n\n"),
                indexes =
                    {
                        @CreateIndex(attribute = "objectClass"),
                        @CreateIndex(attribute = "dc"),
                        @CreateIndex(attribute = "ou")
                    })
        }
)
@ApplyLdifs({
    "dn: ou=people,dc=example,dc=com",
    "objectClass: organizationalUnit",
    "objectClass: top",
    "ou: people",
    "description: Contains entries which describe people",
    "",
    "dn: cn=Eric Dalquist,ou=people,dc=example,dc=com",
    "objectclass: person",
    "objectclass: organizationalPerson",
    "objectclass: inetOrgPerson",
    "objectclass: top",
    "cn: Eric Dalquist",
    "description: uPortal Developer",
    "givenName: Eric",
    "sn: Dalquist",
    "uid: edalquist",
    "mail: eric.dalquist@example.com",
    "userpassword: {SHA}nU4eI71bcnBGqeO0t9tXvY1u5oQ=",
    "",
    "dn: cn=Jim Johnson,ou=people,dc=example,dc=com",
    "objectclass: person",
    "objectclass: organizationalPerson",
    "objectclass: inetOrgPerson",
    "objectclass: top",
    "cn: Jim Johnson",
    "description: uPortal Lackey",
    "givenname: Jim",
    "sn: Johnson",
    "uid: jjohnson",
    "mail: jim.johnson@example.com",
    "userpassword: {SHA}nU4eI71bcnBGqeO0t9tXvY1u5aQ=",
    "",
})
@CreateLdapServer(
    transports =
        {
            @CreateTransport(protocol = "LDAP", port = 10200)
        },
    allowAnonymousAccess = true)
public class LdapPersonAttributeDaoTest extends AbstractLdapTestUnit {

    private static ContextSource contextSource;
    /**
     * Create a Spring-LDAP ContextSource for the in-memory LDAP server
     */
    @BeforeClass
    public static void initContextSource() {
        var ctxSrc = new LdapContextSource();
        ctxSrc.setUrl("ldap://localhost:10200");
        ctxSrc.setBase("DC=example,DC=com");
        ctxSrc.setUserDn("uid=admin,ou=system");
        ctxSrc.setPassword("secret");
        ctxSrc.afterPropertiesSet(); /* ! */
        contextSource = ctxSrc;
    }

    private ContextSource getContextSource() {
        return contextSource;
    }

    @Test
    public void testNotFoundQuery() throws Exception {
        final var impl = new LdapPersonAttributeDao();

        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");

        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);

        impl.setContextSource(this.getContextSource());

        impl.setQueryAttributeMapping(Collections.singletonMap("uid", null));

        impl.afterPropertiesSet();

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("unknown"));

        try {
            final var attribs = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            assertEquals(0, attribs.size());
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test for a query with a single attribute. 
     */
    @Test
    public void testSingleAttrQuery() throws Exception {
        final var impl = new LdapPersonAttributeDao();

        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", "email");

        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);

        impl.setContextSource(this.getContextSource());

        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            var attribs = attribsSet.iterator().next();
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.getAttributes().get("email"));
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test for a query with a single attribute. 
     *
     * This testcase will cease to work on that fateful day when edalquist
     * no longer appears in Yale University LDAP.
     */
    @Test
    public void testMultipleMappings() throws Exception {
        final var impl = new LdapPersonAttributeDao();

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
            final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            var attribs = attribsSet.iterator().next();
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.getAttributes().get("email"));
            assertEquals(Util.list("eric.dalquist@example.com"), attribs.getAttributes().get("work.email"));
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    @Test
    public void testInvalidAttrMap() throws Exception {
        final var impl = new LdapPersonAttributeDao();

        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("email", "email");

        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);

        impl.setContextSource(this.getContextSource());

        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            assertNull(attribsSet.iterator().next().getAttributes().get("email"));
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    @Test
    public void testDefaultAttrMap() throws Exception {
        final var impl = new LdapPersonAttributeDao();

        final Map<String, Object> ldapAttribsToPortalAttribs = new HashMap<>();
        ldapAttribsToPortalAttribs.put("mail", null);

        impl.setResultAttributeMapping(ldapAttribsToPortalAttribs);

        impl.setContextSource(this.getContextSource());

        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "uid"));

        impl.afterPropertiesSet();

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("edalquist"));

        try {
            final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            assertEquals(Util.list("eric.dalquist@example.com"), attribsSet.iterator().next().getAttributes().get("mail"));
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    @Test
    public void testMultiAttrQuery() throws Exception {
        final var impl = new LdapPersonAttributeDao();

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
            final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            assertEquals(Util.list("eric.dalquist@example.com"), attribsSet.iterator().next().getAttributes().get("email"));
        } catch (final DataAccessResourceFailureException darfe) {
            //OK, No net connection
        }
    }

    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    @Test
    public void testInsufficientAttrQuery() throws Exception {
        final var impl = new LdapPersonAttributeDao();

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

        final var attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(attribsSet);
    }

    /**
     * Test proper reporting of declared attribute names.
     */
    @Test
    public void testAttributeNames() throws Exception {
        final var impl = new LdapPersonAttributeDao();

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

        assertEquals(expectedAttributeNames, impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
    }

    @Test
    public void testProperties() throws Exception {
        final var impl = new LdapPersonAttributeDao();

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
        assertEquals(Collections.EMPTY_SET, impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
        impl.setResultAttributeMapping(attrMap);
        assertEquals(Collections.singleton("email"), impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
    }

    /**
     * Test proper reporting of declared attribute names.
     */
    @Test
    public void testNullContext() throws Exception {
        final var impl = new LdapPersonAttributeDao();

        try {
            impl.afterPropertiesSet();
            fail("BeanCreationException should have been thrown with no context configured");
        } catch (final BeanCreationException ise) {
            //expected
        }
    }
}
