package org.jasig.services.persondir.support;

import static org.junit.Assert.*;

import org.jasig.services.persondir.support.TomlLdapPersonAttributeDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@RunWith(JUnit4.class)
public class TomlLdapPersonAttributeDaoTests {

    @Test
    public void testTomlConfigFileStuffedIntoDao() throws Exception {
        final Resource tomlConfigFile = new FileSystemResource("src/test/resources/ldap.toml");
        final TomlLdapPersonAttributeDao dao = new TomlLdapPersonAttributeDao(tomlConfigFile);
        
        assertNotNull(dao.getBaseDN());
        assertNotNull(dao.getQueryAttributeMapping());
        assertNotNull(dao.getQueryType());
        assertNotNull(dao.getResultAttributeMapping());
        assertNotNull(dao.getContextSource());
    }
}
