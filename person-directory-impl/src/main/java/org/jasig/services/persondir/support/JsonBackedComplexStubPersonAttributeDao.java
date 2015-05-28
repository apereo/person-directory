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
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

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
public class JsonBackedComplexStubPersonAttributeDao extends ComplexStubPersonAttributeDao {

    /**
     * A configuration file containing JSON representation of the stub person attributes. REQUIRED.
     */
    private final Resource personAttributesConfigFile;

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    private final Object synchronizationMonitor = new Object();

    public JsonBackedComplexStubPersonAttributeDao(final Resource personAttributesConfigFile) {
        this.personAttributesConfigFile = personAttributesConfigFile;
    }

    /**
     * Init method un-marshals JSON representation of the person attributes.
     */
    public void init() throws Exception {
        /* If we get to this point, the JSON file is well-formed, but its structure does not map into
         * PersonDir backingMap generic type - fail fast.
         */
        try {
            unmarshalAndSetBackingMap();
        } catch (final ClassCastException ex) {
            throw new BeanCreationException(String.format("The semantic structure of the person attributes"
                    + "JSON config is not correct. Please fix it in this resource: [%s]", this.personAttributesConfigFile.getURI()));
        }
    }

    @SuppressWarnings("unchecked")
    private void unmarshalAndSetBackingMap() throws Exception {
        logger.info("Un-marshaling person attributes from the config file " + this.personAttributesConfigFile.getFile());
        final Map<String, Map<String, List<Object>>> backingMap = this.jacksonObjectMapper.readValue(
                this.personAttributesConfigFile.getFile(), Map.class);
        logger.debug("Person attributes have been successfully read into the map ");
        synchronized (this.synchronizationMonitor) {
            super.setBackingMap(backingMap);
        }
    }

}
