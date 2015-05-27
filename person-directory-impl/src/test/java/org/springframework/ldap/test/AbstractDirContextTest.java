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

package org.springframework.ldap.test;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.server.unit.AbstractServerTest;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Base LDAP server test for testing code that uses a Spring-LDAP ContextSource
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public abstract class AbstractDirContextTest extends AbstractServerTest {
    private ContextSource contextSource;
    
    /**
     * Initialize the server.
     */
    @Override
    public final void setUp() throws Exception {
        final String partitionName = this.getPartitionName();
        
        // Add partition 'sevenSeas'
        final MutablePartitionConfiguration partitionConfiguration = new MutablePartitionConfiguration();
        partitionConfiguration.setName(partitionName);
        partitionConfiguration.setSuffix("o=" + partitionName);

        // Create some indices
        final Set<Object> indexedAttrs = new HashSet<Object>();
        indexedAttrs.add("objectClass");
        indexedAttrs.add("o");
        partitionConfiguration.setIndexedAttributes(indexedAttrs);

        // Create a first entry associated to the partition
        final Attributes attrs = new BasicAttributes(true);

        // First, the objectClass attribute
        final Attribute objectClassAttr = new BasicAttribute("objectClass");
        objectClassAttr.add("top");
        objectClassAttr.add("organization");
        attrs.put(objectClassAttr);

        // The the 'Organization' attribute
        final Attribute orgAttribute = new BasicAttribute("o");
        orgAttribute.add(partitionName);
        attrs.put(orgAttribute);

        // Associate this entry to the partition
        partitionConfiguration.setContextEntry(attrs);

        // As we can create more than one partition, we must store
        // each created partition in a Set before initialization
        final Set<MutablePartitionConfiguration> partitionConfigurations = new HashSet<MutablePartitionConfiguration>();
        partitionConfigurations.add(partitionConfiguration);

        this.configuration.setContextPartitionConfigurations(partitionConfigurations);

        // Create a working directory
        final File workingDirectory = File.createTempFile(this.getClass().getName() + ".", ".apacheds-server-work");
        workingDirectory.delete();
        workingDirectory.deleteOnExit();
        this.configuration.setWorkingDirectory(workingDirectory);

        // Now, let's call the upper class which is responsible for the
        // partitions creation
        super.setUp();

        // Load initializationg ldif data
        final Resource[] initializationData = this.initializationData();
        for (final Resource data : initializationData) {
            final InputStream dataStream = data.getInputStream();
            this.importLdif(dataStream);
        }

        //Setup the ContextSource
        final DirContext context = this.createContext();
        this.contextSource = new SingleContextSource(context);
        
        this.contextSource = new LdapContextSource();
        ((LdapContextSource) this.contextSource).setUrl("ldap://localhost:" + this.port);
        ((LdapContextSource) this.contextSource).setBase(this.getBaseDn());
        ((LdapContextSource) this.contextSource).afterPropertiesSet(); 
        
        this.internalSetUp();
    }

    /**
     * Shutdown the server.
     */
    @Override
    public final void tearDown() throws Exception {
        this.internaltearDown();
        
        this.contextSource = null;
        
        super.tearDown();
    }
    
    /**
     * Create a context pointing to the partition
     */
    @SuppressWarnings("unchecked")
    protected final DirContext createContext() throws NamingException {
        // Create a environment container
        final Hashtable<Object, Object> env = new Hashtable<Object, Object>(configuration.toJndiEnvironment());
        
        final String partitionName = this.getPartitionName();

        // Create a new context pointing to the partition
        env.put(Context.PROVIDER_URL, "o=" + partitionName);
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory");

        // Let's open a connection on this partition
        final InitialContext initialContext = new InitialContext(env);

        // We should be able to read it
        final DirContext appRoot = (DirContext) initialContext.lookup("");
        assertNotNull(appRoot);

        return appRoot;
    }
    
    /**
     * @return A Spring-LDAP ContextSource for the in-memory LDAP server
     */
    protected final ContextSource getContextSource() {
        return this.contextSource;
    }
    
    /**
     * Can be overridden for local setUp logic
     */
    protected void internalSetUp() throws Exception {
    }

    /**
     * Can be overridden for local tearDown logic
     */
    protected void internaltearDown() throws Exception {
    }
    
    /**
     * @return The root name for the in-memory LDAP partition
     */
    protected abstract String getPartitionName();
    
    /**
     * @return The baseDn for the in-memory LDAP context
     */
    protected abstract String getBaseDn();
    
    /**
     * @return Resources pointing to ldif content to import into the in-memory LDAP server during setUp
     */
    protected abstract Resource[] initializationData();
    
    /**
     * Tests that the partition is created correctly
     */
    public final void testPartition() throws NamingException {
        final DirContext appRoot = this.createContext();
        final String partitionName = this.getPartitionName();
        
        // Let's get the entry associated to the top level
        final Attributes attributes = appRoot.getAttributes("");
        assertNotNull(attributes);
        assertEquals(partitionName, attributes.get("o").get());

        final Attribute attribute = attributes.get("objectClass");
        assertNotNull(attribute);
        assertTrue(attribute.contains("top"));
        assertTrue(attribute.contains("organization"));
        // Ok, everything is fine
    }
}
