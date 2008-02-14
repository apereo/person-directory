/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.mock;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;
import org.springmodules.cache.provider.ehcache.EhCacheCachingModel;
import org.springmodules.cache.provider.ehcache.EhCacheFlushingModel;
import org.springmodules.cache.provider.ehcache.EhCacheModelValidator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MapCacheProviderFacade extends AbstractCacheProviderFacade {
    final Map<Serializable, Object> cache = new HashMap<Serializable, Object>();
    
    private int hitCount = 0;
    private int missCount = 0;
    private int flushCount = 0;
    private int putCount = 0;
    private int removeCount = 0;
    
    /**
     * @return the hitCount
     */
    public int getHitCount() {
        return hitCount;
    }
    /**
     * @return the missCount
     */
    public int getMissCount() {
        return missCount;
    }
    /**
     * @return the flushCount
     */
    public int getFlushCount() {
        return flushCount;
    }
    /**
     * @return the putCount
     */
    public int getPutCount() {
        return putCount;
    }
    /**
     * @return the removeCount
     */
    public int getRemoveCount() {
        return removeCount;
    }
    /**
     * @return the cacheSize
     */
    public int getCacheSize() {
        return this.cache.size();
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#isSerializableCacheElementRequired()
     */
    @Override
    protected boolean isSerializableCacheElementRequired() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#onFlushCache(org.springmodules.cache.FlushingModel)
     */
    @Override
    protected void onFlushCache(FlushingModel model) throws CacheException {
        this.flushCount++;
        this.cache.clear();
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#onGetFromCache(java.io.Serializable, org.springmodules.cache.CachingModel)
     */
    @Override
    protected Object onGetFromCache(Serializable key, CachingModel model) throws CacheException {
        if (this.cache.containsKey(key)) {
            this.hitCount++;
        }
        else {
            this.missCount++;
        }
        
        return this.cache.get(key);
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#onPutInCache(java.io.Serializable, org.springmodules.cache.CachingModel, java.lang.Object)
     */
    @Override
    protected void onPutInCache(Serializable key, CachingModel model, Object obj) throws CacheException {
        this.putCount++;
        this.cache.put(key, obj);
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#onRemoveFromCache(java.io.Serializable, org.springmodules.cache.CachingModel)
     */
    @Override
    protected void onRemoveFromCache(Serializable key, CachingModel model) throws CacheException {
        this.removeCount++;
        this.cache.remove(key);
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.AbstractCacheProviderFacade#validateCacheManager()
     */
    @Override
    protected void validateCacheManager() throws FatalCacheException {
        //noop
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.CacheProviderFacade#getCachingModelEditor()
     */
    public PropertyEditor getCachingModelEditor() {
        ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
        editor.setCacheModelClass(EhCacheCachingModel.class);
        return editor;
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
     */
    public PropertyEditor getFlushingModelEditor() {
        ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
        editor.setCacheModelClass(EhCacheFlushingModel.class);
        return editor;
    }

    /* (non-Javadoc)
     * @see org.springmodules.cache.provider.CacheProviderFacade#modelValidator()
     */
    public CacheModelValidator modelValidator() {
        return new EhCacheModelValidator();
    }
}
