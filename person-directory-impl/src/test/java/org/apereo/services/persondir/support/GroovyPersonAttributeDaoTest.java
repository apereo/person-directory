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
package org.apereo.services.persondir.support;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributeScriptDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class GroovyPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

    private GroovyPersonAttributeDao dao;
    private GroovyClassLoader loader;
    private Map<String, List<Object>> items;

    @Override
    @Before
    public void setUp() {
        final ClassLoader parent = getClass().getClassLoader();
        loader = new GroovyClassLoader(parent);
        dao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDao.groovy"));

        items = new HashMap<>();
        items.put("dog", Arrays.asList(new Object[]{"barks", "eats"}));
        items.put("cat", Arrays.asList(new Object[]{"meows", "scratches"}));
    }

    private IPersonAttributeScriptDao loadGroovyClass(String filename) {
        try {
            final ClassPathResource scriptFile = new ClassPathResource(filename);
            final Class<?> groovyClass = loader.parseClass(scriptFile.getFile());

            final IPersonAttributeScriptDao groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();
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

    @Override
    @After
    public void tearDown() {
        try {
            if (loader != null) loader.close();
        } catch (IOException e) {
            logger.debug("Error closing groovy classloader");
        }
    }

    @Test
    public void testGetPerson() {
        final IPersonAttributes attrs = dao.getPerson("userid", IPersonAttributeDaoFilter.alwaysChoose());
        assertFalse(attrs.getAttributes().isEmpty());
        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");
        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributes() {
        final Set<IPersonAttributes> results = dao.getPeopleWithMultivaluedAttributes(items, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals("script did not add one attribute to passed-in attribute list",
            items.size() + 1, results.iterator().next().getAttributes().size());
    }

    @Test
    public void testGetPersonNullResult() {
        GroovyPersonAttributeDao nullResponseDao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDaoForTestingUpdates.groovy"));
        final IPersonAttributes attrs = nullResponseDao.getPerson("userid", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(attrs);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributesNullResult() {
        GroovyPersonAttributeDao nullResponseDao = new GroovyPersonAttributeDao(loadGroovyClass("SampleGroovyPersonAttributeDaoForTestingUpdates.groovy"));
        final Set<IPersonAttributes> results = nullResponseDao.getPeopleWithMultivaluedAttributes(items, IPersonAttributeDaoFilter.alwaysChoose());
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
