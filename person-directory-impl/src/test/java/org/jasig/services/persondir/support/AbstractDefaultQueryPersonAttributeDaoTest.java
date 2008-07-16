/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.util.Util;

/**
 * Provides base tests for classes that implement AbstractDefaultAttributePersonAttributeDao.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractDefaultQueryPersonAttributeDaoTest extends AbstractFlatteningPersonAttributeDaoTest {
    /**
     * @see org.jasig.services.persondir.support.AbstractFlatteningPersonAttributeDaoTest#getAbstractFlatteningPersonAttributeDao()
     */
    @Override
    protected final AbstractFlatteningPersonAttributeDao getAbstractFlatteningPersonAttributeDao() {
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
        
        assertEquals(expected, dao.getMultivaluedUserAttributes("edalquist"));
    }
    
    private class SimpleDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
         */
        public Set<String> getAvailableQueryAttributes() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
         */
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
            return Collections.singleton((IPersonAttributes)new AttributeNamedPersonImpl(query));
        }
    }
}
