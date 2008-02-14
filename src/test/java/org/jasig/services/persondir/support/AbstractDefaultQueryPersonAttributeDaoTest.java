/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.Util;

/**
 * Provides base tests for classes that implement AbstractDefaultAttributePersonAttributeDao.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractDefaultQueryPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractPersonAttributeDaoTest#getPersonAttributeDaoInstance()
     */
    @Override
    protected final IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.getAbstractDefaultQueryPersonAttributeDao();
    }
    
    protected abstract AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao();

    public void testNullDefaultAttributeName() {
        AbstractDefaultAttributePersonAttributeDao dao = getAbstractDefaultQueryPersonAttributeDao();
        try {
            dao.setDefaultAttributeName(null);
            fail("Expected IllegalArgumentException on setDefaultAttributeName(null)");
        } 
        catch (IllegalArgumentException iae) {
            return;
        }
    }
    
    public void testDefaultAttributeName() {
        AbstractDefaultAttributePersonAttributeDao dao = getAbstractDefaultQueryPersonAttributeDao();
        dao.setDefaultAttributeName("TestAttrName");
        assertEquals("TestAttrName", dao.getDefaultAttributeName());
    }
    
    public void testGetAttributesByString() {
        AbstractDefaultAttributePersonAttributeDao dao = new SimpleDefaultQueryPersonAttributeDao();
        dao.setDefaultAttributeName("TestAttrName");
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.put("TestAttrName", Util.list("edalquist"));
        
        assertEquals(expected, dao.getUserAttributes("edalquist"));
    }
    
    private class SimpleDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
        /**
         * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }

        /**
         * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
         */
        public Map<String, List<Object>> getUserAttributes(Map<String, List<Object>> seed) {
            return seed;
        }
    }
}
