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
import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.util.CollectionsUtil;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides a base set of implementations and properties for IPersonAttributeDao
 * implementations that aggregate results from a sub List of IPersonAttributeDaos.
 *
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
 * <td  valign="top">personAttributeDaos</td>
 * <td>
 * A {@link List} of {@link IPersonAttributeDao}s to aggregate attributes from.
 * </td>
 * <td valign="top">Yes</td>
 * <td valign="top">null</td>
 * </tr>
 * <tr>
 * <td  valign="top">attrMerger</td>
 * <td>
 * A {@link IAttributeMerger} strategy to use for merging the attributes from
 * the {@link List} of {@link IPersonAttributeDao}s.
 * </td>
 * <td valign="top">No</td>
 * <td valign="top">{@link MultivaluedAttributeMerger}</td>
 * </tr>
 * <tr>
 * <td  valign="top">recoverExceptions</td>
 * <td>
 * Sets the action to be taken if one of the {@link IPersonAttributeDao}s in the
 * {@link List} fails with a {@link RuntimeException}. If set to true a warn level
 * log message and stack trace will be logged. If set to false an error level
 * message and stack trace will be logged and the exception will re-thrown.
 * </td>
 * <td valign="top">No</td>
 * <td valign="top">true</td>
 * </tr>
 * <tr>
 * <td  valign="top">stopOnSuccess</td>
 * <td>
 * If true iteration of the child DAOs will stop after the first one that returns without
 * throwing an exception. This is intended to provide fail-over among attribute sources. The
 * <b>recoverExceptions</b> should be set to true for this to function as expected
 * </td>
 * <td valign="top">No</td>
 * <td valign="top">false</td>
 * </tr>
 * </table>
 *
 * @author Eric Dalquist
 */
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    /**
     * A List of child IPersonAttributeDao instances which we will poll in order.
     */
    protected List<IPersonAttributeDao> personAttributeDaos;

    /**
     * Strategy for merging together the results from successive PersonAttributeDaos.
     */
    protected IAttributeMerger attrMerger = new MultivaluedAttributeMerger();

    /**
     * True if we should catch, logger, and ignore Throwables propogated by
     * individual DAOs.
     */
    protected boolean recoverExceptions = true;

    /**
     * The Stop on success.
     */
    protected boolean stopOnSuccess = false;


    @Override
    public String[] getId() {
        final List<String> ids = new ArrayList<>();
        ids.add(super.getClass().getSimpleName());
        personAttributeDaos.forEach(dao -> ids.addAll(Arrays.asList(dao.getId())));
        return ids.toArray(new String[]{});
    }

    /**
     * Iterates through the configured {@link java.util.List} of {@link IPersonAttributeDao}
     * instances. The results from each DAO are merged into the result {@link Map}
     * by the configured {@link IAttributeMerger}.
     *
     * @see IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map, IPersonAttributeDaoFilter)
     */
    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter) {
        Validate.notNull(query, "query may not be null.");

        //Initialize null, so that if none of the sub-DAOs find any people null is returned appropriately
        Set<IPersonAttributes> resultPeople = null;

        //Denotes that this is the first time we are running a query and the original seed should be used
        var isFirstQuery = true;

        if (this.personAttributeDaos == null) {
            throw new IllegalStateException("personAttributeDaos must be set");
        }

        //Iterate through the configured IPersonAttributeDaos, querying each.
        for (final var currentlyConsidering : this.personAttributeDaos) {
            if (filter != null && !filter.choosePersonAttributeDao(currentlyConsidering)) {
                continue;
            }

            var handledException = false;
            Set<IPersonAttributes> currentPeople = null;
            try {
                currentPeople = this.getAttributesFromDao(query, isFirstQuery, currentlyConsidering, resultPeople, filter);
                isFirstQuery = false;

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Retrieved attributes='" + currentPeople + "' for query='"
                        + query + "', isFirstQuery=" + isFirstQuery + ", currentlyConsidering='"
                        + currentlyConsidering + "', resultAttributes='" + resultPeople + "'");
                }
            } catch (final RuntimeException rte) {
                handledException |= handleRuntimeException(currentlyConsidering, rte);
            }

            if (currentPeople != null) {
                if (resultPeople == null) {
                    //If this is the first valid result set just use it.
                    resultPeople = new LinkedHashSet<>(currentPeople);
                } else {
                    //Merge the Sets of IPersons
                    resultPeople = this.attrMerger.mergeResults(resultPeople, currentPeople);
                }
            }

            if (this.stopOnSuccess && !handledException) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Successfully retrieved attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                }

                break;
            }
        }

        if (resultPeople == null) {
            return null;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Aggregated search results '" + resultPeople + "' for query='" + query + "'");
        }

        return CollectionsUtil.safelyWrapAsUnmodifiableSet(resultPeople);
    }

    /**
     * Handle runtime exception boolean.
     *
     * @param currentlyConsidering the currently considering
     * @param rte                  the rte
     * @return the boolean
     */
    private boolean handleRuntimeException(IPersonAttributeDao currentlyConsidering, RuntimeException rte) {
        if (this.recoverExceptions) {
            this.logger.warn("Recovering From Exception thrown by '" + currentlyConsidering + "'", rte);
            return true;
        } else {
            this.logger.error("Failing From Exception thrown by '" + currentlyConsidering + "'", rte);
            throw rte;
        }
    }


    /**
     * Call to execute the appropriate query on the current {@link IPersonAttributeDao}. Provides extra information
     * beyond the seed for the state of the query chain and previous results.
     *
     * @param seed                 The seed for the original query.
     * @param isFirstQuery         If this is the first query, this will stay true until a call to this method returns (does not throw an exception).
     * @param currentlyConsidering The IPersonAttributeDao to execute the query on.
     * @param resultPeople         The Map of results from all previous queries, may be null.
     * @param filter               the filter
     * @return The results from the call to the DAO.
     */
    protected abstract Set<IPersonAttributes> getAttributesFromDao(Map<String, List<Object>> seed, boolean isFirstQuery,
                                                                   IPersonAttributeDao currentlyConsidering,
                                                                   Set<IPersonAttributes> resultPeople,
                                                                   IPersonAttributeDaoFilter filter);


    /**
     * Merges the results of calling {@link IPersonAttributeDao#getPossibleUserAttributeNames(IPersonAttributeDaoFilter)} on each child dao using
     * the configured {@link IAttributeMerger#mergePossibleUserAttributeNames(Set, Set)}. If all children return null
     * this method returns null as well. If any child does not return null this method will not return null.
     *
     * @see IPersonAttributeDao#getPossibleUserAttributeNames(IPersonAttributeDaoFilter)
     */
    @Override
    @JsonIgnore
    public final Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        Set<String> attrNames = null;

        for (final var currentDao : this.personAttributeDaos) {

            if (filter != null && !filter.choosePersonAttributeDao(currentDao)) {
                continue;
            }
            var handledException = false;
            Set<String> currentDaoAttrNames = null;
            try {
                currentDaoAttrNames = currentDao.getPossibleUserAttributeNames(filter);

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Retrieved possible attribute names '" + currentDaoAttrNames + "' from '" + currentDao + "'");
                }
            } catch (final RuntimeException rte) {
                handledException |= handleRuntimeException(currentDao, rte);
            }

            if (currentDaoAttrNames != null) {
                if (attrNames == null) {
                    attrNames = new LinkedHashSet<>();
                }

                attrNames = this.attrMerger.mergePossibleUserAttributeNames(attrNames, currentDaoAttrNames);
            }

            if (this.stopOnSuccess && !handledException) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Successfully retrieved possible user attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                }

                break;
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Aggregated possible attribute names '" + attrNames + "'");
        }

        if (attrNames == null) {
            return null;
        }

        return CollectionsUtil.safelyWrapAsUnmodifiableSet(attrNames);
    }

    /**
     * Merges the results of calling {@link IPersonAttributeDao#getAvailableQueryAttributes(IPersonAttributeDaoFilter)} on each child dao using
     * the configured {@link IAttributeMerger#mergeAvailableQueryAttributes(Set, Set)}. If all children return null this
     * method returns null as well. If any child does not return null this method will not return null.
     *
     * @see IPersonAttributeDao#getAvailableQueryAttributes(IPersonAttributeDaoFilter)
     */
    @JsonIgnore
    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        Set<String> queryAttrs = null;

        for (final var currentDao : this.personAttributeDaos) {
            if (filter != null && !filter.choosePersonAttributeDao(currentDao)) {
                continue;
            }
            var handledException = false;
            Set<String> currentDaoQueryAttrs = null;
            try {
                currentDaoQueryAttrs = currentDao.getAvailableQueryAttributes(filter);

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Retrieved possible query attributes '" + currentDaoQueryAttrs + "' from '" + currentDao + "'");
                }
            } catch (final RuntimeException rte) {
                handledException |= handleRuntimeException(currentDao, rte);
            }

            if (currentDaoQueryAttrs != null) {
                if (queryAttrs == null) {
                    queryAttrs = new LinkedHashSet<>();
                }

                queryAttrs = this.attrMerger.mergeAvailableQueryAttributes(queryAttrs, currentDaoQueryAttrs);
            }

            if (this.stopOnSuccess && !handledException) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Successfully retrieved available query attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                }

                break;
            }
        }


        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Aggregated possible query attributes '" + queryAttrs + "'");
        }

        if (queryAttrs == null) {
            return null;
        }

        return CollectionsUtil.safelyWrapAsUnmodifiableSet(queryAttrs);
    }

    /**
     * Get the strategy whereby we accumulate attributes.
     *
     * @return Returns the attrMerger.
     */
    public final IAttributeMerger getMerger() {
        return this.attrMerger;
    }

    /**
     * Set the strategy whereby we accumulate attributes from the results of
     * polling our delegates.
     *
     * @param merger The attrMerger to set.
     * @throws IllegalArgumentException If merger is <code>null</code>.
     */
    public final void setMerger(final IAttributeMerger merger) {
        Validate.notNull(merger, "The IAttributeMerger cannot be null");
        this.attrMerger = merger;
    }

    /**
     * Get an unmodifiable {@link List} of delegates which we will poll for attributes.
     *
     * @return Returns the personAttributeDaos.
     */
    public final List<IPersonAttributeDao> getPersonAttributeDaos() {
        return this.personAttributeDaos;
    }

    /**
     * Set the {@link List} of delegates which we will poll for attributes.
     *
     * @param daos The personAttributeDaos to set.
     * @throws IllegalArgumentException If daos is <code>null</code>.
     */
    @Required
    public final void setPersonAttributeDaos(final List<IPersonAttributeDao> daos) {
        Validate.notNull(daos, "The IPersonAttributeDao List cannot be null");
        this.personAttributeDaos = CollectionsUtil.safelyWrapAsUnmodifiableList(daos);
    }

    /**
     * True if this class will catch exceptions thrown by its delegate DAOs
     * and fail to propogate them.  False if this class will stop on failure.
     *
     * @return true if will recover exceptions, false otherwise
     */
    public final boolean isRecoverExceptions() {
        return this.recoverExceptions;
    }

    /**
     * Set to true if you would like this class to swallow RuntimeExceptions
     * thrown by its delegates.  This allows it to recover if a particular attribute
     * source fails, still considering previous and subsequent sources.
     * Set to false if you would like this class to fail hard upon any Throwable
     * thrown by its children.  This is desirable in cases where your Portal will not
     * function without attributes from all of its sources.
     *
     * @param recover whether you would like exceptions recovered internally
     */
    public final void setRecoverExceptions(final boolean recover) {
        this.recoverExceptions = recover;
    }

    /**
     * Is stop on success boolean.
     *
     * @return the boolean
     */
    public boolean isStopOnSuccess() {
        return stopOnSuccess;
    }

    /**
     * If true iteration of the child DAOs will stop after the first one that returns without
     * throwing an exception. This is intended to provide fail-over among attribute sources. The
     * <b>recoverExceptions</b> should be set to true for this to function as expected
     *
     * @param stopOnSuccess If the first valid results should be returned, defaults to false
     */
    public void setStopOnSuccess(final boolean stopOnSuccess) {
        this.stopOnSuccess = stopOnSuccess;
    }


}
