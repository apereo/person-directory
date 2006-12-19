/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.services.persondir.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * A configurable caching implementation of {@link IPersonAttributeDao} 
 * which caches results from a wrapped IPersonAttributeDao. 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cachedPersonAttributesDao</td>
 *         <td>
 *             The {@link org.jasig.portal.services.persondir.IPersonAttributeDao} to delegate
 *             queries to on cache misses.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">userInfoCache</td>
 *         <td>
 *             The {@link java.util.Map} to use for result caching. This class does no cache
 *             maintenence. It is assumed the underlying Map implementation will ensure the cache
 *             is in a good state at all times.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cacheKeyAttributes</td>
 *         <td>
 *             A Set of attribute names to use when building the cache key. The default
 *             implementation generates the key as a Map of attributeNames to values retrieved
 *             from the seed for the query. Zero length sets are treaded as null.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cacheNullResults</td>
 *         <td>
 *             If the wrapped IPersonAttributeDao returns null for the query should that null
 *             value be stored in the cache. 
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">nullResultsObject</td>
 *         <td>
 *             If cacheNullResults is set to true this value is stored in the cache for any
 *             query that returns null. This is used as a flag so the same query will return
 *             null from the cache by seeing this value
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link CachingPersonAttributeDaoImpl#NULL_RESULTS_OBJECT}</td>
 *     </tr>
 * </table>
 * 
 * 
 * @author dgrimwood@unicon.net
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Id
 */
public class CachingPersonAttributeDaoImpl extends AbstractDefaultAttributePersonAttributeDao {
    protected static final Map NULL_RESULTS_OBJECT = Collections.singletonMap(CachingPersonAttributeDaoImpl.class.getName() + "UNIQUE_NULL_RESULTS_MAP", new Integer(CachingPersonAttributeDaoImpl.class.hashCode()));
    
    protected Log statsLogger = LogFactory.getLog(this.getClass().getName() + ".statistics");

    private long queries = 0;
    private long misses = 0;
    
    /*
     * The IPersonAttributeDao to delegate cache misses to.
     */
    private IPersonAttributeDao cachedPersonAttributesDao = null;
    
    /*
     * The cache to store query results in.
     */
    private Map userInfoCache = null; 
    
    /*
     * The set of attributes to use to generate the cache key.
     */
    private Set cacheKeyAttributes = null;
    
    /*
     * If null resutls should be cached
     */
    private boolean cacheNullResults = false;
    
    /*
     * The Object that should be stored in the cache if cacheNullResults is true
     */
    private Map nullResultsObject = NULL_RESULTS_OBJECT;
    
    /**
     * @return Returns the cachedPersonAttributesDao.
     */
    public IPersonAttributeDao getCachedPersonAttributesDao() {
        return this.cachedPersonAttributesDao;
    }
    /**
     * @param cachedPersonAttributesDao The cachedPersonAttributesDao to set.
     */
    public void setCachedPersonAttributesDao(IPersonAttributeDao cachedPersonAttributesDao) {
        if (cachedPersonAttributesDao == null) {
            throw new IllegalArgumentException("cachedPersonAttributesDao may not be null");
        }

        this.cachedPersonAttributesDao = cachedPersonAttributesDao;
    }
    
    /**
     * @return Returns the cacheKeyAttributes.
     */
    public Set getCacheKeyAttributes() {
        return this.cacheKeyAttributes;
    }
    /**
     * @param cacheKeyAttributes The cacheKeyAttributes to set.
     */
    public void setCacheKeyAttributes(Set cacheKeyAttributes) {
        this.cacheKeyAttributes = cacheKeyAttributes;
    }

    /**
     * @return Returns the userInfoCache.
     */
    public Map getUserInfoCache() {
        return this.userInfoCache;
    }
    /**
     * @param userInfoCache The userInfoCache to set.
     */
    public void setUserInfoCache(Map userInfoCache) {
        if (userInfoCache == null) {
            throw new IllegalArgumentException("userInfoCache may not be null");
        }

        this.userInfoCache = userInfoCache;
    }
    
    /**
     * @return the cacheNullResults
     */
    public boolean isCacheNullResults() {
        return this.cacheNullResults;
    }
    /**
     * @param cacheNullResults the cacheNullResults to set
     */
    public void setCacheNullResults(boolean cacheNullResults) {
        this.cacheNullResults = cacheNullResults;
    }
    
