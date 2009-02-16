/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.Collections;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.mock.MapCacheProviderFacade;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractSingleSpringContextTests;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@SuppressWarnings("deprecation")
public class AttributeBasedCacheKeyGeneratorTest extends AbstractSingleSpringContextTests {
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigPath()
     */
    @Override
    protected String getConfigPath() {
        return "/cacheKeyGeneratorTestContext.xml";
    }

    public void testCacheKeyGeneratorWithFactoryBean() {
        final ConfigurableApplicationContext applicationContext = this.getApplicationContext();
        
        final IPersonAttributeDao personAttributeDao = (IPersonAttributeDao)applicationContext.getBean("personAttributeDao", IPersonAttributeDao.class);
        
        final MapCacheProviderFacade cacheProviderFacade = (MapCacheProviderFacade)applicationContext.getBean("cacheProviderFacade", MapCacheProviderFacade.class);
        
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
        personAttributeDao.getMultivaluedUserAttributes(Collections.singletonMap("userName", Collections.singletonList((Object)"edalquist")));
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
        personAttributeDao.getUserAttributes(Collections.singletonMap("userName", (Object)"edalquist"));
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
