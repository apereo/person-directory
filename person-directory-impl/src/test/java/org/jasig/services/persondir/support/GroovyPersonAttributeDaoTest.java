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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Arrays;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

//@RunWith(JUnit4.class)
public class GroovyPersonAttributeDaoTest extends TestCase {

    @Test
    public void testGetPerson() throws Exception {
        GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(
                new FileSystemResource("src/test/resources/SampleGroovyPersonAttributeDao.groovy"));

        final IPersonAttributes attrs = dao.getPerson("userid");
        assertFalse(attrs.getAttributes().isEmpty());
        
        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");
        
        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributes() throws Exception {
        GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(
                new FileSystemResource("src/test/resources/SampleGroovyPersonAttributeDao.groovy"));
        Map<String, List<Object>> items = new HashMap<String, List<Object>>();
        items.put("dog", Arrays.asList(new String[] {"barks", "eats"}));
        items.put("cat", Arrays.asList(new String[] {"meows", "scratches"}));
        final Set<IPersonAttributes> results = dao.getPeopleWithMultivaluedAttributes(items);
        assertTrue("script did not add one attribute to passed-in attribute list",
                results.iterator().next().getAttributes().size() == items.size() + 1);
    }

    @Test
    public void testUpdatingScriptReloadsIt() throws Exception {
        FileSystemResource originalFile = new FileSystemResource("src/test/resources/SampleGroovyPersonAttributeDao.groovy");
        System.out.println(originalFile.getFile().getAbsolutePath());
        File testFile = File.createTempFile("fooprefix", "foosuffix");
        System.out.println("Temp file is at " + testFile.getAbsolutePath());
        FileUtils.copyFile(originalFile.getFile(), testFile);

        GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(new FileSystemResource(testFile));
        Map<String, List<Object>> items = new HashMap<String, List<Object>>();
        items.put("dog", Arrays.asList(new String[] {"barks", "eats"}));
        items.put("cat", Arrays.asList(new String[] {"meows", "scratches"}));
        Set<IPersonAttributes> results = dao.getPeopleWithMultivaluedAttributes(items);
        assertTrue("script did not add one attribute to passed-in attribute list",
                results.iterator().next().getAttributes().size() == items.size() + 1);
        System.out.println("Results are " + results);

        FileSystemResource updatedFile = new FileSystemResource("src/test/resources/SampleGroovyPersonAttributeDaoForTestingUpdates.groovy");
        FileUtils.copyFile(updatedFile.getFile(), testFile);
        testFile.setLastModified(new Date().getTime());
        results = dao.getPeopleWithMultivaluedAttributes(items);
        System.out.println("Results are " + results);
        assertNull("Did not run the updated script!", results);
    }

    @Test
    @Ignore // Currently fails with class cast exception for some reason
    /*
    public void testGetPersonWithClass() throws Exception {

        final ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);

        final Class<?> groovyClass = loader.parseClass("src/test/resources/SampleGroovyPersonAttributeDao.groovy");

        IPersonAttributeScriptDao groovyObject = (IPersonAttributeScriptDao) groovyClass.newInstance();

        GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(groovyObject);

        final IPersonAttributes attrs = dao.getPerson("userid");
        assertFalse(attrs.getAttributes().isEmpty());

        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");

        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }
    */

    private List<?> getAttributeAsList(final IPersonAttributes attrs, final String name) {
        return ((List<?>) attrs.getAttributes().get(name));
    }
    private Object getAttributeAsSingleValue(final IPersonAttributes attrs, final String name) {
        return getAttributeAsList(attrs, name).get(0);
    }
}
