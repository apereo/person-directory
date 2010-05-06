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

package org.jasig.services.persondir.support.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.util.Util;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XmlPersonAttributeDaoTest extends TestCase {
    private XmlPersonAttributeDao xmlPersonAttributeDao;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        if (this.xmlPersonAttributeDao == null) {
            this.xmlPersonAttributeDao = new XmlPersonAttributeDao();
            this.xmlPersonAttributeDao.setMappedXmlResource(new ClassPathResource("/PersonData.xml"));
            this.xmlPersonAttributeDao.afterPropertiesSet();
        }
    }
    
    public void testAvailableAttributes() {
        final Set<String> expectedAttributes = new LinkedHashSet<String>(
                Arrays.asList("username", "givenName", "familyName", "email", "sisID", "portalId", "emplid"));

        final Set<String> availableQueryAttributes = this.xmlPersonAttributeDao.getAvailableQueryAttributes();
        assertEquals(expectedAttributes, availableQueryAttributes);

        final Set<String> possibleUserAttributeNames = this.xmlPersonAttributeDao.getPossibleUserAttributeNames();
        assertEquals(expectedAttributes, possibleUserAttributeNames);
    }
    
    public void testQueryByUsername() {
        final IPersonAttributes boringPerson = this.xmlPersonAttributeDao.getPerson("iboring");
        assertNotNull(boringPerson);
        assertEquals("iboring", boringPerson.getName());
        assertEquals(Collections.EMPTY_MAP, boringPerson.getAttributes());
        
        
        final IPersonAttributes mstaffPerson = this.xmlPersonAttributeDao.getPerson("mstaff");
        assertNotNull(mstaffPerson);
        assertEquals("mstaff", mstaffPerson.getName());
        
        final Map<String, List<String>> mstaffAttributes = new LinkedHashMap<String, List<String>>();
        mstaffAttributes.put("givenName", Arrays.asList("Mary"));
        mstaffAttributes.put("familyName", Arrays.asList("Staff"));
        mstaffAttributes.put("email", Arrays.asList("mstaff@example.edu"));
        mstaffAttributes.put("emplid", Arrays.asList("963852741"));
        mstaffAttributes.put("portalId", Arrays.asList("!@#$%^*()_+"));
        
        assertEquals(mstaffAttributes, mstaffPerson.getAttributes());
        
        final IPersonAttributes nullPerson = this.xmlPersonAttributeDao.getPerson("doesntexist");
        assertNull(nullPerson);
    }
    
    public void testAttributeSearch1() {
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("email", Util.list("*@example.edu", "*@faculty.org"));
        
        final Set<IPersonAttributes> results = this.xmlPersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        assertEquals(3, results.size());
    }
    
    public void testAttributeSearch2() {
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("email", Util.list("*@example.edu", "j*"));
        
        final Set<IPersonAttributes> results = this.xmlPersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        assertEquals(2, results.size());
    }
    
    public void testAttributeSearch3() {
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("email", Util.list("*@example.edu", "*@faculty.org"));
        query.put("emplid", Util.list("*"));
        
        final Set<IPersonAttributes> results = this.xmlPersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        assertEquals(2, results.size());
    }
    
    public void testAttributeSearch4() {
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("email", Util.list("*@example.edu", "*@faculty.org"));
        query.put("emplid", Util.list(""));
        
        final Set<IPersonAttributes> results = this.xmlPersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        assertEquals(3, results.size());
    }
    
    public void testAttributeSearch5() {
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("emplid", Util.list(""));
        query.put("email", Util.list("*@example.edu", "*@faculty.org"));
        
        final Set<IPersonAttributes> results = this.xmlPersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        assertEquals(3, results.size());
    }
}
