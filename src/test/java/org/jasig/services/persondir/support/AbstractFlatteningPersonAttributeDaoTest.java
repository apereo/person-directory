/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.Util;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractFlatteningPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {
    /**
     * @see org.jasig.services.persondir.support.AbstractPersonAttributeDaoTest#getPersonAttributeDaoInstance()
     */
    @Override
    protected final IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.getAbstractFlatteningPersonAttributeDao();
    }
    
    protected abstract AbstractFlatteningPersonAttributeDao getAbstractFlatteningPersonAttributeDao();
    
    
    public void testFlattenMap() throws Exception {
        Map<String, List<Object>> backingMap = new HashMap<String, List<Object>>();
        backingMap.put("name", Util.list("edalquist"));
        backingMap.put("emails", Util.list("edalquist@foo.com", "ebd@none.org"));
        backingMap.put("phone", Util.list((Object)null));
        backingMap.put("title", null);
        backingMap.put("address", Collections.emptyList());
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", "edalquist");
        expected.put("emails", "edalquist@foo.com");
        expected.put("phone", null);
        expected.put("title", null);
        expected.put("address", null);
        
        
        
        final SimpleDefaultQueryPersonAttributeDao flatteningPersonAttributeDao = new SimpleDefaultQueryPersonAttributeDao(backingMap);
        
        final Map<String, Object> userAttributesUid = flatteningPersonAttributeDao.getUserAttributes("seed");
        assertEquals(expected, userAttributesUid);
        
        final Map<String, Object> userAttributesMap = flatteningPersonAttributeDao.getUserAttributes(Collections.singletonMap("key", new Object()));
        assertEquals(expected, userAttributesMap);
    }
    

    
    private class SimpleDefaultQueryPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
        private Map<String, List<Object>> backingMap;
        
        public SimpleDefaultQueryPersonAttributeDao(Map<String, List<Object>> backingMap) {
            this.backingMap = backingMap;
        }
        
        public Map<String, List<Object>> getMultivaluedUserAttributes(String uid) {
            return this.backingMap;
        }
        
        public Map<String, List<Object>> getMultivaluedUserAttributes(Map<String, List<Object>> seed) {
            return this.backingMap;
        }

        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }
    }
}
