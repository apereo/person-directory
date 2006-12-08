/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class CachingPersonAttributeDaoTest extends AbstractDefaultQueryPersonAttributeDaoTest {
    private static final String defaultAttr = "uid"; 

    private ComplexStubPersonAttributeDao stubDao;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.stubDao = new ComplexStubPersonAttributeDao();
        
        super.setUp();
        
        Map daoBackingMap = new HashMap();
        
        Map user1 = new HashMap();
        user1.put("phone", "777-7777");
        user1.put("displayName", "Display Name");
        daoBackingMap.put("edalquist", user1);
        
        Map user2 = new HashMap();
        user2.put("phone", "888-8888");
        user2.put("displayName", "");
        daoBackingMap.put("awp9", user2);
        
        Map user3 = new HashMap();
        user3.put("phone", "666-6666");
        user3.put("givenName", "Howard");
        daoBackingMap.put("erider", user3);
        
        
        this.stubDao.setBackingMap(daoBackingMap);
        this.stubDao.setDefaultAttributeName(defaultAttr);
        
        super.setUp();
    }
    
    private void validateUser1(Map attrs) {
        assertNotNull("Attribute Map for User1 May not be null", attrs);
        assertEquals("777-7777", attrs.get("phone"));
        assertEquals("Display Name", attrs.get("displayName"));
    }
    
    private void validateUser2(Map attrs) {
        assertNotNull("Attribute Map for User2 May not be null", attrs);
        assertEquals("888-8888", attrs.get("phone"));
        assertEquals("", attrs.get("displayName"));
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.stubDao = null;
        
        super.tearDown();
    }
    
    
    public void testCacheStats() {
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setDefaultAttributeName(defaultAttr);
        dao.setUserInfoCache(new HashMap());
        
        assertEquals("Query count incorrect", 0, dao.getQueries());
        assertEquals("Miss count incorrect", 0, dao.getMisses());
        
        Map result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 1, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());
        
        result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 2, dao.getQueries());
        assertEquals("Miss count incorrect", 1, dao.getMisses());
        
        result = dao.getUserAttributes("nobody");
        assertNull(result);
        assertEquals("Query count incorrect", 3, dao.getQueries());
        assertEquals("Miss count incorrect", 2, dao.getMisses());
        
        result = dao.getUserAttributes("awp9");
        this.validateUser2(result);
        assertEquals("Query count incorrect", 4, dao.getQueries());
        assertEquals("Miss count incorrect", 3, dao.getMisses());
        
        result = dao.getUserAttributes("nobody");
        assertNull(result);
        assertEquals("Query count incorrect", 5, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
        
        result = dao.getUserAttributes("awp9");
        this.validateUser2(result);
        assertEquals("Query count incorrect", 6, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
        
        result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Query count incorrect", 7, dao.getQueries());
        assertEquals("Miss count incorrect", 4, dao.getMisses());
    }
    
    public void testCaching() {
        Map cacheMap = new HashMap();
        
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setDefaultAttributeName(defaultAttr);
        dao.setUserInfoCache(cacheMap);
        
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        result = dao.getUserAttributes("nobody");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
        
        result = dao.getUserAttributes("edalquist");
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
        
        
        Map queryMap = new HashMap();
        queryMap.put(defaultAttr, "edalquist");
        queryMap.put("name.first", "Eric");
        queryMap.put("name.last", "Dalquist");
        
        result = dao.getUserAttributes(queryMap);
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
    }
    

    
    public void testMulipleAttributeKeys() {
        Map cacheMap = new HashMap();
        
        Set keyAttrs = new HashSet();
        keyAttrs.add("name.first");
        keyAttrs.add("name.last");
        
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setCacheKeyAttributes(keyAttrs);
        dao.setUserInfoCache(cacheMap);
        
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map result = dao.getUserAttributes("edalquist");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        result = dao.getUserAttributes("nobody");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        result = dao.getUserAttributes("edalquist");
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map queryMap1 = new HashMap();
        queryMap1.put(defaultAttr, "edalquist");
        queryMap1.put("name.first", "Eric");
        queryMap1.put("name.last", "Dalquist");
        
        result = dao.getUserAttributes(queryMap1);
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 1, cacheMap.size());
        
        
        Map queryMap2 = new HashMap();
        queryMap2.put("name.first", "John");
        queryMap2.put("name.last", "Doe");
        
        result = dao.getUserAttributes(queryMap2);
        assertNull(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
        
        
        result = dao.getUserAttributes(queryMap1);
        this.validateUser1(result);
        assertEquals("Incorrect number of items in cache", 2, cacheMap.size());
    }
    
    public void testPropertyConstraints() {
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        
        try {
            dao.setCachedPersonAttributesDao(null);
            fail("setCachedPersonAttributesDao(null) should have thrown an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //expected
        }
        dao.setCachedPersonAttributesDao(this.stubDao);
        assertEquals(this.stubDao, dao.getCachedPersonAttributesDao());
        
        
        dao.setCacheKeyAttributes(null);
        assertNull(dao.getCacheKeyAttributes());
        final Set keyAttrs = new HashSet();
        keyAttrs.add("name.first");
        keyAttrs.add("name.last");
        final Set expectedAttrs = new HashSet(keyAttrs);
        dao.setCacheKeyAttributes(keyAttrs);
        assertEquals(expectedAttrs, dao.getCacheKeyAttributes());
        
        
        try {
            dao.setUserInfoCache(null);
            fail("setUserInfoCache(null) should have thrown an IllegalArgumentException.");
        }
        catch (IllegalArgumentException iae) {
            //expected
        }
        final Map cacheMap = new HashMap();
        final Map expectedcacheMap = new HashMap(cacheMap);
        dao.setUserInfoCache(cacheMap);
        assertEquals(expectedcacheMap, dao.getUserInfoCache());
    }
    
    public void testUninitializedDao() {
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        try {
            dao.getUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        try {
            dao.getUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with only setCachedPersonAttributesDao initialized should result in an IllegalStateException");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        dao = new CachingPersonAttributeDaoImpl();
        dao.setUserInfoCache(new HashMap());
        try {
            dao.getUserAttributes(Collections.EMPTY_MAP);
            fail("Calling getUserAttributes(Map) with no initialization should result in an IllegalStateException");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testNullCacheKeyInSeed() {
        Map cacheMap = new HashMap();
        
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setDefaultAttributeName("UNUSED_ATTR_NAME");
        dao.setUserInfoCache(cacheMap);

        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        Map results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "nobody"));
        assertNull(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        
        Map queryMap = new HashMap();
        queryMap.put(defaultAttr, "edalquist");
        queryMap.put("name.first", "Eric");
        queryMap.put("name.last", "Dalquist");
        
        results = dao.getUserAttributes(queryMap);
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        
        
        
        dao.setCacheKeyAttributes(Collections.singleton("UNUSED_ATTR_NAME"));


        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "nobody"));
        assertNull(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(Collections.singletonMap(defaultAttr, "edalquist"));
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());
        
        results = dao.getUserAttributes(queryMap);
        this.validateUser1(results);
        assertEquals("Incorrect number of items in cache", 0, cacheMap.size());

    }


    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        CachingPersonAttributeDaoImpl dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(this.stubDao);
        dao.setDefaultAttributeName(defaultAttr);
        dao.setUserInfoCache(new HashMap());
        
        return dao;
    }
}
