/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.mock.ThrowingPersonAttributeDao;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.util.Util;

/**
 * MergingPersonAttributeDaoImpl testcase.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MergingPersonAttributeDaoImplTest 
    extends AbstractAggregatingDefaultQueryPersonAttributeDaoTest {
    
    private StubPersonAttributeDao sourceNull;
    private StubPersonAttributeDao sourceOne;
    private StubPersonAttributeDao sourceTwo;
    private StubPersonAttributeDao collidesWithOne;
    private Map<String, List<Object>> oneAndTwo;
    private Map<String, List<Object>> oneAndTwoAndThree;
    private final String queryAttr = "ThisDoesntMatterForMockDaos";
        
    @Override
    protected void setUp() {
        this.sourceNull = new StubPersonAttributeDao();
        
        this.sourceOne = new StubPersonAttributeDao();
        Map<String, List<Object>> sourceOneMap = new HashMap<String, List<Object>>();
        sourceOneMap.put("shirtColor", Util.list("blue"));
        sourceOneMap.put("favoriteColor", Util.list("purple"));
        this.sourceOne.setBackingMap(sourceOneMap);
        
        this.sourceTwo = new StubPersonAttributeDao();
        Map<String, List<Object>> sourceTwoMap = new HashMap<String, List<Object>>();
        sourceTwoMap.put("tieColor", Util.list("black"));
        sourceTwoMap.put("shoeType", Util.list("closed-toe"));
        this.sourceTwo.setBackingMap(sourceTwoMap);
        
        this.oneAndTwo = new HashMap<String, List<Object>>();
        this.oneAndTwo.putAll(sourceOneMap);
        this.oneAndTwo.putAll(sourceTwoMap);
        
        this.collidesWithOne = new StubPersonAttributeDao();
        Map<String, List<Object>> collidingMap = new HashMap<String, List<Object>>();
        collidingMap.put("shirtColor", Util.list("white"));
        collidingMap.put("favoriteColor", Util.list("red"));
        this.collidesWithOne.setBackingMap(collidingMap);
        
        this.oneAndTwoAndThree = new HashMap<String, List<Object>>();
        MultivaluedAttributeMerger merger = new MultivaluedAttributeMerger();
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceOneMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceTwoMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, collidingMap);
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        return impl;
    }

    /**
     * @see org.jasig.portal.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao() {
        return new MergingPersonAttributeDaoImpl();
    }


    /**
     * Test basic usage to merge attributes from a couple of sources.
     */
    public void testBasics() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceNull);
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceNull);
        attributeSources.add(this.sourceTwo);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("awp9"));
        
        Map<String, List<Object>> result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwo, result);
    }
    
    /**
     * Test basic merging of attribute names.
     */
    public void testAttributeNames() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Set<String> attributeNames = impl.getPossibleUserAttributeNames();
        
        assertEquals(this.oneAndTwo.keySet(), attributeNames);
    }
    
    /**
     * Test default exception handling behavior of recovering from failures
     * of individual attribute sources on the merge list.
     */
    public void testExceptionHandling() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("awp9"));
        
        Map<String, List<Object>> result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwoAndThree, result);
    }
    
    /**
     * Test handling of underlying sources which return null on 
     * getPossibleUserAttributeNames().
     */
    public void testNullAttribNames() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new NullAttribNamesPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        
        Set<String> attribNames = impl.getPossibleUserAttributeNames();
        
        Set<String> expectedAttribNames = new HashSet<String>();
        expectedAttribNames.addAll(this.sourceOne.getPossibleUserAttributeNames());
        expectedAttribNames.addAll(this.sourceTwo.getPossibleUserAttributeNames());
        expectedAttribNames.addAll(this.collidesWithOne.getPossibleUserAttributeNames());
        
        assertEquals(expectedAttribNames, attribNames);
    }
    
    /**
     * Test that, when configured to do so, MergingPersonAttributeDaoImpl
     * propogates RuntimeExceptions generated by its attribute sources.
     */
    public void testExceptionThrowing() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setRecoverExceptions(false);
        
        try {
            Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
            queryMap.put(queryAttr, Util.list("awp9"));
            
            impl.getUserAttributes(queryMap);
        } catch (RuntimeException rte) {
            // good, was propogated
            return;
        }
        fail("MergingPersonAttributeDao should have propogated RTE");
    }
    
    /**
     * Test ability to override the default merging strategy.
     *
     */
    public void testAlternativeMerging() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(this.collidesWithOne);
        
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setMerger(new NoncollidingAttributeAdder());
        
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("awp9"));
        
        Map<String, List<Object>> result = impl.getUserAttributes(queryMap);
        assertEquals(this.oneAndTwo, result);
    }
    
    public void testNoChildDaos() {
        MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("awp9"));
        
        try {
            impl.getUserAttributes(queryMap);
            fail("IllegalStateException should have been thrown");
        }
        catch (IllegalStateException ise) {
        }
    }
    
    /**
     * A mock, test implementation of IPersonAttributeDao which throws a 
     * RuntimeExcedption for the attribute getting methods and returns null
     * for the getPossibleUserAttributeNames() method.
     */
    private class NullAttribNamesPersonAttributeDao implements IPersonAttributeDao {

        /**
         * @throws RuntimeException Always.
         */
        public Map<String, List<Object>> getUserAttributes(String uid) {
            throw new RuntimeException("NullAttribNamesPersonAttributeDao always throws");
        }
        
        /**
         * @throws RuntimeException always
         */
        public Map<String, List<Object>> getUserAttributes(Map<String, List<Object>> queryMap) {
            throw new RuntimeException("NullAttribNamesPersonAttributeDao always throws");
        }
        
        /**
         * @return null
         */
        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }
    }
}
