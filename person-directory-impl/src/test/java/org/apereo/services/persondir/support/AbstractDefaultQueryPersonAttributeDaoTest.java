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

import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.util.Util;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides base tests for classes that implement AbstractDefaultAttributePersonAttributeDao.
 *
 * @author Eric Dalquist

 */
public abstract class AbstractDefaultQueryPersonAttributeDaoTest extends AbstractFlatteningPersonAttributeDaoTest {
    /**
     * @see AbstractFlatteningPersonAttributeDaoTest#getAbstractFlatteningPersonAttributeDao()
     */
    @Override
    protected final AbstractFlatteningPersonAttributeDao getAbstractFlatteningPersonAttributeDao() {
        return this.getAbstractDefaultQueryPersonAttributeDao();
    }

    protected abstract AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao();

    @Test
    public void testNullDefaultAttributeName() {
        var dao = getAbstractDefaultQueryPersonAttributeDao();
        try {
            dao.setUsernameAttributeProvider(null);
            fail("Expected Exception on setUsernameAttributeProvider(null)");
        } catch (final Exception iae) {
            return;
        }
    }


    @Test
    public void testGetAttributesByString() {
        var dao = new SimpleDefaultQueryPersonAttributeDao();
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("TestAttrName"));
        final Map<String, List<Object>> expected = new HashMap<>();
        expected.put("TestAttrName", Util.list("edalquist"));

        assertEquals(expected, dao.getPerson("edalquist", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes());
    }

    private static class SimpleDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
         */
        @Override
        public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
         */
        @Override
        public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
         */
        @Override
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                         final IPersonAttributeDaoFilter filter) {
            return Collections.singleton((IPersonAttributes) new AttributeNamedPersonImpl(query));
        }
    }
}
