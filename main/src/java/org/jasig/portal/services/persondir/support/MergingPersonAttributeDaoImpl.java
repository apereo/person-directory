/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.MultivaluedAttributeMerger;

/**
 * A {@link IPersonAttributeDao} implementation which iterates over children 
 * IPersonAttributeDaos queries each with the same data and merges their
 * reported attributes in a configurable way. The default merger is
 * {@link MultivaluedAttributeMerger}.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class MergingPersonAttributeDaoImpl extends AbstractAggregatingDefaultQueryPersonAttributeDao {
    public MergingPersonAttributeDaoImpl() {
        this.attrMerger = new MultivaluedAttributeMerger();
    }
    
    /**
     * Iterates through the configured {@link java.util.List} of {@link IPersonAttributeDao}
     * instances. The results from each DAO are merged into the result {@link Map}
     * by the configured {@link org.jasig.portal.services.persondir.support.merger.IAttributeMerger}. 
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        //Ensure the arguements and state are valid
        if (seed == null)
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        
        if (this.personAttributeDaos == null)
            throw new IllegalStateException("No IPersonAttributeDaos have been specified.");
        
        //Initialize null, so that if none of the sub-DAOs find the user null is returned appropriately
        Map resultAttributes = null;
        
        //Iterate through the configured IPersonAttributeDaos, querying each.
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final IPersonAttributeDao currentlyConsidering = (IPersonAttributeDao)iter.next();
            
            Map currentAttributes = new HashMap();
            try {
                currentAttributes = currentlyConsidering.getUserAttributes(seed);
            }
            catch (final RuntimeException rte) {
                final String msg = "Exception thrown by DAO: " + currentlyConsidering;

                if (this.recoverExceptions) {
                    log.warn("Recovering From " + msg, rte);
                }
                else {
                    log.error(msg, rte);
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
        
        return resultAttributes;
    }
}