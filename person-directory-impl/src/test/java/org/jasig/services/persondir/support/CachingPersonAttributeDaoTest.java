/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.util.Util;



/**
 * @author Eric Dalquist
 * @version $Revision$
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
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(new HashMap<Serializable, Set<IPersonAttributes>>());
        dao.afterPropertiesSet();
        
        assertEquals("Query count incorrect", 0, dao.getQueries());
        assertEquals("Miss count incorrect", 0, dao.getMisses());
        
        Map<String, List<Object>> result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 1, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 2, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("nobody");
        assertNull(result);
        assertEquals("Query count incorrect", 3, dao.getQueries());
        assertEquals("Miss count incorrect", 2, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("awp9");
        this.validateUser2(result);
        assertEquals("Query count incorrect", 4, dao.getQueries());
        assertEquals("Miss count incorrect", 3, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("nobody");
        assertNull(result);
        assertEquals("Query count incorrect", 5, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("awp9");
        this.validateUser2(result);
        assertEquals("Query count incorrect", 6, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 7, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
    }
    
    public void testCaching() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();
        
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();
        
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map<String, List<Object>> result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("nobody");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        //Enable null result caching
        dao.setCacheNullResults(true);
        result = dao.getMultivaluedUserAttributes("nobody");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
        
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));
        
        result = dao.getMultivaluedUserAttributes(queryMap);
        this.validateUser1(result);
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
        
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setCacheKeyAttributes(keyAttrs);
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();
        
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map<String, List<Object>> result = dao.getMultivaluedUserAttributes("edalquist");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("nobody");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        result = dao.getMultivaluedUserAttributes("edalquist");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        final Map<String, List<Object>> queryMap1 = new HashMap<>();
        queryMap1.put(defaultAttr, Util.list("edalquist"));
        queryMap1.put("name.first", Util.list("Eric"));
        queryMap1.put("name.last", Util.list("Dalquist"));
        
        result = dao.getMultivaluedUserAttributes(queryMap1);
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        
        final Map<String, List<Object>> queryMap2 = new HashMap<>();
        queryMap2.put("name.first", Util.list("John"));
        queryMap2.put("name.last", Util.list("Doe"));
        
        result = dao.getMultivaluedUserAttributes(queryMap2);
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        
        result = dao.getMultivaluedUserAttributes(queryMap1);
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
    }
    
    public void testPropertyConstraints() {
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        
        try {
            dao.setCachedPersonAttributesDao(null);
            fail("setCachedPersonAttributesDao(null) should have thrown an IllegalArgumentException.");
        }
        catch (final IllegalArgumentException iae) {
            //expected
        }
        dao.setCachedPersonAttributesDao(this.stubDao);
        assertEquals(this.stubDao, dao.getCachedPersonAttributesDao());
        
        
        dao.setCacheKeyAttributes(null);
        assertNull(dao.getCacheKeyAttributes());
        final Set<String> keyAttrs = new HashSet<>();
        keyAttrs.add("name.first");
        keyAttrs.add("name.last");
        final Set<String> expectedAttrs = new HashSet<>(keyAttrs);
        dao.setCacheKeyAttributes(keyAttrs);
        assertEquals(expectedAttrs, dao.getCacheKeyAttributes());
        
        
        try {
            dao.setUserInfoCache(null);
            fail("setUserInfoCache(null) should have thrown an IllegalArgumentException.");
        }
        catch (final IllegalArgumentException iae) {
            //expected
        }
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();
        final Map<Serializable, Set<IPersonAttributes>> expectedcacheMap = new HashMap<>(cacheMap);
        dao.setUserInfoCache(cacheMap);
        assertEquals(expectedcacheMap, dao.getUserInfoCache());
    }
    
    @SuppressWarnings("unchecked")
    public void testUninitializedDao() {
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        try {
            dao.getMultivaluedUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        }
        catch (final IllegalStateException ise) {
            //expected
        }
        
        dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        try {
            dao.getMultivaluedUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with only setCachedPersonAttributesDao initialized should result in an IllegalStateException");
        }
        catch (final IllegalStateException ise) {
            //expected
        }
        
        dao = new CachingPersonAttributeDaoImpl();
        dao.setUserInfoCache(new HashMap<Serializable, Set<IPersonAttributes>>());
        try {
            dao.getMultivaluedUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        }
        catch (final IllegalStateException ise) {
            //expected
        }
    }
    
    public void testEmptyCacheKeyWithDefaultAttr() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();
        
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map<String, List<Object>> results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("nobody")));
        assertNull(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));
        
        results = dao.getMultivaluedUserAttributes(queryMap);
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
    }
    
    public void testEmptyCacheKeyWithKeyAttrs() throws Exception {
        final Map<Serializable, Set<IPersonAttributes>> cacheMap = new HashMap<>();
        
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        dao.setCacheKeyAttributes(Collections.singleton("UNUSED_ATTR_NAME"));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();
        
        
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map<String, List<Object>> results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("nobody")));
        assertNull(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getMultivaluedUserAttributes(Collections.singletonMap(defaultAttr, Util.list("edalquist")));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        
        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put(defaultAttr, Util.list("edalquist"));
        queryMap.put("name.first", Util.list("Eric"));
        queryMap.put("name.last", Util.list("Dalquist"));
        
        results = dao.getMultivaluedUserAttributes(queryMap);
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

    }


    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(defaultAttr));
        dao.setUserInfoCache(new HashMap<Serializable, Set<IPersonAttributes>>());
        
        return dao;
    }
}
