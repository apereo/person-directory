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
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.util.Util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Eric Dalquist

 */
@SuppressWarnings("deprecation")
public class CachingPersonAttributeDaoTest extends AbstractDefaultQueryPersonAttributeDaoTest {
    private static final String defaultAttr = "uid";

    private ComplexStubPersonAttributeDao stubDao;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.stubDao = new ComplexStubPersonAttributeDao();

        final Map<String, Map<String, List<Object>>> daoBackingMap = new HashMap<>();

        final Map<String, List<Object>> user1 = new HashMap<>();
        user1.put("phone", Util.list("777-7777"));
        user1.put("displayName", Util.list("Display Name"));
        daoBackingMap.put("edalquist", user1);

        final Map<String, List<Object>> user2 = new HashMap<>();
        user2.put("phone", Util.list("888-8888"));
        user2.put("displayName", Util.list(""));
        daoBackingMap.put("awp9", user2);

        final Map<String, List<Object>> user3 = new HashMap<>();
        user3.put("phone", Util.list("666-6666"));
        user3.put("givenName", Util.list("Howard"));
        daoBackingMap.put("erider", user3);


        this.stubDao.setBackingMap(daoBackingMap);
        this.stubDao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));

        super.setUp();
    }

    private void validateUser1(final Map<String, List<Object>> attrs) {
        assertNotNull("Attribute Map for User1 May not be null", attrs);
        assertEquals(Util.list("777-7777"), attrs.get("phone"));
        assertEquals(Util.list("Display Name"), attrs.get("displayName"));
    }

    private void validateUser2(final Map<String, List<Object>> attrs) {
        assertNotNull("Attribute Map for User2 May not be null", attrs);
        assertEquals(Util.list("888-8888"), attrs.get("phone"));
        assertEquals(Util.list(""), attrs.get("displayName"));
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.stubDao = null;

        super.tearDown();
    }


    public void testCacheStats() throws Exception {
        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(new HashMap<>());
        dao.afterPropertiesSet();

        assertEquals("Query count incorrect", 0, dao.getQueries());
        assertEquals("Miss count incorrect", 0, dao.getMisses());

        var result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Query count incorrect", 1, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Query count incorrect", 2, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());

        result = dao.getPerson("nobody", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Query count incorrect", 3, dao.getQueries());
        assertEquals("Miss count incorrect", 2, dao.getMisses());

        result = dao.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser2(result.getAttributes());
        assertEquals("Query count incorrect", 4, dao.getQueries());
        assertEquals("Miss count incorrect", 3, dao.getMisses());

        result = dao.getPerson("nobody", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Query count incorrect", 5, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());

        result = dao.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser2(result.getAttributes());
        assertEquals("Query count incorrect", 6, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Query count incorrect", 7, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
    }

    public void testCaching() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        var result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());

        result = dao.getPerson("nobody", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());

        //Enable null result caching
        dao.setCacheNullResults(true);
        result = dao.getPerson("nobody", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(result.getAttributes());
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());


        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));

        var resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultSet.size());
        this.validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());

        dao.removeUserAttributesMultivaluedSeed(queryMap);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());

        dao.removeUserAttributes("nobody");
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
    }


    public void testMulipleAttributeKeys() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();

        final Set<String> keyAttrs = new HashSet<>();
        keyAttrs.add("name.first");
        keyAttrs.add("name.last");

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        var cacheKeyGenerator = new AttributeBasedCacheKeyGenerator();
        cacheKeyGenerator.setCacheKeyAttributes(keyAttrs);
        dao.setCacheKeyGenerator(cacheKeyGenerator);
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        var result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        result = dao.getPerson("nobody", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        result = dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        final Map<String, List<Object>> queryMap1 = new HashMap<>();
        queryMap1.put(defaultAttr, Util.list("edalquist"));
        queryMap1.put("name.first", Util.list("Eric"));
        queryMap1.put("name.last", Util.list("Dalquist"));

        var resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap1, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultSet.size());
        this.validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());


        final Map<String, Object> queryMap2 = new HashMap<>();
        queryMap2.put("name.first", Util.list("John"));
        queryMap2.put("name.last", Util.list("Doe"));

        resultSet = dao.getPeople(queryMap2, IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(resultSet);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());


        resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap1, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultSet.size());
        this.validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
    }

    public void testPropertyConstraints() {
        var dao = new CachingPersonAttributeDaoImpl();

        try {
            dao.setCachedPersonAttributesDao(null);
            fail("setCachedPersonAttributesDao(null) should have thrown an IllegalArgumentException.");
        } catch (final IllegalArgumentException iae) {
            //expected
        }
        dao.setCachedPersonAttributesDao(this.stubDao);
        assertEquals(this.stubDao, dao.getCachedPersonAttributesDao());

        try {
            dao.setUserInfoCache(null);
            fail("setUserInfoCache(null) should have thrown an IllegalArgumentException.");
        } catch (final IllegalArgumentException iae) {
            //expected
        }
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();
        final Map<Serializable, Set<IPersonAttributes>> expectedcacheMap = new HashMap<>(cacheMap);
        dao.setUserInfoCache(cacheMap);
        assertEquals(expectedcacheMap, dao.getUserInfoCache());
    }

    @SuppressWarnings("unchecked")
    public void testUninitializedDao() {
        var dao = new CachingPersonAttributeDaoImpl();
        try {
            dao.getPeople(Collections.EMPTY_MAP, IPersonAttributeDaoFilter.alwaysChoose());
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        } catch (final IllegalStateException ise) {
            //expected
        }

        dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        try {
            dao.getPeople(Collections.EMPTY_MAP, IPersonAttributeDaoFilter.alwaysChoose());
            fail("Calling getUserAttributes(Map) with only setCachedPersonAttributesDao initialized should result in an IllegalStateException");
        } catch (final IllegalStateException ise) {
            //expected
        }

        dao = new CachingPersonAttributeDaoImpl();
        dao.setUserInfoCache(new HashMap<>());
        try {
            dao.getPeople(Collections.EMPTY_MAP, IPersonAttributeDaoFilter.alwaysChoose());
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        } catch (final IllegalStateException ise) {
            //expected
        }
    }

    public void testEmptyCacheKeyWithDefaultAttr() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        var resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")), IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist"))
            , IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("nobody"))
            , IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(resultsSet);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist"))
            , IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());


        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));

        resultsSet = dao.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
    }

    public void testEmptyCacheKeyWithKeyAttrs() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        var cacheKeyGenerator = new AttributeBasedCacheKeyGenerator();
        cacheKeyGenerator.setCacheKeyAttributes(Collections.singleton("UNUSED_ATTR_NAME"));
        dao.setCacheKeyGenerator(cacheKeyGenerator);
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();


        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        var resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")), IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")), IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("nobody")), IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(resultsSet);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")), IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, resultsSet.size());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));

        resultsSet = dao.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        this.validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

    }


    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(new HashMap<>());

        return dao;
    }
}
