/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

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
        final List<String> queryAttributes = new ArrayList<String>();
        queryAttributes.add("userid");
        
        this.testQueryPersonAttributeDao.setQueryAttributes(queryAttributes);
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final List<List<Object>>  args = this.testQueryPersonAttributeDao.getArgs();
        assertNull(args);
    }
    
    public void testCustomAttributes() {
        final List<String> queryAttributes = new ArrayList<String>();
        queryAttributes.add("name.first");
        queryAttributes.add("name.last");
        this.testQueryPersonAttributeDao.setQueryAttributes(queryAttributes);
        
        final Map<String, List<Object>> seed = new HashMap<String, List<Object>>();
        seed.put("name.first", Collections.singletonList((Object)"eric"));
        seed.put("name.last", Collections.singletonList((Object)"dalquist"));
        this.testQueryPersonAttributeDao.getMultivaluedUserAttributes(seed);
        final List<List<Object>> args = this.testQueryPersonAttributeDao.getArgs();
        final Object[] expectedArgs = new Object[] { Collections.singletonList("eric"), Collections.singletonList("dalquist") };
        
        //Do asList for an easy comparison
        assertEquals(Arrays.asList(expectedArgs), args);
    }

    private class TestQueryPersonAttributeDao extends AbstractQueryPersonAttributeDao {
        private List<List<Object>> args = null;
        
        /**
         * @return the args
         */
        public List<List<Object>> getArgs() {
            return this.args;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getUserAttributesIfNeeded(java.util.List)
         */
        @Override
        protected Map<String, List<Object>> getUserAttributesIfNeeded(List<List<Object>> args) {
            this.args = args;
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }
    }
}
