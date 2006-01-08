/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.services.persondir.mock.ThrowingPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.MultivaluedAttributeMerger;

/**
 * CascadingPersonAttributeDao testcase.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class CascadingPersonAttributeDaoTest 
    extends AbstractAggregatingDefaultQueryPersonAttributeDaoTest {
    
    private ComplexStubPersonAttributeDao sourceOne;
    private ComplexStubPersonAttributeDao sourceTwo;
    private StubPersonAttributeDao nullSource;
        
    protected void setUp() {
        Map daoBackingMap1 = new HashMap();
        
        Map user1 = new HashMap();
        user1.put("phone", "777-7777");
        user1.put("studentId", "123456789");
        daoBackingMap1.put("edalquist", user1);
        
        Map user2 = new HashMap();
        user2.put("phone", "888-8888");
        user2.put("studentId", "987654321");
        daoBackingMap1.put("awp9", user2);
        
        Map user3 = new HashMap();
        user3.put("phone", "666-6666");
        user3.put("studentId", "000000000");
        daoBackingMap1.put("erider", user3);
        
        this.sourceOne = new ComplexStubPersonAttributeDao();
        this.sourceOne.setBackingMap(daoBackingMap1);
        this.sourceOne.setDefaultAttributeName("username");
        

        Map daoBackingMap2 = new HashMap();
        
        Map user1a = new HashMap();
        user1a.put("phone", "777-7777x777");
        user1a.put("major", "CS");
        daoBackingMap2.put("123456789", user1a);
        
        Map user2a = new HashMap();
        user2a.put("phone", "888-8887x888");
        user2a.put("major", "ME");
        daoBackingMap2.put("987654321", user2a);
        
        Map user3a = new HashMap();
        user3a.put("phone", "666-6666x666");
        user3a.put("major", "EE");
        daoBackingMap2.put("000000000", user3a);
        
        this.sourceTwo = new ComplexStubPersonAttributeDao();
        this.sourceTwo.setBackingMap(daoBackingMap2);
        this.sourceTwo.setDefaultAttributeName("studentId");
        
        
        this.nullSource = new StubPersonAttributeDao();
    }
    
    public void testCascadingQuery() {
        final List targets = new ArrayList();
        targets.add(this.sourceOne);
        targets.add(this.nullSource);
        targets.add(this.sourceTwo);
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());
        
        
        Map results = targetDao.getUserAttributes("edalquist");
        
        Map expected = new HashMap();
        expected.put("studentId", "123456789");
        expected.put("major", "CS");
        List phoneNums = new ArrayList();
        phoneNums.add("777-7777");
        phoneNums.add("777-7777x777");
        expected.put("phone", phoneNums);
        
        assertEquals(expected, results);
    }

    public void testNoChildren() {
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        
        try {
            targetDao.getUserAttributes("edalquist");
            fail("IllegalStateException should have been thrown with no child DAOs");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testThrowingChildDao() {
        final List targets = new ArrayList();
        targets.add(this.sourceOne);
        targets.add(new ThrowingPersonAttributeDao());
        targets.add(this.sourceTwo);
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());
        
        
        targetDao.setRecoverExceptions(true);
        Map results = targetDao.getUserAttributes("edalquist");
        
        Map expected = new HashMap();
        expected.put("studentId", "123456789");
        expected.put("major", "CS");
        List phoneNums = new ArrayList();
        phoneNums.add("777-7777");
        phoneNums.add("777-7777x777");
        expected.put("phone", phoneNums);
        
        assertEquals(expected, results);
        
        
        
        targetDao.setRecoverExceptions(false);
        try {
            targetDao.getUserAttributes("edalquist");
            fail("RuntimeException should have been thrown with no child DAOs");
        }
        catch (RuntimeException ise) {
            //expected
        }
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao() {
        List attributeSources = new ArrayList();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        final CascadingPersonAttributeDao impl = new CascadingPersonAttributeDao();
        impl.setPersonAttributeDaos(attributeSources);
        
        return impl;
    }

    /**
     * @see org.jasig.portal.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao() {
        return new CascadingPersonAttributeDao();
    }
}
