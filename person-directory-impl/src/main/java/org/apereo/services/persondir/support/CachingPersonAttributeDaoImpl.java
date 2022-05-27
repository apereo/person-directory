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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A configurable caching implementation of {@link IPersonAttributeDao}
 * which caches results from a wrapped IPersonAttributeDao.
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * <tr>
 * <td  valign="top">cachedPersonAttributesDao</td>
 * <td>
 * The {@link IPersonAttributeDao} to delegate
 * queries to on cache misses.
 * </td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td  valign="top">userInfoCache</td>
 * <td>
 * The {@link java.util.Map} to use for result caching. This class does no cache
 * maintenence. It is assumed the underlying Map implementation will ensure the cache
 * is in a good state at all times.
 * </td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td  valign="top">cacheNullResults</td>
 * <td>
 * If the wrapped IPersonAttributeDao returns null for the query should that null
 * value be stored in the cache.
 * </td>
 * <td valign="top">No</td>
 * <td valign="top">false</td>
 * </tr>
 * <tr>
 * <td  valign="top">nullResultsObject</td>
 * <td>
 * If cacheNullResults is set to true this value is stored in the cache for any
 * query that returns null. This is used as a flag so the same query will return
 * null from the cache by seeing this value
 * </td>
 * <td valign="top">No</td>
 * <td valign="top">{@link CachingPersonAttributeDaoImpl#NULL_RESULTS_OBJECT}</td>
 * </tr>
 * </table>
 *
 * @author dgrimwood@unicon.net
 * @author Eric Dalquist
 * @version $Id
 */
public class CachingPersonAttributeDaoImpl extends AbstractDefaultAttributePersonAttributeDao implements BeanNameAware {
    private final Object objectMonitor = new Object();

    protected static final Set<IPersonAttributes> NULL_RESULTS_OBJECT;

    protected Log statsLogger = LogFactory.getLog(this.getClass().getName() + ".statistics");

    private long queries = 0;

    private long misses = 0;

    static {
        NULL_RESULTS_OBJECT = new HashSet<>();
        NULL_RESULTS_OBJECT.add(new SingletonPersonImpl());
    }

    /*
     * The IPersonAttributeDao to delegate cache misses to.
     */
    private IPersonAttributeDao cachedPersonAttributesDao = null;

    /*
     * The cache to store query results in.
     */
    private Map<Serializable, Set<IPersonAttributes>> userInfoCache = null;

    /*
     * If null resutls should be cached
     */
    private boolean cacheNullResults = false;

    /*
     * The Object that should be stored in the cache if cacheNullResults is true
     */
    private Set<IPersonAttributes> nullResultsObject = NULL_RESULTS_OBJECT;

    private String beanName;

    /**
     * @return Returns the cachedPersonAttributesDao.
     */
    public IPersonAttributeDao getCachedPersonAttributesDao() {
        return this.cachedPersonAttributesDao;
    }

    /**
     * The IPersonAttributeDao to cache results from.
     *
     * @param cachedPersonAttributesDao The cachedPersonAttributesDao to set.
     */
    public void setCachedPersonAttributesDao(final IPersonAttributeDao cachedPersonAttributesDao) {
        if (cachedPersonAttributesDao == null) {
            throw new IllegalArgumentException("cachedPersonAttributesDao may not be null");
        }

        this.cachedPersonAttributesDao = cachedPersonAttributesDao;
    }

    /**
     * @return Returns the userInfoCache.
     */
    @JsonIgnore
    public Map<Serializable, Set<IPersonAttributes>> getUserInfoCache() {
        return this.userInfoCache;
    }

    /**
     * The Map to use for caching results. Only get, put and remove are used so the Map may be backed by a real caching
     * implementation.
     *
     * @param userInfoCache The userInfoCache to set.
     */
    @JsonIgnore
    public void setUserInfoCache(final Map<Serializable, Set<IPersonAttributes>> userInfoCache) {
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
     * If null results should be cached to avoid repeating failed lookups. Defaults to false.
     *
     * @param cacheNullResults the cacheNullResults to set
     */
    public void setCacheNullResults(final boolean cacheNullResults) {
        this.cacheNullResults = cacheNullResults;
    }

    /**
     * @return the nullResultsObject
     */
    public Set<IPersonAttributes> getNullResultsObject() {
        return this.nullResultsObject;
    }

    /**
     * Used to specify the placeholder object to put in the cache for null results. Defaults to a minimal Set. Most
     * installations will not need to set this.
     *
     * @param nullResultsObject the nullResultsObject to set
     */
    @JsonIgnore
    public void setNullResultsObject(final Set<IPersonAttributes> nullResultsObject) {
        if (nullResultsObject == null) {
            throw new IllegalArgumentException("nullResultsObject may not be null");
        }

        this.nullResultsObject = nullResultsObject;
    }

    @Override
    public void setBeanName(final String name) {
        this.beanName = name;
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

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> seed,
                                                                     final IPersonAttributeDaoFilter filter) {
        //Ensure the arguments and state are valid
        if (seed == null) {
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        }

        if (this.cachedPersonAttributesDao == null) {
            throw new IllegalStateException("No 'cachedPersonAttributesDao' has been specified.");
        }
        if (this.userInfoCache == null) {
            throw new IllegalStateException("No 'userInfoCache' has been specified.");
        }

        //Get the cache key
        var methodInvocation = new PersonAttributeDaoMethodInvocation(seed);
        var cacheKey = generateKey(methodInvocation);

        if (cacheKey != null) {
            var cacheResults = this.userInfoCache.get(cacheKey);
            if (cacheResults != null) {
                //If the returned object is the null results object, set the cache results to null
                if (this.nullResultsObject.equals(cacheResults)) {
                    cacheResults = null;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Retrieved query from cache for " + beanName + ". key='" + cacheKey + "', results='" + cacheResults + "'");
                }

                this.queries++;
                if (statsLogger.isDebugEnabled()) {
                    statsLogger.debug("Cache Stats " + beanName + ": queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
                }

                return cacheResults;
            }
        }

        var queryResults = this.cachedPersonAttributesDao.getPeopleWithMultivaluedAttributes(seed, filter);

        if (cacheKey != null) {
            if (queryResults != null) {
                this.userInfoCache.put(cacheKey, queryResults);
            } else if (this.cacheNullResults) {
                this.userInfoCache.put(cacheKey, this.nullResultsObject);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved query from wrapped IPersonAttributeDao and stored in cache for "
                             + beanName + ". key='" + cacheKey + "', results='" + queryResults + "'");
            }

            this.queries++;
            this.misses++;
            if (statsLogger.isDebugEnabled()) {
                statsLogger.debug("Cache Stats " + beanName + ": queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
            }
        }

        return queryResults;
    }

    private Serializable generateKey(final PersonAttributeDaoMethodInvocation methodInvocation) {
        return null;
    }

    public void removeUserAttributes(final String uid) {
        Validate.notNull(uid, "uid may not be null.");
        var seed = this.toSeedMap(uid);
        this.removeUserAttributesMultivaluedSeed(seed);
    }

    public void removeUserAttributes(final Map<String, Object> seed) {
        var multiSeed = MultivaluedPersonAttributeUtils.toMultivaluedMap(seed);
        this.removeUserAttributesMultivaluedSeed(multiSeed);
    }

    public void removeUserAttributesMultivaluedSeed(final Map<String, List<Object>> seed) {
        var methodInvocation = new PersonAttributeDaoMethodInvocation(seed);
        var cacheKey = generateKey(methodInvocation);
        this.userInfoCache.remove(cacheKey);
    }

    @Override
    public String[] getId() {
        final List<String> ids = new ArrayList<>();
        ids.add(super.getClass().getSimpleName());
        ids.addAll(Arrays.asList(this.cachedPersonAttributesDao.getId()));
        return ids.toArray(new String[]{});
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return this.cachedPersonAttributesDao.getPossibleUserAttributeNames(filter);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return this.cachedPersonAttributesDao.getAvailableQueryAttributes(filter);
    }

    private static class PersonAttributeDaoMethodInvocation implements MethodInvocation {
        private final static Method getPeopleWithMultivaluedAttributesMethod;

        static {
            try {
                getPeopleWithMultivaluedAttributesMethod = IPersonAttributeDao.class.getMethod("getPeopleWithMultivaluedAttributes",
                    Map.class, IPersonAttributeDaoFilter.class);
            } catch (final SecurityException e) {
                var nsme = new NoSuchMethodError(
                    "The 'getPeopleWithMultivaluedAttributes(" + Map.class + ")' method on the '" + IPersonAttributeDao.class + "' is not accessible due to a security policy.");
                nsme.initCause(e);
                throw nsme;
            } catch (final NoSuchMethodException e) {
                var nsme = new NoSuchMethodError("The 'getPeopleWithMultivaluedAttributes(" + Map.class + ")' method on the '" + IPersonAttributeDao.class + "' does not exist.");
                nsme.initCause(e);
                throw nsme;
            }
        }

        private final Object[] args;

        public PersonAttributeDaoMethodInvocation(final Object... args) {
            this.args = args;
        }

        @Override
        public Method getMethod() {
            return getPeopleWithMultivaluedAttributesMethod;
        }

        @Override
        public Object[] getArguments() {
            return this.args;
        }

        @Override
        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException("This is a fake MethodInvocation, getStaticPart() is not supported.");
        }

        @Override
        public Object getThis() {
            throw new UnsupportedOperationException("This is a fake MethodInvocation, getThis() is not supported.");
        }

        @Override
        public Object proceed() {
            throw new UnsupportedOperationException("This is a fake MethodInvocation, proceed() is not supported.");
        }
    }

    private static final class SingletonPersonImpl extends BasePersonImpl implements Serializable {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        public SingletonPersonImpl() {
            super(new HashMap<>());
        }

        @Override
        public String getName() {
            return CachingPersonAttributeDaoImpl.class.getName() + "UNIQUE_NULL_RESULTS";
        }
    }
}
