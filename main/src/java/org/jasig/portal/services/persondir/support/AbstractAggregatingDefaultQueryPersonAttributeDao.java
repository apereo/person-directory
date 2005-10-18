/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.IAttributeMerger;
import org.jasig.portal.services.persondir.support.merger.MultivaluedAttributeMerger;


/**
 * Provides a base set of implementations and properties for IPersonAttributeDao
 * implementations that aggregate results from a sub List of IPersonAttributeDaos.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDao extends AbstractDefaultQueryPersonAttributeDao {
    /**
     * A List of child IPersonAttributeDao instances which we will poll in order.
     */
    protected List personAttributeDaos;
    
    /**
     * Strategy for merging together the results from successive PersonAttributeDaos.
     */
    protected IAttributeMerger attrMerger = new MultivaluedAttributeMerger();
    
    /**
     * True if we should catch, log, and ignore Throwables propogated by
     * individual DAOs.
     */
    protected boolean recoverExceptions = true;
    
    /**
     * This implementation is not always correct.
     * It handles the basic case where the Set of attributes returned by this
     * implementation is the union of the attributes declared by all of the
     * underlying implementations to be merged.  Of course, an IAttributeMerger
     * might provide for a merging policy such that the attributes resulting from
     * invoking this IPersonAttributeDao implementation are not the union
     * of the attributes declared by the underlying PersonAttributeDaos.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        final Set attrNames = new HashSet();
        
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final IPersonAttributeDao currentDao = (IPersonAttributeDao)iter.next();
            
            Set currentDaoAttrNames = null;
            try {
                currentDaoAttrNames = currentDao.getPossibleUserAttributeNames();
            }
            catch (final RuntimeException rte) {
                final String msg = "Exception thrown by DAO: " + currentDao;

                if (this.recoverExceptions) {
                    log.warn(msg + ", ignoring this DAO and continuing.", rte);
                }
                else {
                    log.error(msg + ", failing.", rte);
                    throw rte;
                }
            }
            
            if (currentDaoAttrNames != null)
                attrNames.addAll(currentDaoAttrNames);
        }
        
        return Collections.unmodifiableSet(attrNames);
    }
    
    /**
     * Get the strategy whereby we accumulate attributes.
     * 
     * @return Returns the attrMerger.
     */
    public IAttributeMerger getMerger() {
        return this.attrMerger;
    }
    
    /**
     * Set the strategy whereby we accumulate attributes from the results of 
     * polling our delegates.
     * 
     * @param merger The attrMerger to set.
     * @throws IllegalArgumentException If merger is <code>null</code>.
     */
    public void setMerger(final IAttributeMerger merger) {
        if (merger == null)
            throw new IllegalArgumentException("The merger cannot be null");
            
        this.attrMerger = merger;
    }
    
    
    /**
     * Get an unmodifiable {@link List} of delegates which we will poll for attributes.
     * 
     * @return Returns the personAttributeDaos.
     */
    public List getPersonAttributeDaos() {
        return this.personAttributeDaos;
    }
    
    /**
     * Set the {@link List} of delegates which we will poll for attributes.
     * 
     * @param daos The personAttributeDaos to set.
     * @throws IllegalArgumentException If daos is <code>null</code>.
     */
    public void setPersonAttributeDaos(final List daos) {
        if (daos == null)
            throw new IllegalArgumentException("The dao list cannot be null");
            
        this.personAttributeDaos = Collections.unmodifiableList(daos);
    }
    
    /**
     * True if this class will catch exceptions thrown by its delegate DAOs
     * and fail to propogate them.  False if this class will stop on failure.
     * 
     * @return true if will recover exceptions, false otherwise
     */
    public boolean isRecoverExceptions() {
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
    public void setRecoverExceptions(boolean recover) {
        this.recoverExceptions = recover;
    }
}
