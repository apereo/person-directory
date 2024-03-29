/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support;

import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.mock.ThrowingPersonAttributeDao;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.util.Util;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Provides base tests for classes that implement AbstractAggregatingDefaultQueryPersonAttributeDao.
 *
 * @author Eric Dalquist
 */
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDaoTest extends AbstractDefaultQueryPersonAttributeDaoTest {
    /**
     * @see AbstractDefaultQueryPersonAttributeDaoTest#getAbstractDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();
    }

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();


    @Test
    public void testGetPossibleNamesWithException() {
        var dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();

        final Map<String, List<Object>> attrMap1 = new HashMap<>();
        attrMap1.put("key1.1", Util.list("val1.1"));
        attrMap1.put("key1.2", Util.list("val1.2"));

        final Map<String, List<Object>> attrMap2 = new HashMap<>();
        attrMap1.put("key2.1", Util.list("val2.1"));
        attrMap1.put("key2.2", Util.list("val2.2"));

        final Set<String> expectedNames = new HashSet<>();
        expectedNames.addAll(attrMap1.keySet());
        expectedNames.addAll(attrMap2.keySet());

        final List<IPersonAttributeDao> childDaos = new ArrayList<>(3);
        childDaos.add(new StubPersonAttributeDao(attrMap1));
        childDaos.add(new ThrowingPersonAttributeDao());
        childDaos.add(new StubPersonAttributeDao(attrMap2));

        dao.setPersonAttributeDaos(childDaos);

        //Test exception recovery
        dao.setRecoverExceptions(true);
        var resultNames = dao.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedNames, resultNames);

        //Test fail on exception
        dao.setRecoverExceptions(false);
        try {
            dao.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
            fail("Expected RuntimeException on getPossibleUserAttributeNames() with ThrowingPersonAttributeDao as a child DAO");
        } catch (final RuntimeException re) {
            //expected
        }
    }


    @Test
    public void testStopOnSuccess() {
        var dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();

        final Map<String, List<Object>> attrMap1 = new HashMap<>();
        attrMap1.put("username", Util.list("test"));
        attrMap1.put("key1.1", Util.list("val1.1"));
        attrMap1.put("key1.2", Util.list("val1.2"));

        final Map<String, List<Object>> attrMap2 = new HashMap<>();
        attrMap2.put("username", Util.list("test"));
        attrMap2.put("key2.1", Util.list("val2.1"));
        attrMap2.put("key2.2", Util.list("val2.2"));

        final Set<String> expectedNamesWithStop = new HashSet<>();
        expectedNamesWithStop.addAll(attrMap1.keySet());

        final Set<String> expectedNamesWithoutStop = new HashSet<>();
        expectedNamesWithoutStop.addAll(attrMap1.keySet());
        expectedNamesWithoutStop.addAll(attrMap2.keySet());

        final List<IPersonAttributeDao> childDaos = new ArrayList<>(3);
        childDaos.add(new ThrowingPersonAttributeDao());
        childDaos.add(new StubPersonAttributeDao(attrMap1));
        childDaos.add(new StubPersonAttributeDao(attrMap2));

        dao.setPersonAttributeDaos(childDaos);


        dao.setStopOnSuccess(true);

        var resultNamesWithStop = dao.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedNamesWithStop, resultNamesWithStop);

        var personWithStop = dao.getPerson("test");
        assertEquals(new AttributeNamedPersonImpl(attrMap1), personWithStop);

        dao.setStopOnSuccess(false);

        var resultNamesWithoutStop = dao.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedNamesWithoutStop, resultNamesWithoutStop);

        var personWithoutStop = dao.getPerson("test");
        assertEquals(new AttributeNamedPersonImpl(attrMap1), personWithoutStop);

    }

    @Test
    public void testSetNullMerger() {
        var dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();

        try {
            dao.setMerger(null);
            fail("Expected IllegalArgumentException on setMerger(null)");
        } catch (final NullPointerException iae) {
            //expected
        }
    }

    @Test
    public void testSetNullPersonAttributeDaos() {
        var dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();

        try {
            dao.setPersonAttributeDaos(null);
            fail("Expected IllegalArgumentException on setPersonAttributeDaos(null)");
        } catch (final NullPointerException iae) {
            //expected
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProperties() {
        var dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();

        var merger = new MultivaluedAttributeMerger();
        dao.setMerger(merger);
        assertEquals(merger, dao.getMerger());

        dao.setPersonAttributeDaos(Collections.EMPTY_LIST);
        assertEquals(Collections.EMPTY_LIST, dao.getPersonAttributeDaos());

        dao.setRecoverExceptions(true);
        assertTrue(dao.isRecoverExceptions());
    }
}
