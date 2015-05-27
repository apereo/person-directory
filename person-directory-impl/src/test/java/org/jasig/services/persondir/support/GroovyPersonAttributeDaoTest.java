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
import org.apache.commons.io.IOUtils;
import org.jasig.services.persondir.AbstractPersonAttributeDaoTest;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributeScriptDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.util.*;

@RunWith(JUnit4.class)
public class GroovyPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

    private GroovyPersonAttributeDao dao;
    private GroovyClassLoader loader;

    @Before
    public void setUp() throws Exception {
        final ClassLoader parent = getClass().getClassLoader();
        loader = new GroovyClassLoader(parent);

        final ClassPathResource scriptFile = new ClassPathResource("SampleGroovyPersonAttributeDao.groovy");
        final Class<?> groovyClass = loader.parseClass(scriptFile.getFile());

        final IPersonAttributeScriptDao groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();

        dao = new GroovyPersonAttributeDao(groovyObject);
    }

    @After
    public void tearDown() {
        IOUtils.closeQuietly(loader);
    }

    @Test
    public void testGetPerson() throws Exception {
        final IPersonAttributes attrs = dao.getPerson("userid");
        assertFalse(attrs.getAttributes().isEmpty());
        
        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");
        
        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributes() throws Exception {
        final Map<String, List<Object>> items = new HashMap<>();
        items.put("dog", Arrays.asList(new Object[]{"barks", "eats"}));
        items.put("cat", Arrays.asList(new Object[]{"meows", "scratches"}));
        final Set<IPersonAttributes> results = dao.getPeopleWithMultivaluedAttributes(items);
        assertTrue("script did not add one attribute to passed-in attribute list",
                results.iterator().next().getAttributes().size() == items.size() + 1);
    }

    private List<?> getAttributeAsList(final IPersonAttributes attrs, final String name) {
        return ((List<?>) attrs.getAttributes().get(name));
    }
    private Object getAttributeAsSingleValue(final IPersonAttributes attrs, final String name) {
        return getAttributeAsList(attrs, name).get(0);
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }
}
