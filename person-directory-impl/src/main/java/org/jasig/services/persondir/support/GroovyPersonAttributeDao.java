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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jasig.services.persondir.IPersonAttributeScriptDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.core.io.Resource;

/**
 * An implementation of the {@link org.jasig.services.persondir.IPersonAttributeDao} that is able to resolve attributes
 * based on an external groovy script. Changes to the groovy script are to be auto-detected.
 * 
 * <p>Groovy file:
 * <pre><code>
 *
import org.apache.commons.logging.Log
import org.jasig.services.persondir.IPersonAttributeScriptDao

class SampleGroovyPersonAttributeDao implements IPersonAttributeScriptDao {

    @Override
    Map<String, Object> getAttributesForUser(String uid, Log log) {
        log.debug("[Groovy script " + this.class.getSimpleName() + "] The received uid is " + uid)
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }

    @Override
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes, Log log) {
        log.debug("[Groovy script " + this.class.getSimpleName() + "] Input map size is " + attributes.size())
        Map<String, List<Object>> newMap = new HashMap<>(attributes)
        newMap.put("foo", Arrays.asList(["value1", "value2"]))
        return newMap
    }

}
 * </code></pre>
 * @author Misagh Moayyed
 */
public class GroovyPersonAttributeDao extends BasePersonAttributeDao {

    private final Resource groovyScriptResource;
    private final IPersonAttributeScriptDao groovyScriptClass;
    private GroovyClassLoader loader = null;

    private boolean caseInsensitiveUsername = false;
    
    public GroovyPersonAttributeDao(final Resource groovyScriptPath) throws IOException {
        verifyGroovyScriptAndThrowExceptionIfNeeded(groovyScriptPath);
        this.groovyScriptResource = groovyScriptPath;
        this.groovyScriptClass = null;
    }

    public GroovyPersonAttributeDao(IPersonAttributeScriptDao scriptClass) {
        this.groovyScriptResource = null;
        this.groovyScriptClass = scriptClass;
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

    private IPersonAttributeScriptDao getScriptObject() throws Exception {
        IPersonAttributeScriptDao groovyObject = groovyScriptClass;
        if (groovyObject == null) {
            final ClassLoader parent = getClass().getClassLoader();
            loader = new GroovyClassLoader(parent);

            final Class<?> groovyClass = loader.parseClass(this.groovyScriptResource.getFile());
            logger.debug("Loaded groovy class " + groovyClass.getSimpleName() + " from script " +
                    this.groovyScriptResource.getFilename());

            groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();
            logger.debug("Created groovy object instance from class " +
                    this.groovyScriptResource.getFilename());
        }
        return groovyObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPersonAttributes getPerson(final String uid) {
        try {
            IPersonAttributeScriptDao groovyObject = getScriptObject();
            logger.debug("Executing groovy script's getAttributesForUser method");
            
            final Map<String, Object> personAttributesMap = groovyObject.getAttributesForUser(uid, logger);
            logger.debug("Creating person attributes with the username " + uid + " and attributes " +
                     personAttributesMap);
            
            final Map<String, List<Object>> personAttributes = stuffAttributesIntoListValues(personAttributesMap);
                    
            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, personAttributes);
            }
            return new NamedPersonImpl(uid, personAttributes);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e); 
        } finally {
            // Do we really need to close the class loader every time?  Would we potentially incur memory issues
            // if we just left it around?
            IOUtils.closeQuietly(loader);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
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
    public Set<IPersonAttributes> getPeople(Map<String, Object> attributes) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoListValues(attributes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> attributes) {
        try {
            IPersonAttributeScriptDao groovyObject = getScriptObject();
            logger.debug("Executing groovy script's getPersonAttributesFromMultivaluedAttributes method, with parameters "
                    + attributes);

            @SuppressWarnings("unchecked")
            final Map<String, List<Object>> personAttributesMap =
                    groovyObject.getPersonAttributesFromMultivaluedAttributes(attributes, logger);

            logger.debug("Creating person attributes: " + personAttributesMap);

            return Collections.singleton((IPersonAttributes) new AttributeNamedPersonImpl(personAttributesMap));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            // Do we really need to close the class loader every time?  Would we potentially incur memory issues
            // if we just left it around?
            IOUtils.closeQuietly(loader);
        }
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
