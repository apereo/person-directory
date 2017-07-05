package org.apereo.services.persondir.support;

import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;

public class ScriptEnginePersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    private ScriptEnginePersonAttributeDao dao;

    public ScriptEnginePersonAttributeDaoTest() {
        this.dao = new ScriptEnginePersonAttributeDao();
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.dao;
    }

    public void testGetAttributesWithJavascript() {
        this.dao.setScriptFile("SampleScriptedJavascriptPersonAttributeDao.js");
        final IPersonAttributes person = this.dao.getPerson("testuser");
        assertNotNull(person);
        assertEquals(person.getName(), "testuser");
        assertEquals(person.getAttributes().size(), 4);
    }

    public void testGetAttributesWithGroovy() {
        this.dao.setScriptFile("SampleScriptedGroovyPersonAttributeDao.groovy");
        final IPersonAttributes person = this.dao.getPerson("testuser");
        assertNotNull(person);
        assertEquals(person.getName(), "testuser");
        assertEquals(person.getAttributes().size(), 4);
    }

}
