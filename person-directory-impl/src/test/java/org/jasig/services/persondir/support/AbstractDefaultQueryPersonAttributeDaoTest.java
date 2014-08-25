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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.util.Util;

/**
 * Provides base tests for classes that implement AbstractDefaultAttributePersonAttributeDao.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractDefaultQueryPersonAttributeDaoTest extends AbstractFlatteningPersonAttributeDaoTest {
    /**
     * @see org.jasig.services.persondir.support.AbstractFlatteningPersonAttributeDaoTest#getAbstractFlatteningPersonAttributeDao()
     */
    @Override
    protected final AbstractFlatteningPersonAttributeDao getAbstractFlatteningPersonAttributeDao() {
        return this.getAbstractDefaultQueryPersonAttributeDao();
    }
    
    protected abstract AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao();

    public void testNullDefaultAttributeName() {
        AbstractDefaultAttributePersonAttributeDao dao = getAbstractDefaultQueryPersonAttributeDao();
        try {
            dao.setUsernameAttributeProvider(null);
            fail("Expected Exception on setUsernameAttributeProvider(null)");
        } 
        catch (Exception iae) {
            return;
        }
    }
    
    
    public void testGetAttributesByString() {
        AbstractDefaultAttributePersonAttributeDao dao = new SimpleDefaultQueryPersonAttributeDao();
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("TestAttrName"));
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.put("TestAttrName", Util.list("edalquist"));
        
        assertEquals(expected, dao.getMultivaluedUserAttributes("edalquist"));
    }
    
    private class SimpleDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        public Set<String> getPossibleUserAttributeNames() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
         */
        public Set<String> getAvailableQueryAttributes() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
         */
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
            return Collections.singleton((IPersonAttributes)new AttributeNamedPersonImpl(query));
        }
    }
}
