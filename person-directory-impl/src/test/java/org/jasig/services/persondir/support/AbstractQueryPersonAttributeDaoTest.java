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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.util.CaseCanonicalizationMode;
import org.jasig.services.persondir.util.Util;

/**
 * @author Eric Dalquist 
 * @version $Revision$
 */
public class AbstractQueryPersonAttributeDaoTest extends TestCase {
    private TestQueryPersonAttributeDao testQueryPersonAttributeDao;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.testQueryPersonAttributeDao = new TestQueryPersonAttributeDao();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.testQueryPersonAttributeDao = null;
    }
    
    public void testDefaultAttributeNameUsage() {
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final List<List<Object>> args = this.testQueryPersonAttributeDao.getArgs();
        
        //Do asList for an easy comparison
        assertEquals(Collections.singletonList(Collections.singletonList("eric")), args);
    }
    
    public void testNoQueryAttributeMapping() {
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final List<List<Object>>  args1 = this.testQueryPersonAttributeDao.getArgs();
        assertEquals(Arrays.asList(Arrays.asList("eric")), args1);
        
        this.testQueryPersonAttributeDao.setUseAllQueryAttributes(false);
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final List<List<Object>>  args2 = this.testQueryPersonAttributeDao.getArgs();
        assertNull(args2);
    }
    
    public void testInsuffcientSeed() {
        final Map<String, String> queryAttributes = new LinkedHashMap<String, String>();
        queryAttributes.put("userid", null);
        
        this.testQueryPersonAttributeDao.setQueryAttributeMapping(queryAttributes);
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final List<List<Object>>  args = this.testQueryPersonAttributeDao.getArgs();
        assertNull(args);
    }
    
    public void testCustomAttributes() {
        final Map<String, String> queryAttributes = new LinkedHashMap<String, String>();
        queryAttributes.put("name.first", null);
        queryAttributes.put("name.last", null);
        this.testQueryPersonAttributeDao.setQueryAttributeMapping(queryAttributes);
        
        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("name.first", Collections.singletonList((Object)"eric"));
        seed.put("name.last", Collections.singletonList((Object)"dalquist"));
        this.testQueryPersonAttributeDao.getMultivaluedUserAttributes(seed);
        final List<List<Object>> args = this.testQueryPersonAttributeDao.getArgs();
        final Object[] expectedArgs = new Object[] { Collections.singletonList("eric"), Collections.singletonList("dalquist") };
        
        //Do asList for an easy comparison
        assertEquals(Arrays.asList(expectedArgs), args);
    }

    public void testMapPersonAttributes_AsIs() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("edalquist"));
        storedAttrs.put("name.first", Util.list("eric"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        // By default should just echo attribs from data layer as-is
        assertEquals("edalquist", result.getName());
        assertEquals(Util.genList("edalquist"), result.getAttributeValues("username"));
        assertEquals(Util.genList("eric"), result.getAttributeValues("name.first"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("name.last"));
    }

    public void testMapPersonAttributes_Mapped() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("edalquist"));
        storedAttrs.put("name.first", Util.list("eric"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);

        final Map<String, String> resultAttributeMappings = new LinkedHashMap<String, String>();
        resultAttributeMappings.put("name.first", "fname");
        resultAttributeMappings.put("name.last", "lname");
        dao.setResultAttributeMapping(resultAttributeMappings);

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        assertEquals("edalquist", result.getName());
        // Don't actually get a username attribute in this case because it's
        // not in the result attribute mappings. But it *is* successfully mapped
        // into the special "name" property on the IPersonAttributes as asserted
        // above
        assertEquals(Util.genList("eric"), result.getAttributeValues("fname"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("lname"));
    }

    public void testMapPersonAttributes_CaseInsensitive() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("edalquist"));
        storedAttrs.put("name.first", Util.list("eric"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);
        final Map<String, CaseCanonicalizationMode> caseInsensitiveAttributes = new HashMap<String, CaseCanonicalizationMode>();
        caseInsensitiveAttributes.put("name.first", CaseCanonicalizationMode.UPPER);
        dao.setCaseInsensitiveResultAttributes(caseInsensitiveAttributes);

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        // By default should just echo attribs from data layer as-is
        assertEquals("edalquist", result.getName());
        assertEquals(Util.genList("edalquist"), result.getAttributeValues("username"));
        assertEquals(Util.genList("ERIC"), result.getAttributeValues("name.first"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("name.last"));
    }

    public void testMapPersonAttributes_MappedCaseInsensitive() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("edalquist"));
        storedAttrs.put("name.first", Util.list("eric"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);
        final Map<String, CaseCanonicalizationMode> caseInsensitiveAttributes = new HashMap<String, CaseCanonicalizationMode>();
        caseInsensitiveAttributes.put("fname", CaseCanonicalizationMode.UPPER);
        dao.setCaseInsensitiveResultAttributes(caseInsensitiveAttributes);

        final Map<String, String> resultAttributeMappings = new LinkedHashMap<String, String>();
        resultAttributeMappings.put("name.first", "fname");
        resultAttributeMappings.put("name.last", "lname");
        dao.setResultAttributeMapping(resultAttributeMappings);

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        assertEquals("edalquist", result.getName());
        // Don't actually get a username attribute in this case because it's
        // not in the result attribute mappings. But it *is* successfully mapped
        // into the special "name" property on the IPersonAttributes as asserted
        // above
        assertEquals(Util.genList("ERIC"), result.getAttributeValues("fname"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("lname"));
    }

    public void testMapPersonAttributes_CaseInsensitiveDefaultCanonicalization() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("EDALQUIST"));
        storedAttrs.put("name.first", Util.list("ERIC"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);
        // Not setting the CaseCanonicalizationMode here nor with an explicit
        // setter
        final Collection<String> caseInsensitiveAttributes = new HashSet<String>();
        caseInsensitiveAttributes.add("username");
        caseInsensitiveAttributes.add("name.first");
        dao.setCaseInsensitiveResultAttributesAsCollection(caseInsensitiveAttributes);

        // Without this the username *attribute* will be canonicalized correctly
        // but the special username ("name", actually) *property* on
        // IPersonAttributes won't be. See test below
        dao.setUsernameCaseCanonicalizationMode(CaseCanonicalizationMode.LOWER);

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        // By default should just echo attribs from data layer as-is
        assertEquals("edalquist", result.getName());
        assertEquals(Util.genList("edalquist"), result.getAttributeValues("username"));
        assertEquals(Util.genList("eric"), result.getAttributeValues("name.first"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("name.last"));
    }

    public void testMapPersonAttributes_IndependentUsernameCanonicalization() {
        final Map<String, List<Object>> storedAttrs = new HashMap<String, List<Object>>();
        storedAttrs.put("username", Util.list("EDALQUIST"));
        storedAttrs.put("name.first", Util.list("ERIC"));
        storedAttrs.put("name.last", Util.list("dalquist"));

        final InMemoryAbstractQueryPersonAttributeDao dao = new InMemoryAbstractQueryPersonAttributeDao(storedAttrs);
        // Not setting the CaseCanonicalizationMode here nor with an explicit
        // setter
        final Collection<String> caseInsensitiveAttributes = new HashSet<String>();
        caseInsensitiveAttributes.add("username");
        caseInsensitiveAttributes.add("name.first");
        dao.setCaseInsensitiveResultAttributesAsCollection(caseInsensitiveAttributes);
        // Intentionally *not* calling setUsernameCaseCanonicalizationMode()

        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("username", Collections.singletonList((Object)"edalquist"));

        final Set<IPersonAttributes> allResults = dao.getPeopleWithMultivaluedAttributes(seed);

        assertEquals(1, allResults.size());
        IPersonAttributes result = allResults.iterator().next();
        // Username canonicalization always independent, for better or worse,
        // of attribute canonicalization. See setUsernameCaseCanonicalizationMode()
        assertEquals("EDALQUIST", result.getName());
        assertEquals(Util.genList("edalquist"), result.getAttributeValues("username"));
        assertEquals(Util.genList("eric"), result.getAttributeValues("name.first"));
        assertEquals(Util.genList("dalquist"), result.getAttributeValues("name.last"));
    }

    private static class InMemoryAbstractQueryPersonAttributeDao extends AbstractQueryPersonAttributeDao<List<List<Object>>> {

        private StubPersonAttributeDao storage;

        InMemoryAbstractQueryPersonAttributeDao(Map<String, List<Object>> backingMap) {
            storage = new StubPersonAttributeDao(backingMap);
        }

        @Override
        protected List<IPersonAttributes> getPeopleForQuery(List<List<Object>> queryBuilder, String queryUserName) {
            return new ArrayList(storage.getPeopleWithMultivaluedAttributes(new HashMap<String,List<Object>>()));
        }

        @Override
        protected List<List<Object>> appendAttributeToQuery(List<List<Object>> queryBuilder, String dataAttribute, List<Object> queryValues) {
            // copy/paste from TestQueryPersonAttributeDao. Don't really care what this does, though
            if (queryBuilder == null) {
                queryBuilder = new LinkedList<List<Object>>();
            }

            queryBuilder.add(queryValues);

            return queryBuilder;
        }
    }

    private class TestQueryPersonAttributeDao extends AbstractQueryPersonAttributeDao<List<List<Object>>> {
        private List<List<Object>> args = null;
        
        /**
         * @return the args
         */
        public List<List<Object>> getArgs() {
            return this.args;
        }
        
        /* (non-Javadoc)
         * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#appendAttributeToQuery(java.lang.Object, java.lang.String, java.util.List)
         */
        @Override
        protected List<List<Object>> appendAttributeToQuery(List<List<Object>> queryBuilder, String dataAttribute, List<Object> queryValues) {
            if (queryBuilder == null) {
                queryBuilder = new LinkedList<List<Object>>();
            }
            
            queryBuilder.add(queryValues);
            
            return queryBuilder;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getPeopleForQuery(java.lang.Object, java.lang.String)
         */
        @Override
        protected List<IPersonAttributes> getPeopleForQuery(List<List<Object>> queryBuilder, String queryUserName) {
            this.args = queryBuilder;
            return null;
        }
    }
}
