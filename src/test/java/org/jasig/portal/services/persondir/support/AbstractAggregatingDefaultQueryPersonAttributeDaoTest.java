/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.mock.ThrowingPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.MultivaluedAttributeMerger;

/**
 * Provides base tests for classes that implement AbstractAggregatingDefaultQueryPersonAttributeDao.
 * 
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDaoTest extends AbstractDefaultQueryPersonAttributeDaoTest {
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest#getAbstractDefaultQueryPersonAttributeDao()
     */
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();
    }

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
 
    
    public void testGetPossibleNamesWithException() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        final Map attrMap1 = new HashMap();
        attrMap1.put("key1.1", "val1.1");
        attrMap1.put("key1.2", "val1.2");
        
        final Map attrMap2 = new HashMap();
        attrMap1.put("key2.1", "val2.1");
        attrMap1.put("key2.2", "val2.2");
        
        final Set expectedNames = new HashSet();
        expectedNames.addAll(attrMap1.keySet());
        expectedNames.addAll(attrMap2.keySet());
        
        final List childDaos = new ArrayList(3);
        childDaos.add(new StubPersonAttributeDao(attrMap1));
        childDaos.add(new ThrowingPersonAttributeDao());
        childDaos.add(new StubPersonAttributeDao(attrMap2));
        
        dao.setPersonAttributeDaos(childDaos);
        
        //Test exception recovery
        dao.setRecoverExceptions(true);
        final Set resultNames = dao.getPossibleUserAttributeNames();
        assertEquals(expectedNames, resultNames);
        
        //Test fail on exception
        dao.setRecoverExceptions(false);
        try {
            dao.getPossibleUserAttributeNames();
            fail("Expected RuntimeException on getPossibleUserAttributeNames() with ThrowingPersonAttributeDao as a child DAO");
        } 
        catch (RuntimeException re) {
            //expected
        }
    }
    
    public void testSetNullMerger() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        try {
            dao.setMerger(null);
            fail("Expected IllegalArgumentException on setMerger(null)");
        } 
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    public void testSetNullPersonAttributeDaos() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        try {
            dao.setPersonAttributeDaos(null);
            fail("Expected IllegalArgumentException on setPersonAttributeDaos(null)");
        } 
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    public void testProperties() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        final MultivaluedAttributeMerger merger = new MultivaluedAttributeMerger();
        dao.setMerger(merger);
        assertEquals(merger, dao.getMerger());
        
        dao.setPersonAttributeDaos(Collections.EMPTY_LIST);
        assertEquals(Collections.EMPTY_LIST, dao.getPersonAttributeDaos());
        
        dao.setRecoverExceptions(true);
        assertTrue(dao.isRecoverExceptions());
    }
}
