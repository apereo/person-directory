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

import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.mock.ThrowingPersonAttributeDao;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CascadingPersonAttributeDao testcase.
 * @author andrew.petro@yale.edu

 */
public class CascadingPersonAttributeDaoTest
        extends AbstractAggregatingDefaultQueryPersonAttributeDaoTest {

    private ComplexStubPersonAttributeDao sourceOne;
    private ComplexStubPersonAttributeDao sourceTwo;
    private StubPersonAttributeDao nullSource;

    @Override
    protected void setUp() {
        var usernameAttributeProvider = new SimpleUsernameAttributeProvider("username");


        final Map<String, Map<String, List<Object>>> daoBackingMap1 = new HashMap<>();

        final Map<String, List<Object>> user1 = new HashMap<>();
        user1.put("phone", Util.list("777-7777"));
        user1.put("studentId", Util.list("123456789"));
        daoBackingMap1.put("edalquist", user1);

        final Map<String, List<Object>> user2 = new HashMap<>();
        user2.put("phone", Util.list("888-8888"));
        user2.put("studentId", Util.list("987654321"));
        daoBackingMap1.put("awp9", user2);

        final Map<String, List<Object>> user3 = new HashMap<>();
        user3.put("phone", Util.list("666-6666"));
        user3.put("studentId", Util.list("000000000"));
        daoBackingMap1.put("erider", user3);

        this.sourceOne = new ComplexStubPersonAttributeDao();
        this.sourceOne.setBackingMap(daoBackingMap1);
        this.sourceOne.setUsernameAttributeProvider(usernameAttributeProvider);


        final Map<String, Map<String, List<Object>>> daoBackingMap2 = new HashMap<>();

        final Map<String, List<Object>> user1a = new HashMap<>();
        user1a.put("phone", Util.list("777-7777x777"));
        user1a.put("major", Util.list("CS"));
        user1a.put("username", Util.list("edalquist"));
        daoBackingMap2.put("123456789", user1a);

        final Map<String, List<Object>> user2a = new HashMap<>();
        user2a.put("phone", Util.list("888-8887x888"));
        user2a.put("major", Util.list("ME"));
        user2a.put("username", Util.list("awp9"));
        daoBackingMap2.put("987654321", user2a);

        final Map<String, List<Object>> user3a = new HashMap<>();
        user3a.put("phone", Util.list("666-6666x666"));
        user3a.put("major", Util.list("EE"));
        user3a.put("username", Util.list("erider"));
        daoBackingMap2.put("000000000", user3a);

        this.sourceTwo = new ComplexStubPersonAttributeDao();
        this.sourceTwo.setBackingMap(daoBackingMap2);
        this.sourceTwo.setQueryAttributeName("studentId");
        this.sourceTwo.setUsernameAttributeProvider(usernameAttributeProvider);

        this.nullSource = new StubPersonAttributeDao();
    }

    public void testCascadingQuery() {
        final List<IPersonAttributeDao> targets = new ArrayList<>();
        targets.add(this.sourceOne);
        targets.add(this.nullSource);
        targets.add(this.sourceTwo);

        var targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());

        var results = targetDao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());

        final Map<String, List<Object>> expected = new HashMap<>();
        expected.put("username", Util.list("edalquist"));
        expected.put("studentId", Util.list("123456789"));
        expected.put("major", Util.list("CS"));
        expected.put("phone", Util.list("777-7777", "777-7777x777"));

        assertEquals(expected, results.getAttributes());
    }

    public void testNoChildren() {
        var targetDao = new CascadingPersonAttributeDao();

        try {
            targetDao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
            fail("IllegalStateException should have been thrown with no child DAOs");
        } catch (final IllegalStateException ise) {
            //expected
        }
    }

    public void testThrowingChildDao() {
        final List<IPersonAttributeDao> targets = new ArrayList<>();
        targets.add(this.sourceOne);
        targets.add(new ThrowingPersonAttributeDao());
        targets.add(this.sourceTwo);

        var targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());


        targetDao.setRecoverExceptions(true);
        var results = targetDao.getPerson("edalquist",
            IPersonAttributeDaoFilter.alwaysChoose());

        final Map<String, List<Object>> expected = new HashMap<>();
        expected.put("studentId", Util.list("123456789"));
        expected.put("major", Util.list("CS"));
        expected.put("username", Util.list("edalquist"));
        expected.put("phone", Util.list("777-7777", "777-7777x777"));

        assertEquals(expected, results.getAttributes());


        targetDao.setRecoverExceptions(false);
        try {
            targetDao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
            fail("RuntimeException should have been thrown with no child DAOs");
        } catch (final RuntimeException ise) {
            //expected
        }
    }

    public void testNullFirstResultNoStop() {
        final List<IPersonAttributeDao> targets = new ArrayList<>();
        targets.add(this.nullSource);
        targets.add(this.sourceOne);
        targets.add(this.sourceTwo);

        var targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());

        var results = targetDao.getPerson("edalquist",
            IPersonAttributeDaoFilter.alwaysChoose());

        final Map<String, List<Object>> expected = new HashMap<>();
        expected.put("username", Util.list("edalquist"));
        expected.put("studentId", Util.list("123456789"));
        expected.put("major", Util.list("CS"));
        expected.put("phone", Util.list("777-7777", "777-7777x777"));

        assertEquals(expected, results.getAttributes());
    }

    public void testNullFirstResultStop() {
        final List<IPersonAttributeDao> targets = new ArrayList<>();
        targets.add(this.nullSource);
        targets.add(this.sourceOne);
        targets.add(this.sourceTwo);

        var targetDao = new CascadingPersonAttributeDao();
        targetDao.setStopIfFirstDaoReturnsNull(true);
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());

        var results = targetDao.getPerson("edalquist",
            IPersonAttributeDaoFilter.alwaysChoose());

        assertNull(results);
    }

    /**
     * @see AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao() {
        final List<IPersonAttributeDao> attributeSources = new ArrayList<>();

        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);

        var impl = new CascadingPersonAttributeDao();
        impl.setPersonAttributeDaos(attributeSources);

        return impl;
    }

    /**
     * @see AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao() {
        return new CascadingPersonAttributeDao();
    }
}
