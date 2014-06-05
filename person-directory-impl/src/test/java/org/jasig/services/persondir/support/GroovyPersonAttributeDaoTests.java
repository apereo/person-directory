package org.jasig.services.persondir.support;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.GroovyPersonAttributeDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.FileSystemResource;

@RunWith(JUnit4.class)
public class GroovyPersonAttributeDaoTests {

    @Test
    public void testScript() throws IOException {
        final GroovyPersonAttributeDao g = new GroovyPersonAttributeDao(
                new FileSystemResource("src/test/resources/SampleGroovyPersonAttributeDao.groovy"));
        
        final IPersonAttributes attrs = g.getPerson("userid");
        assertFalse(attrs.getAttributes().isEmpty());
        
        assertEquals(getAttributeAsSingleValue(attrs, "name"), "userid");
        
        assertEquals(getAttributeAsList(attrs, "likes").size(), 2);
    }

    private List<?> getAttributeAsList(final IPersonAttributes attrs, final String name) {
        return ((List<?>) attrs.getAttributes().get(name));
    }
    private Object getAttributeAsSingleValue(final IPersonAttributes attrs, final String name) {
        return getAttributeAsList(attrs, name).get(0);
    }
}
