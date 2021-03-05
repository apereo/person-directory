/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.PooledConnectionFactory;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.directory.SearchControls;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * The {@link LdaptivePersonAttributeDaoTest} is responsible for
 * test cases for LDAP DAO based on Ldaptive lib.
 *
 * @author Misagh Moayyed
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
            @CreateTransport(protocol = "LDAP", port = 10201)
        },
    allowAnonymousAccess = true)
public class LdaptivePersonAttributeDaoTest extends AbstractLdapTestUnit {

    private static ContextSource contextSource;
    /**
     * Create a Spring-LDAP ContextSource for the in-memory LDAP server
     */
    @BeforeClass
    public static void initContextSource() {
        var ctxSrc = new LdapContextSource();
        ctxSrc.setUrl("ldap://localhost:10201");
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
    public void testVerifyGetPerson() {
        var urls = ((LdapContextSource) this.getContextSource()).getUrls();
        var factory = new PooledConnectionFactory(urls[0]);
        factory.initialize();

        final Map<String, String> resultAttributeMap = new HashMap<>();
        resultAttributeMap.put("cn", "commonName");
        resultAttributeMap.put("mail", "displayName");
        resultAttributeMap.put("givenName", "givenName");

        var ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        var dao = new LdaptivePersonAttributeDao();
        dao.setConnectionFactory(factory);
        dao.setBaseDN("ou=people,dc=example,dc=com");
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("uid={0}");
        dao.setResultAttributeMapping(resultAttributeMap);

        var person = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        assertTrue(person.getAttributes().size() > 0);
        assertNotNull(person.getAttributeValue("commonName"));
        assertNotNull(person.getAttributeValue("displayName"));
        assertNotNull(person.getAttributeValue("givenName"));

        dao.setSearchFilter("uid={user}");
        person = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        assertTrue(person.getAttributes().size() > 0);
        assertNotNull(person.getAttributeValue("commonName"));
        assertNotNull(person.getAttributeValue("displayName"));
        assertNotNull(person.getAttributeValue("givenName"));

        dao.setSearchFilter("uid={username}");
        var people = dao.getPeople(Map.of("username", List.of("edalquist")));
        person = people.iterator().next();
        assertTrue(person.getAttributes().size() > 0);
        assertNotNull(person.getAttributeValue("commonName"));
        assertNotNull(person.getAttributeValue("displayName"));
        assertNotNull(person.getAttributeValue("givenName"));
    }


    /**
     * Show using multiple attributes to get person with QueryAttributeMapping set on DAO.
     */
    @Test
    public void testVerifyGetPeopleWithQueryAttributeMapping() {
        var urls = ((LdapContextSource) this.getContextSource()).getUrls();
        var factory = new PooledConnectionFactory(urls[0]);
        factory.initialize();

        final Map<String, String> resultAttributeMap = new HashMap<>();
        resultAttributeMap.put("cn", "commonName");
        resultAttributeMap.put("mail", "displayName");
        resultAttributeMap.put("givenName", "givenName");

        var ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        var dao = new LdaptivePersonAttributeDao();
        Map attributeMapping = new HashMap();
        attributeMapping.put("username","thename");
        dao.setQueryAttributeMapping(attributeMapping);
        dao.setConnectionFactory(factory);
        dao.setBaseDN("ou=people,dc=example,dc=com");
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("uid={thename}");
        dao.setResultAttributeMapping(resultAttributeMap);

        final Map<String, Object> query = new HashMap<>();
        query.put("sn", Collections.singletonList("Johnson"));
        query.put("username", Collections.singletonList("jjohnson"));

        var people = dao.getPeople(query);
        assertTrue(people.iterator().hasNext());
        var person = people.iterator().next();
        assertEquals("Jim", person.getAttributeValue("givenName"));
    }

    /**
     * Show using multiple attributes to get person without QueryAttributeMapping set on DAO.
     * This demonstrates that as long as the filter doesn't use <code>{0}</code> or <code>{user}</code>
     * then it can work with multiple attributes and no username specifier. This allows for complex
     * multi-attribute search filters.
     */
    @Test
    public void testVerifyGetPeopleWithoutQueryAttributeMapping() {
        var urls = ((LdapContextSource) this.getContextSource()).getUrls();
        var factory = new PooledConnectionFactory(urls[0]);
        factory.initialize();

        final Map<String, String> resultAttributeMap = new HashMap<>();
        resultAttributeMap.put("cn", "commonName");
        resultAttributeMap.put("mail", "displayName");
        resultAttributeMap.put("givenName", "givenName");

        var ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        var dao = new LdaptivePersonAttributeDao();
        dao.setConnectionFactory(factory);
        dao.setBaseDN("ou=people,dc=example,dc=com");
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("uid={thename}");
        dao.setResultAttributeMapping(resultAttributeMap);

        final Map<String, Object> query = new HashMap<>();
        query.put("sn", Collections.singletonList("Johnson"));
        query.put("thename", Collections.singletonList("jjohnson"));

        var people = dao.getPeople(query);
        assertTrue(people.iterator().hasNext());
        var person = people.iterator().next();
        assertEquals("Jim", person.getAttributeValue("givenName"));
    }

    /**
     * Show using multiple attributes to get person with multiple arguments in search filter.
     */
    @Test
    public void testVerifyGetPeopleWithMultiArgSearchFilter() {
        var urls = ((LdapContextSource) this.getContextSource()).getUrls();
        var factory = new PooledConnectionFactory(urls[0]);
        factory.initialize();

        final Map<String, String> resultAttributeMap = new HashMap<>();
        resultAttributeMap.put("cn", "commonName");
        resultAttributeMap.put("mail", "displayName");
        resultAttributeMap.put("givenName", "givenName");

        var ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        var dao = new LdaptivePersonAttributeDao();
        dao.setConnectionFactory(factory);
        dao.setBaseDN("ou=people,dc=example,dc=com");
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("(&(uid={thename})(sn={sn})");
        dao.setResultAttributeMapping(resultAttributeMap);

        final Map<String, Object> query = new HashMap<>();
        query.put("sn", Collections.singletonList("Johnson"));
        query.put("thename", Collections.singletonList("jjohnson"));

        var people = dao.getPeople(query);
        assertTrue(people.iterator().hasNext());
        var person = people.iterator().next();
        assertEquals("Jim", person.getAttributeValue("givenName"));
    }
}

