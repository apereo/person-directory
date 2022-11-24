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

import groovy.lang.GroovyClassLoader;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeScriptDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GroovyPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

    private GroovyPersonAttributeDao dao;

    private GroovyClassLoader loader;

    private Map<String, List<Object>> items;

    @BeforeEach
    public void setUp() {
        var parent = getClass().getClassLoader();
        loader = new GroovyClassLoader(parent);
        dao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDao.groovy"));

        items = new HashMap<>();
        items.put("dog", Arrays.asList(new Object[]{"barks", "eats"}));
        items.put("cat", Arrays.asList(new Object[]{"meows", "scratches"}));
    }

    private IPersonAttributeScriptDao loadGroovyClass(String filename) {
        try {
            var scriptFile = new ClassPathResource(filename);
            final Class<?> groovyClass = loader.parseClass(scriptFile.getFile());
            var groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();
            return groovyObject;
        } catch (IOException e) {
            logger.error("Unable to load groovy file {} ", filename, e);
        } catch (InstantiationException e) {
            logger.error("Unable to instantiate groovy class specified in file {}", filename, e);
        } catch (IllegalAccessException e) {
            logger.error("Unable to instantiate groovy class specified in file {}", filename, e);
        }
        fail("Unable to instantiate groovy class from file " + filename);
        return null;
    }

    @AfterEach
    public void tearDown() {
        try {
            if (loader != null) {
                loader.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing groovy classloader");
        }
    }

    @Test
    public void testGetPerson() {
        var attrs = dao.getPerson("userid");
        assertFalse(attrs.getAttributes().isEmpty());
        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");
        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributes() {
        var results = dao.getPeopleWithMultivaluedAttributes(items);
        assertEquals(items.size() + 1, results.iterator().next().getAttributes().size());
    }

    @Test
    public void testGetPersonNullResult() {
        var nullResponseDao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDaoForTestingUpdates.groovy"));
        var attrs = nullResponseDao.getPerson("userid");
        assertNull(attrs);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributesNullResult() {
        var nullResponseDao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDaoForTestingUpdates.groovy"));
        var results = nullResponseDao.getPeopleWithMultivaluedAttributes(items);
        assertNull(results);
    }

    private List<?> getAttributeAsList(final IPersonAttributes attrs, final String name) {
        return attrs.getAttributes().get(name);
    }

    private Object getAttributeAsSingleValue(final IPersonAttributes attrs, final String name) {
        return getAttributeAsList(attrs, name).get(0);
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }
}
