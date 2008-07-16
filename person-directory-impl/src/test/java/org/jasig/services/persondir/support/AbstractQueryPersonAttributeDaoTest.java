/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributes;

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
         * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getPeopleForQuery(java.lang.Object)
         */
        @Override
        protected List<IPersonAttributes> getPeopleForQuery(List<List<Object>> queryBuilder) {
            this.args = queryBuilder;
            return null;
        }
    }
}
