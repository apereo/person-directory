/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Provides a base set of implementations and properties for IPersonAttributeDao
 * implementations that aggregate results from a sub List of IPersonAttributeDaos.
 * 
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
 *         <td align="right" valign="top">personAttributeDaos</td>
 *         <td>
 *             A {@link List} of {@link IPersonAttributeDao}s to aggregate attributes from.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">attrMerger</td>
 *         <td>
 *             A {@link IAttributeMerger} strategy to use for merging the attributes from
 *             the {@link List} of {@link IPersonAttributeDao}s.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link MultivaluedAttributeMerger}</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">recoverExceptions</td>
 *         <td>
 *             Sets the action to be taken if one of the {@link IPersonAttributeDao}s in the
 *             {@link List} fails with a {@link RuntimeException}. If set to true a warn level
 *             log message and stack trace will be logged. If set to false an error level
 *             message and stack trace will be logged and the exception will re-thrown. 
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">true</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist
 * @version $Revision$
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
     * Iterates through the configured {@link java.util.List} of {@link IPersonAttributeDao}
     * instances. The results from each DAO are merged into the result {@link Map}
     * by the configured {@link IAttributeMerger}. 
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(Map)
     */
    public final Map<String, List<Object>> getMultivaluedUserAttributes(Map<String, List<Object>> seed) {
        Validate.notNull(seed, "seed may not be null.");
        
        if (this.personAttributeDaos == null) {
            throw new IllegalStateException("personAttributeDaos property must be set");
        }

        //Initialize null, so that if none of the sub-DAOs find the user null is returned appropriately
        Map<String, List<Object>> resultAttributes = null;
        
        //Denotes that this is the first time we are running a query and the original seed should be used
        boolean isFirstQuery = true;
        
        //Iterate through the configured IPersonAttributeDaos, querying each.
        for (final IPersonAttributeDao currentlyConsidering : this.personAttributeDaos) {
            Map<String, List<Object>> currentAttributes = new HashMap<String, List<Object>>();
            try {
                currentAttributes = this.getAttributesFromDao(seed, isFirstQuery, currentlyConsidering, resultAttributes);
                isFirstQuery = false;

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Retrieved attributes='" + currentAttributes + "' for seed='" + seed + "', isFirstQuery=" + isFirstQuery + ", currentlyConsidering='" + currentlyConsidering + "', resultAttributes='" + resultAttributes + "'");
                }
            }
            catch (final RuntimeException rte) {
                if (this.recoverExceptions) {
                    this.logger.warn("Recovering From Exception thrown by '" + currentlyConsidering + "'", rte);
                }
                else {
                    this.logger.error("Failing From Exception thrown by '" + currentlyConsidering + "'", rte);
                    throw rte;
                }
            }

            if (resultAttributes == null) {
                //If this is the first valid result set just use it.
                resultAttributes = currentAttributes;
            }
            else if (currentAttributes != null) {
                //Perform the appropriate attribute attrMerger
                resultAttributes = this.attrMerger.mergeAttributes(resultAttributes, currentAttributes);
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Aggregated attributes '" + resultAttributes + "' for seed='" + seed + "'");
        }
        
        return resultAttributes;
    }
    
    /**
     * Call to execute the appropriate query on the current {@link IPersonAttributeDao}. Provides extra information
     * beyond the seed for the state of the query chain and previous results.
     * 
     * @param seed The seed for the original query.
     * @param isFirstQuery If this is the first query, this will stay true until a call to this method returns (does not throw an exception).
     * @param currentlyConsidering The IPersonAttributeDao to execute the query on.
     * @param resultAttributes The Map of results from all previous queries, may be null.
     * @return The results from the call to the DAO, follows the same rules as {@link IPersonAttributeDao#getUserAttributes(Map)}.
     */
    protected abstract Map<String, List<Object>> getAttributesFromDao(final Map<String, List<Object>> seed, final boolean isFirstQuery, final IPersonAttributeDao currentlyConsidering, final Map<String, List<Object>> resultAttributes);
    
    
    /**
     * This implementation is not always correct.
     * It handles the basic case where the Set of attributes returned by this
     * implementation is the union of the attributes declared by all of the
     * underlying implementations to be merged.  Of course, an IAttributeMerger
     * might provide for a merging policy such that the attributes resulting from
     * invoking this IPersonAttributeDao implementation are not the union
     * of the attributes declared by the underlying PersonAttributeDaos.
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public final Set<String> getPossibleUserAttributeNames() {
        final Set<String> attrNames = new HashSet<String>();
        
        for (final IPersonAttributeDao currentDao : this.personAttributeDaos) {
            Set<String> currentDaoAttrNames = null;
            try {
                currentDaoAttrNames = currentDao.getPossibleUserAttributeNames();
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Retrieved possible attribute names '" + currentDaoAttrNames + "' from '" + currentDao + "'");
                }
            }
            catch (final RuntimeException rte) {
                if (this.recoverExceptions) {
                    this.logger.warn("Recovering From Exception thrown by '" + currentDao + "'", rte);
                }
                else {
                    this.logger.error("Failing From Exception thrown by '" + currentDao + "'", rte);
                    throw rte;
                }
            }
            
            if (currentDaoAttrNames != null) {
                attrNames.addAll(currentDaoAttrNames);
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Aggregated possible attribute names '" + attrNames + "'");
        }
        
        return Collections.unmodifiableSet(attrNames);
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
        this.personAttributeDaos = Collections.unmodifiableList(daos);
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
    public final void setRecoverExceptions(boolean recover) {
        this.recoverExceptions = recover;
    }
}
