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
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.PooledConnectionFactory;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.directory.SearchControls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static void initontextSource() {
        LdapContextSource ctxSrc = new LdapContextSource();
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
        final String[] urls = ((LdapContextSource) this.getContextSource()).getUrls();
        final PooledConnectionFactory factory = new PooledConnectionFactory(urls[0]);
        factory.initialize();

        final Map<String, String> map = new HashMap<>();
        map.put("cn", "commonName");
        map.put("mail", "displayName");
        map.put("givenName", "givenName");

        final SearchControls ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        final LdaptivePersonAttributeDao dao = new LdaptivePersonAttributeDao();
        dao.setConnectionFactory(factory);
        dao.setBaseDN("ou=people,dc=example,dc=com");
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("uid={0}");
        dao.setResultAttributeMapping(map);

        IPersonAttributes person = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
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
        Set<IPersonAttributes> people = dao.getPeople(Map.of("username", List.of("edalquist")));
        person = people.iterator().next();
        assertTrue(person.getAttributes().size() > 0);
        assertNotNull(person.getAttributeValue("commonName"));
        assertNotNull(person.getAttributeValue("displayName"));
        assertNotNull(person.getAttributeValue("givenName"));
    }
}

