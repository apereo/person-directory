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
package org.jasig.services.persondir.support;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * An implementation of the {@link org.jasig.services.persondir.IPersonAttributeDao} that is able to resolve attributes
 * based on an external groovy script. Changes to the groovy script are to be auto-detected.
 * 
 * <p>Groovy file:
 * <pre><code>
 *
import java.util.List;
import java.util.Map;

class SampleGroovyPersonAttributeDao {
    def Map<String, List<Object>> run(final Object... args) {

        def uid = args[0]
        def logger = args[1];

        logger.debug("[{}]: The received uid is {}", this.class.simpleName, uid)
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }
}
 * </code></pre>
 * @author Misagh Moayyed
 */
public class GroovyPersonAttributeDao extends BasePersonAttributeDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final Resource groovyScriptResource;
    
    private String groovyScriptExecutingMethodName = "run";
    private boolean caseInsensitiveUsername = false;
    
    public GroovyPersonAttributeDao(final Resource groovyScriptPath) throws IOException {
        verifyGroovyScriptAndThrowExceptionIfNeeded(groovyScriptPath);
        this.groovyScriptResource = groovyScriptPath;
    }
    
    public void setGroovyScriptExecutingMethodName(final String methodName) {
        this.groovyScriptExecutingMethodName = methodName;
    }
    
    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }
    
    private void verifyGroovyScriptAndThrowExceptionIfNeeded(final Resource groovyScriptPath) throws IOException {
        if (!groovyScriptPath.exists()) {
            throw new RuntimeException("Groovy script cannot be found at the specifiied location: " + groovyScriptPath.getFilename());
        }
        if (groovyScriptPath.isOpen()) {
            throw new RuntimeException("Another process/application is busy with the specified groovy script");
        }
        if (!groovyScriptPath.isReadable()) {
            throw new RuntimeException("Groovy script cannot be read");
        }
        if (groovyScriptPath.contentLength() <= 0) {
            throw new RuntimeException("Groovy script is empty and has no content");
        }
        
    }

    @Override
    public IPersonAttributes getPerson(final String uid) {
        GroovyClassLoader loader = null;
        try {
            final ClassLoader parent = getClass().getClassLoader();
            loader = new GroovyClassLoader(parent);
            
            final Class<?> groovyClass = loader.parseClass(this.groovyScriptResource.getFile());
            logger.debug("Loaded groovy class {} from script {}", groovyClass.getSimpleName(),
                    this.groovyScriptResource.getFilename());
            
            final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            logger.debug("Created groovy object instance from class {}",
                    this.groovyScriptResource.getFilename());
            
            final Object[] args = {uid, logger};
            logger.debug("Executing groovy script's {} method, with parameters {}",
                    this.groovyScriptExecutingMethodName, args);
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> personAttributesMap = (Map<String, Object>)
                groovyObject.invokeMethod(this.groovyScriptExecutingMethodName, args);
            
            logger.debug("Creating person attributes with the username {} and attributes {}",
                    uid, personAttributesMap);
            
            final Map<String, List<Object>> personAttributes = stuffAttributesIntoListValues(personAttributesMap);
                    
            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, personAttributes);
            }
            return new NamedPersonImpl(uid, personAttributes);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e); 
        } finally {
            IOUtils.closeQuietly(loader);
        }
        return null;
    }

    private Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, Object> personAttributesMap) {
        final Map<String, List<Object>> personAttributes = new HashMap<String, List<Object>>();
        
        for (final String key : personAttributesMap.keySet()) {
            final Object value = personAttributesMap.get(key);
            if (value instanceof List) {
                personAttributes.put(key, (List) value); 
            } else {
                personAttributes.put(key, Arrays.asList(value));
            }
        }
        return personAttributes;
    }

    @Override
    public Set<IPersonAttributes> getPeople(Map<String, Object> query) {
        return null;
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        return null;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }

    @Override
    public Set<String> getAvailableQueryAttributes() {
        return null;
    }
}
