/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support;

import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.mock.ThrowingPersonAttributeDao;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MergingPersonAttributeDaoImpl testcase.
 * @author andrew.petro@yale.edu

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
        final Map<String, List<Object>> sourceOneMap = new HashMap<>();
        sourceOneMap.put("shirtColor", Util.list("blue"));
        sourceOneMap.put("favoriteColor", Util.list("purple"));
        this.sourceOne.setBackingMap(sourceOneMap);

        this.sourceTwo = new StubPersonAttributeDao();
        final Map<String, List<Object>> sourceTwoMap = new HashMap<>();
        sourceTwoMap.put("tieColor", Util.list("black"));
        sourceTwoMap.put("shoeType", Util.list("closed-toe"));
        this.sourceTwo.setBackingMap(sourceTwoMap);

        this.oneAndTwo = new HashMap<>();
        this.oneAndTwo.putAll(sourceOneMap);
        this.oneAndTwo.putAll(sourceTwoMap);

        this.collidesWithOne = new StubPersonAttributeDao();
        final Map<String, List<Object>> collidingMap = new HashMap<>();
        collidingMap.put("shirtColor", Util.list("white"));
        collidingMap.put("favoriteColor", Util.list("red"));
        this.collidesWithOne.setBackingMap(collidingMap);

        this.oneAndTwoAndThree = new HashMap<>();
        final MultivaluedAttributeMerger merger = new MultivaluedAttributeMerger();
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceOneMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, sourceTwoMap);
        this.oneAndTwoAndThree = merger.mergeAttributes(this.oneAndTwoAndThree, collidingMap);
    }

    /**
     * @see AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        return impl;
    }

    /**
     * @see AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao() {
        return new MergingPersonAttributeDaoImpl();
    }


    /**
     * Test basic usage to merge attributes from a couple of sources.
     */
    public void testBasics() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceNull);
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceNull);
        attributeSources.add(this.sourceTwo);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(queryAttr, Util.list("awp9"));

        final Map<String, List<Object>> result = impl.getMultivaluedUserAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(this.oneAndTwo, result);
    }

    /**
     * Test basic merging of attribute names.
     */
    public void testAttributeNames() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        final Set<String> attributeNames = impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());

        assertEquals(this.oneAndTwo.keySet(), attributeNames);
    }

    /**
     * Test default exception handling behavior of recovering from failures
     * of individual attribute sources on the merge list.
     */
    public void testExceptionHandling() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(queryAttr, Util.list("awp9"));

        final Map<String, List<Object>> result = impl.getMultivaluedUserAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(this.oneAndTwoAndThree, result);
    }

    /**
     * Test handling of underlying sources which return null on 
     * getPossibleUserAttributeNames().
     */
    public void testNullAttribNames() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new NullAttribNamesPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        final Set<String> attribNames = impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());

        final Set<String> expectedAttribNames = new HashSet<>();
        expectedAttribNames.addAll(this.sourceOne.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
        expectedAttribNames.addAll(this.sourceTwo.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
        expectedAttribNames.addAll(this.collidesWithOne.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));

        assertEquals(expectedAttribNames, attribNames);
    }

    /**
     * Test that, when configured to do so, MergingPersonAttributeDaoImpl
     * propogates RuntimeExceptions generated by its attribute sources.
     */
    public void testExceptionThrowing() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(new ThrowingPersonAttributeDao());
        attributeSources.add(this.collidesWithOne);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setRecoverExceptions(false);

        try {
            final Map<String, List<Object>> queryMap = new HashMap<>();
            queryMap.put(queryAttr, Util.list("awp9"));

            impl.getMultivaluedUserAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        } catch (final RuntimeException rte) {
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
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        attributeSources.add(this.collidesWithOne);

        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setMerger(new NoncollidingAttributeAdder());

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(queryAttr, Util.list("awp9"));

        final Map<String, List<Object>> result = impl.getMultivaluedUserAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(this.oneAndTwo, result);
    }

    public void testNoChildDaos() {
        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(queryAttr, Util.list("awp9"));

        try {
            impl.getMultivaluedUserAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
            fail("IllegalStateException should have been thrown");
        } catch (final IllegalStateException ise) {
        }
    }

    /**
     * Test handling of underlying sources which return null on 
     * getPossibleUserAttributeNames().
     */
    public void testUsernameWildcardQuery() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        final ComplexStubPersonAttributeDao complexSourceOne = new ComplexStubPersonAttributeDao();
        final Map<String, Map<String, List<Object>>> backingMapOne = new HashMap<>();

        final Map<String, List<Object>> loHomeAttrs = new HashMap<>();
        loHomeAttrs.put("username", Arrays.asList((Object) "lo-home"));
        loHomeAttrs.put("givenName", Arrays.asList((Object) "Home"));
        loHomeAttrs.put("familyName", Arrays.asList((Object) "Layout Owner"));
        backingMapOne.put("lo-home", loHomeAttrs);

        final Map<String, List<Object>> loWelcomeAttrs = new HashMap<>();
        loWelcomeAttrs.put("username", Arrays.asList((Object) "lo-welcome"));
        loWelcomeAttrs.put("givenName", Arrays.asList((Object) "Welcome"));
        loWelcomeAttrs.put("familyName", Arrays.asList((Object) "Layout Owner"));
        backingMapOne.put("lo-welcome", loWelcomeAttrs);

        complexSourceOne.setBackingMap(backingMapOne);
        attributeSources.add(complexSourceOne);


        final ComplexStubPersonAttributeDao complexSourceTwo = new ComplexStubPersonAttributeDao();
        final Map<String, Map<String, List<Object>>> backingMapTwo = new HashMap<>();

        final Map<String, List<Object>> edalquistAttrs = new HashMap<>();
        edalquistAttrs.put("username", Arrays.asList((Object) "edalquist"));
        edalquistAttrs.put("givenName", Arrays.asList((Object) "Eric"));
        edalquistAttrs.put("familyName", Arrays.asList((Object) "Dalquist"));
        backingMapTwo.put("edalquist", edalquistAttrs);

        final Map<String, List<Object>> jshomeAttrs = new HashMap<>();
        jshomeAttrs.put("username", Arrays.asList((Object) "jshome"));
        jshomeAttrs.put("givenName", Arrays.asList((Object) "Joe"));
        jshomeAttrs.put("familyName", Arrays.asList((Object) "Shome"));
        backingMapTwo.put("jshome", jshomeAttrs);

        complexSourceTwo.setBackingMap(backingMapTwo);
        attributeSources.add(complexSourceTwo);


        final MergingPersonAttributeDaoImpl impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        final Set<IPersonAttributes> layoutOwners = impl.getPeople(Collections.singletonMap("username", (Object) "lo-*"), IPersonAttributeDaoFilter.alwaysChoose());

        final Set<IPersonAttributes> excepectedLayoutOwners = new HashSet<>();
        excepectedLayoutOwners.add(new NamedPersonImpl("lo-welcome", loWelcomeAttrs));
        excepectedLayoutOwners.add(new NamedPersonImpl("lo-home", loHomeAttrs));

        assertEquals(excepectedLayoutOwners, layoutOwners);


        final Set<IPersonAttributes> homeUsers = impl.getPeople(Collections.singletonMap("username", (Object) "*home"), IPersonAttributeDaoFilter.alwaysChoose());

        final Set<IPersonAttributes> excepectedHomeUsers = new HashSet<>();
        excepectedHomeUsers.add(new NamedPersonImpl("jshome", jshomeAttrs));
        excepectedHomeUsers.add(new NamedPersonImpl("lo-home", loHomeAttrs));

        assertEquals(excepectedHomeUsers, homeUsers);
    }

    /**
     * A mock, test implementation of IPersonAttributeDao which throws a 
     * RuntimeExcedption for the attribute getting methods and returns null
     * for the getPossibleUserAttributeNames() method.
     */
    private static class NullAttribNamesPersonAttributeDao extends ThrowingPersonAttributeDao {
        /**
         * @return null
         */
        @Override
        public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
            return null;
        }
    }
}
