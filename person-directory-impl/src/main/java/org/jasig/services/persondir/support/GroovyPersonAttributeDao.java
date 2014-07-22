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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.jasig.services.persondir.IPersonAttributeScriptDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.core.io.Resource;

/**
 * An implementation of the {@link org.jasig.services.persondir.IPersonAttributeDao} that is able to resolve attributes
 * based on an external groovy script. Changes to the groovy script are to be auto-detected when providing a script
 * path.
 * 
 * <p>Groovy file:
 * <pre><code>
 *
import org.apache.commons.logging.Log
import org.jasig.services.persondir.IPersonAttributeScriptDao

class SampleGroovyPersonAttributeDao implements IPersonAttributeScriptDao {

    @Override
    Map<String, Object> getAttributesForUser(String uid, Log log) {
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }

    @Override
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes, Log log) {
        Map<String, List<Object>> newMap = new HashMap<>(attributes)
        newMap.put("foo", Arrays.asList(["value1", "value2"]))
        return newMap
    }

}
 * </code></pre>
 * @author Misagh Moayyed
 */
public class GroovyPersonAttributeDao extends BasePersonAttributeDao {

    private final IPersonAttributeScriptDao groovyObject;
    private long fileLastModifiedTime = 0;
    private Resource groovyScriptLocation = null;
    private GroovyClassLoader loader = null;

    private boolean caseInsensitiveUsername = false;
    
    public GroovyPersonAttributeDao(final Resource groovyScriptPath) throws Exception {
        groovyScriptLocation = groovyScriptPath;
        verifyGroovyScriptAndThrowExceptionIfNeeded();
        this.groovyObject = getScriptObject(false);
    }

    public GroovyPersonAttributeDao(IPersonAttributeScriptDao groovyObject) {
        this.groovyObject = groovyObject;
    }
    
    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }
    
    private void verifyGroovyScriptAndThrowExceptionIfNeeded() throws IOException {
        if (!groovyScriptLocation.exists()) {
            throw new RuntimeException("Groovy script cannot be found at the specified location: " + groovyScriptLocation.getFilename());
        }
        if (groovyScriptLocation.isOpen()) {
            throw new RuntimeException("Another process/application is busy with the specified groovy script");
        }
        if (!groovyScriptLocation.isReadable()) {
            throw new RuntimeException("Groovy script cannot be read");
        }
        if (groovyScriptLocation.contentLength() <= 0) {
            throw new RuntimeException("Groovy script is empty and has no content");
        }
        
    }

    private synchronized IPersonAttributeScriptDao getScriptObject(boolean forceReload) throws Exception {
        IPersonAttributeScriptDao groovyObject = this.groovyObject;
        if (groovyObject == null || forceReload) {
            System.out.println("Loading script");
            final ClassLoader parent = getClass().getClassLoader();
            loader = new GroovyClassLoader(parent);

            fileLastModifiedTime = groovyScriptLocation.lastModified();
            final Class<?> groovyClass = loader.parseClass(groovyScriptLocation.getFile());
            logger.debug("Loaded groovy class " + groovyClass.getSimpleName() + " from script " +
                    groovyScriptLocation.getFilename());

            groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();
            logger.debug("Created groovy object instance from class " +
                    groovyScriptLocation.getFilename());
        }
        return groovyObject;
    }

    /**
     * Reloads the groovy script and re-instantiates the object if the script file has changed.  Also closes the
     * previous GroovyClassLoader to insure there are no resource leaks.
     * @throws Exception
     */
    private synchronized void reloadScriptIfUpdated() throws Exception {
        System.out.println("Resource location is " + groovyScriptLocation.getFile().getAbsolutePath());
        System.out.println("modified time was " + fileLastModifiedTime);
        System.out.println("modified time is " + groovyScriptLocation.lastModified());
        if (fileLastModifiedTime > 0 && groovyScriptLocation.lastModified() != fileLastModifiedTime) {
            IOUtils.closeQuietly(loader);
            System.out.println("Reloading updated script file " + groovyScriptLocation.getFilename());
            logger.info("Reloading updated script file " + groovyScriptLocation.getFilename());
            getScriptObject(true);
            // When successful, update the last modified time.
            fileLastModifiedTime = groovyScriptLocation.lastModified();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPersonAttributes getPerson(final String uid) {
        try {
            reloadScriptIfUpdated();
            logger.debug("Executing groovy script's getAttributesForUser method");
            
            final Map<String, Object> personAttributesMap = groovyObject.getAttributesForUser(uid);
            logger.debug("Creating person attributes with the username " + uid + " and attributes " +
                     personAttributesMap);
            
            final Map<String, List<Object>> personAttributes = stuffAttributesIntoListValues(personAttributesMap);
                    
            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, personAttributes);
            }
            return new NamedPersonImpl(uid, personAttributes);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e); 
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
            reloadScriptIfUpdated();
            logger.debug("Executing groovy script's getPersonAttributesFromMultivaluedAttributes method, with parameters "
                    + attributes);

            @SuppressWarnings("unchecked")
            final Map<String, List<Object>> personAttributesMap =
                    groovyObject.getPersonAttributesFromMultivaluedAttributes(attributes);

            logger.debug("Creating person attributes: " + personAttributesMap);

            return Collections.singleton((IPersonAttributes) new AttributeNamedPersonImpl(personAttributesMap));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
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
