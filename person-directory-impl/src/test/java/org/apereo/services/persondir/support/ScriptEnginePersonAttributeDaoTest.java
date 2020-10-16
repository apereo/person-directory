package org.apereo.services.persondir.support;

import org.apache.commons.io.IOUtils;
import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/annotationContext.xml"})
@Deprecated
public class ScriptEnginePersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

    @Autowired
    private ApplicationContext applicationContext;

    private ScriptEnginePersonAttributeDao dao;

    public ScriptEnginePersonAttributeDaoTest() {
        this.dao = new ScriptEnginePersonAttributeDao();
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }

    @Test
    public void testGetAttributesWithJavascript() {
        this.dao.setScriptFile("SampleScriptedJavascriptPersonAttributeDao.js");
        final var person = this.dao.getPerson("testuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertEquals("testuser", person.getName());
        assertEquals(4, person.getAttributes().size());
    }

    @Test
    public void testGetAttributesWithGroovy() {
        this.dao.setScriptFile("SampleScriptedGroovyPersonAttributeDao.groovy");
        final var person = this.dao.getPerson("testuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertEquals("testuser", person.getName());
        assertEquals(4, person.getAttributes().size());
    }

    @Test
    public void testGetAttributesWithGroovyString() throws IOException {
        this.dao.setScriptFile(IOUtils.toString(applicationContext.getResource("file:./src/test/resources/SampleScriptedGroovyPersonAttributeDao.groovy").getInputStream(), StandardCharsets.UTF_8));
        this.dao.setEngineName("groovy");
        final var person = this.dao.getPerson("testuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertEquals("testuser", person.getName());
        assertEquals(4, person.getAttributes().size() );
    }

    @Test
    public void testGetMultiValuedAttributesWithGroovy() {
        this.dao.setScriptFile("SampleScriptedGroovyPersonAttributeDao.groovy");
        final Map<String, List<Object>> query = new LinkedHashMap<>();
        query.put("username", Util.list("testuser"));
        final var personSet = this.dao.getPeopleWithMultivaluedAttributes(query, IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(personSet);
        assertEquals(1, personSet.size());
        assertEquals("testuser", (personSet.iterator().next()).getName());
    }

    @Test
    public void testGetMultiValuedAttributesWithExtraAttributesGroovy() {
        this.dao.setScriptFile("SampleScriptedGroovyPersonAttributeDao.groovy");
        final Map<String, List<Object>> query = new LinkedHashMap<>();
        query.put("username", Util.list("testuser"));
        query.put("current_attribute", Util.list("something"));
        final var personSet = this.dao.getPeopleWithMultivaluedAttributes(query, IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(personSet);
        assertEquals(1, personSet.size());
        assertEquals("testuser", (personSet.iterator().next()).getName());
        assertEquals(Util.list("found_something"), (personSet.iterator().next()).getAttributes().get("new_attribute"));
    }

    @Test
    public void testGetScriptEngineName() {
        // test with an inline script, even though DAO avoids this internally
        assertNull(ScriptEnginePersonAttributeDao.getScriptEngineName("not a filename = a script"));
        // test with classpath resource
        final var scriptEngineName = ScriptEnginePersonAttributeDao.getScriptEngineName("SampleScriptedJavascriptPersonAttributeDao.js");
        
        assertTrue(scriptEngineName.equalsIgnoreCase("nashorn") || scriptEngineName.equalsIgnoreCase("Graal.js"));
        final var engineName2 = ScriptEnginePersonAttributeDao.getScriptEngineName("src/test/resources/SampleScriptedJavascriptPersonAttributeDao.js");
        assertTrue(engineName2.equalsIgnoreCase("nashorn") || scriptEngineName.equalsIgnoreCase("Graal.js"));
        assertEquals("groovy", ScriptEnginePersonAttributeDao.getScriptEngineName("src/test/resources/SampleScriptedGroovyPersonAttributeDao.groovy"));
        // note jython not in classpath so null is expected return value, also test.py file doesn't exist
        assertNull(ScriptEnginePersonAttributeDao.getScriptEngineName("test.py") );
    }

    @Test
    public void testDetermineScriptType() {
        // test with classpath resource
        assertEquals(ScriptEnginePersonAttributeDao.SCRIPT_TYPE.RESOURCE, new ScriptEnginePersonAttributeDao("SampleScriptedJavascriptPersonAttributeDao.js").getScriptType());
        assertEquals(ScriptEnginePersonAttributeDao.SCRIPT_TYPE.FILE, new ScriptEnginePersonAttributeDao("src/test/resources/SampleScriptedJavascriptPersonAttributeDao.js").getScriptType());
        // if file doesn't exist, assume script is contents of string
        assertEquals(ScriptEnginePersonAttributeDao.SCRIPT_TYPE.CONTENTS, new ScriptEnginePersonAttributeDao("doesnotexist/src/test/resources/SampleScriptedJavascriptPersonAttributeDao.js").getScriptType());
    }
}
