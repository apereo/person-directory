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

import org.apereo.services.persondir.IPersonAttributes;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.AbstractDirContextTest;

import javax.naming.directory.SearchControls;
import java.util.HashMap;
import java.util.Map;


/**
 * The {@link LdaptivePersonAttributeDaoTest} is responsible for
 * test cases for LDAP DAO based on Ldaptive lib.
 *
 * @author Misagh Moayyed
 */
public class LdaptivePersonAttributeDaoTest extends AbstractDirContextTest {
    @Autowired
    private LdapPersonAttributeDao attributeDao;

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
        return new Resource[]{ldapPersonInfo};
    }

    public void testVerifyGetPerson() throws Exception {

        final String[] urls = ((LdapContextSource) this.getContextSource()).getUrls();
        final DefaultConnectionFactory df = new DefaultConnectionFactory(urls[0]);
        final BlockingConnectionPool cp = new BlockingConnectionPool(df);
        cp.initialize();
        final PooledConnectionFactory factory = new PooledConnectionFactory(cp);

        final Map<String, String> map = new HashMap<>();
        map.put("cn", "commonName");
        map.put("mail", "displayName");
        map.put("givenName", "givenName");

        final SearchControls ctrs = new SearchControls();
        ctrs.setSearchScope(1);
        ctrs.setCountLimit(2);

        final LdaptivePersonAttributeDao dao = new LdaptivePersonAttributeDao();
        dao.setConnectionFactory(factory);
        dao.setBaseDN(getBaseDn());
        dao.setSearchControls(ctrs);
        dao.setSearchFilter("uid={0}");
        dao.setResultAttributeMapping(map);

        final IPersonAttributes person = dao.getPerson("edalquist");
        assertTrue(person.getAttributes().size() > 0);
        assertNotNull(person.getAttributeValue("commonName"));
        assertNotNull(person.getAttributeValue("displayName"));
        assertNotNull(person.getAttributeValue("givenName"));
    }
}

