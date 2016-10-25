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
import org.apereo.services.persondir.mock.MapCacheProviderFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Eric Dalquist

 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/cacheKeyGeneratorTestContext.xml"})
public class AttributeBasedCacheKeyGeneratorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testCacheKeyGeneratorWithFactoryBean() {

        final IPersonAttributeDao personAttributeDao = applicationContext.getBean("personAttributeDao", IPersonAttributeDao.class);

        final MapCacheProviderFacade cacheProviderFacade = applicationContext.getBean("cacheProviderFacade", MapCacheProviderFacade.class);

        assertEquals(0, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(0, cacheProviderFacade.getHitCount());
        assertEquals(0, cacheProviderFacade.getMissCount());
        assertEquals(0, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a miss
        personAttributeDao.getMultivaluedUserAttributes("edalquist");
        assertEquals(1, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(0, cacheProviderFacade.getHitCount());
        assertEquals(1, cacheProviderFacade.getMissCount());
        assertEquals(1, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a hit
        personAttributeDao.getMultivaluedUserAttributes("edalquist");
        assertEquals(1, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(1, cacheProviderFacade.getHitCount());
        assertEquals(1, cacheProviderFacade.getMissCount());
        assertEquals(1, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a hit
        personAttributeDao.getMultivaluedUserAttributes(Collections.singletonMap("userName", Collections.singletonList((Object) "edalquist")));
        assertEquals(1, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(2, cacheProviderFacade.getHitCount());
        assertEquals(1, cacheProviderFacade.getMissCount());
        assertEquals(1, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a miss
        personAttributeDao.getUserAttributes("edalquist");
        assertEquals(2, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(2, cacheProviderFacade.getHitCount());
        assertEquals(2, cacheProviderFacade.getMissCount());
        assertEquals(2, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a hit
        personAttributeDao.getUserAttributes("edalquist");
        assertEquals(2, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(3, cacheProviderFacade.getHitCount());
        assertEquals(2, cacheProviderFacade.getMissCount());
        assertEquals(2, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should be a hit
        personAttributeDao.getUserAttributes(Collections.singletonMap("userName", (Object) "edalquist"));
        assertEquals(2, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(4, cacheProviderFacade.getHitCount());
        assertEquals(2, cacheProviderFacade.getMissCount());
        assertEquals(2, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should miss
        personAttributeDao.getPossibleUserAttributeNames();
        assertEquals(3, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(4, cacheProviderFacade.getHitCount());
        assertEquals(3, cacheProviderFacade.getMissCount());
        assertEquals(3, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());

        //Should hit
        personAttributeDao.getPossibleUserAttributeNames();
        assertEquals(3, cacheProviderFacade.getCacheSize());
        assertEquals(0, cacheProviderFacade.getFlushCount());
        assertEquals(5, cacheProviderFacade.getHitCount());
        assertEquals(3, cacheProviderFacade.getMissCount());
        assertEquals(3, cacheProviderFacade.getPutCount());
        assertEquals(0, cacheProviderFacade.getRemoveCount());
    }
}
