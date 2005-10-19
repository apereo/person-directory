/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * Provides base tests for classes that implement AbstractDefaultQueryPersonAttributeDao.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public abstract class AbstractDefaultQueryPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractPersonAttributeDaoTest#getPersonAttributeDaoInstance()
     */
    protected final IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.getAbstractDefaultQueryPersonAttributeDao();
    }
    
    protected abstract AbstractDefaultQueryPersonAttributeDao getAbstractDefaultQueryPersonAttributeDao();

    public void testNullDefaultAttributeName() {
        AbstractDefaultQueryPersonAttributeDao dao = getAbstractDefaultQueryPersonAttributeDao();
        try {
            dao.setDefaultAttributeName(null);
            fail("Expected IllegalArgumentException on setDefaultAttributeName(null)");
        } 
        catch (IllegalArgumentException iae) {
            return;
        }
    }
    
    public void testDefaultAttributeName() {
        AbstractDefaultQueryPersonAttributeDao dao = getAbstractDefaultQueryPersonAttributeDao();
        dao.setDefaultAttributeName("TestAttrName");
        assertEquals("TestAttrName", dao.getDefaultAttributeName());
    }
    
    public void testGetAttributesByString() {
        AbstractDefaultQueryPersonAttributeDao dao = new SimpleDefaultQueryPersonAttributeDao();
        dao.setDefaultAttributeName("TestAttrName");
        Map expected = new HashMap();
        expected.put("TestAttrName", "edalquist");
        
        assertEquals(expected, dao.getUserAttributes("edalquist"));
    }
    
    private class SimpleDefaultQueryPersonAttributeDao extends AbstractDefaultQueryPersonAttributeDao {
        /**
         * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set getPossibleUserAttributeNames() {
            return null;
        }

        /**
         * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
         */
        public Map getUserAttributes(Map seed) {
            return seed;
        }
    }
}
