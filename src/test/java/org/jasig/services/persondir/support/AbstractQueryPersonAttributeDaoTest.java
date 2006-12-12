/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public class AbstractQueryPersonAttributeDaoTest extends TestCase {
    private TestQueryPersonAttributeDao testQueryPersonAttributeDao;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.testQueryPersonAttributeDao = new TestQueryPersonAttributeDao();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.testQueryPersonAttributeDao = null;
    }
    
    public void testDefaultAttributeNameUsage() {
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final Object[] args = this.testQueryPersonAttributeDao.getArgs();
        final Object[] expectedArgs = new Object[] { "eric" };
        
        //Do asList for an easy comparison
        assertEquals(Arrays.asList(expectedArgs), Arrays.asList(args));
    }
    
    public void testInsuffcientSeed() {
        final List queryAttributes = new ArrayList();
        queryAttributes.add("userid");
        
        this.testQueryPersonAttributeDao.setQueryAttributes(queryAttributes);
        this.testQueryPersonAttributeDao.getUserAttributes("eric");
        final Object[] args = this.testQueryPersonAttributeDao.getArgs();
        assertNull(args);
    }
    
    public void testCustomAttributes() {
        final List queryAttributes = new ArrayList();
        queryAttributes.add("name.first");
        queryAttributes.add("name.last");
        this.testQueryPersonAttributeDao.setQueryAttributes(queryAttributes);
        
        final Map seed = new HashMap();
        seed.put("name.first", "eric");
        seed.put("name.last", "dalquist");
        this.testQueryPersonAttributeDao.getUserAttributes(seed);
        final Object[] args = this.testQueryPersonAttributeDao.getArgs();
        final Object[] expectedArgs = new Object[] { "eric", "dalquist" };
        
        //Do asList for an easy comparison
        assertEquals(Arrays.asList(expectedArgs), Arrays.asList(args));
    }

    private class TestQueryPersonAttributeDao extends AbstractQueryPersonAttributeDao {
        private Object[] args = null;
        
        /**
         * @return the args
         */
        public Object[] getArgs() {
            return this.args;
        }

        /**
         * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getUserAttributesIfNeeded(java.lang.Object[])
         */
        protected Map getUserAttributesIfNeeded(Object[] args) {
            this.args = args;
            return null;
        }

        /**
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set getPossibleUserAttributeNames() {
            return null;
        }
    }
}
