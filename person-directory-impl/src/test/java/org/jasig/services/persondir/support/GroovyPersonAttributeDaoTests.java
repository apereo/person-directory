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
