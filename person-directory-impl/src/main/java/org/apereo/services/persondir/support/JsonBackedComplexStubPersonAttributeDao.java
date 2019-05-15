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
package org.apereo.services.persondir.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A convenient wrapper around <code>ComplexStubPersonAttributeDao</code> that reads the configuration for its <i>backingMap</i>
 * property from an external JSON configuration resource. 
 *
 * <p>Sample JSON file:
 * <pre><code>
 * {
 "u1":{
 "firstName":["Json1"],
 "lastName":["One"],
 "additionalAttribute":["here I AM!!!"],
 "additionalAttribute2":["attr2"],
 "eduPersonAffiliation":["alumni", "staff"]
 },
 "u2":{
 "firstName":["Json2"],
 "lastName":["Two"],
 "eduPersonAffiliation":["employee", "student"]
 },
 "u3":{
 "firstName":["Json3"],
 "lastName":["Three"],
 "eduPersonAffiliation":["alumni", "student", "employee", "some other attr"]
 }
 }
 * </code></pre>
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 */
public class JsonBackedComplexStubPersonAttributeDao extends ComplexStubPersonAttributeDao implements Closeable, DisposableBean {

    /**
     * A configuration file containing JSON representation of the stub person attributes. REQUIRED.
     */
    private final Resource personAttributesConfigFile;

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper().findAndRegisterModules();

    private final Object synchronizationMonitor = new Object();

    private Closeable resourceWatcherService;

    public JsonBackedComplexStubPersonAttributeDao(final Resource personAttributesConfigFile) {
        this.personAttributesConfigFile = personAttributesConfigFile;
        this.resourceWatcherService = null;
    }

    public JsonBackedComplexStubPersonAttributeDao(final Resource personAttributesConfigFile, final Closeable resourceWatcherService) {
        this.personAttributesConfigFile = personAttributesConfigFile;
        this.resourceWatcherService = resourceWatcherService;
    }

    public void setResourceWatcherService(final Closeable resourceWatcherService) {
        this.resourceWatcherService = resourceWatcherService;
    }

    /**
     * Init method un-marshals JSON representation of the person attributes.
     *
     * @throws IOException invalid config file URI
     */
    public void init() throws IOException {
        /* If we get to this point, the JSON file is well-formed, but its structure does not map into
         * PersonDir backingMap generic type - fail fast.
         */
        try {
            unmarshalAndSetBackingMap();
        } catch (final Exception ex) {
            throw new BeanCreationException(String.format("The semantic structure of the person attributes"
                + "JSON config is not correct. Please fix it in this resource: [%s]", this.personAttributesConfigFile), ex);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.resourceWatcherService != null) {
            resourceWatcherService.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void unmarshalAndSetBackingMap() throws IOException {
        logger.info("Un-marshaling person attributes from the config file {}", this.personAttributesConfigFile);
        final Map<String, Map<String, List<Object>>> backingMap = this.jacksonObjectMapper.readValue(
            this.personAttributesConfigFile.getInputStream(), Map.class);
        logger.debug("Person attributes have been successfully read into the map ");
        synchronized (this.synchronizationMonitor) {
            super.setBackingMap(backingMap);
        }
    }

    @Override
    public void destroy() throws Exception {
        close();
    }
}