    /**
     * @return the nullResultsObject
     */
    public Map getNullResultsObject() {
        return this.nullResultsObject;
    }
    /**
     * @param nullResultsObject the nullResultsObject to set
     */
    public void setNullResultsObject(Map nullResultsObject) {
        if (nullResultsObject == null) {
            throw new IllegalArgumentException("nullResultsObject may not be null");
        }

        this.nullResultsObject = nullResultsObject;
    }
    
    
    /**
     * @return Returns the number of cache misses.
     */
    public long getMisses() {
        return this.misses;
    }
    
    /**
     * @return Returns the number of queries.
     */
    public long getQueries() {
        return this.queries;
    }
    
    
    /**
     * Wraps the call to the specified cachedPersonAttributesDao IPersonAttributeDao delegate with
     * a caching layer. Results are cached using keys generated by {@link #getCacheKey(Map)}.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(Map seed) {
        //Ensure the arguements and state are valid
        if (seed == null) {
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        }
        
        if (this.cachedPersonAttributesDao == null) {
            throw new IllegalStateException("No 'cachedPersonAttributesDao' has been specified.");
        }
        if (this.userInfoCache == null) {
            throw new IllegalStateException("No 'userInfoCache' has been specified.");
        }
        
        final Object cacheKey = this.getCacheKey(seed);
        
        if (cacheKey != null) {
            Map cacheResults = (Map)this.userInfoCache.get(cacheKey);
            if (cacheResults != null) {
                //If the returned object is the null results object, set the cache results to null
                if (this.nullResultsObject.equals(cacheResults)) {
                    cacheResults = null;
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Retrieved query from cache. key='" + cacheKey + "', results='" + cacheResults + "'");
                }
                    
                this.queries++;
                if (statsLogger.isDebugEnabled()) {
                    statsLogger.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
                }
                
                return cacheResults;
            }
        
            final Map queryResults = this.cachedPersonAttributesDao.getUserAttributes(seed);
        
            if (queryResults != null) {
                this.userInfoCache.put(cacheKey, queryResults);
            }
            else if (this.cacheNullResults) {
                this.userInfoCache.put(cacheKey, this.nullResultsObject);
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved query from wrapped IPersonAttributeDao and stored in cache. key='" + cacheKey + "', results='" + queryResults + "'");
            }
            
            this.queries++;
            this.misses++;
            if (statsLogger.isDebugEnabled()) {
                statsLogger.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
            }

            return queryResults;
        }
        else {
            logger.warn("No cache key generated, caching disabled for this query. query='" + seed + "', cacheKeyAttributes=" + this.cacheKeyAttributes + "', defaultAttributeName='" + this.getDefaultAttributeName() + "'");

            this.queries++;
            this.misses++;
            if (statsLogger.isDebugEnabled()) {
                statsLogger.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
            }
            
            return this.cachedPersonAttributesDao.getUserAttributes(seed);
        }
    }

    /**
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.cachedPersonAttributesDao.getPossibleUserAttributeNames();
    }
    
    /**
     * Generates a Serializable cache key from the seed parameters according to the documentation
     * of this class. If the return value is NULL caching will be disabled for this query.
     * 
     * @param querySeed The query to base the key on.
     * @return A Serializable cache key.
     */
    protected Serializable getCacheKey(Map querySeed) {
        final HashMap cacheKey = new HashMap();
        
        if (this.cacheKeyAttributes == null || this.cacheKeyAttributes.size() == 0) {
            final String defaultAttrName = this.getDefaultAttributeName();
            
            if (querySeed.containsKey(defaultAttrName)) {
                cacheKey.put(defaultAttrName, querySeed.get(defaultAttrName));
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Created cacheKey='" + cacheKey + "' from query='" + querySeed + "' using default attribute='" + defaultAttrName + "'");
            }
        }
        else {
            for (final Iterator attrItr = this.cacheKeyAttributes.iterator(); attrItr.hasNext();) {
                final String attr = (String)attrItr.next();
                
                if (querySeed.containsKey(attr)) {
                    cacheKey.put(attr, querySeed.get(attr));
                }
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Created cacheKey='" + cacheKey + "' from query='" + querySeed + "' using attributes='" + this.cacheKeyAttributes + "'");
            }
        }
        
        if (cacheKey.size() > 0) {
            return cacheKey;
        }
        else {
            return null;
        }
    }
}
