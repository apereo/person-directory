/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.services.persondir.support.merger.ReplacingAttributeAdder;


/**
 * This {@link org.jasig.portal.services.persondir.IPersonAttributeDao}
 * implementation iterates through an ordered {@link java.util.List} of
 * {@link org.jasig.portal.services.persondir.IPersonAttributeDao} impls
 * when getting user attributes.
 * <br>
 * The first DAO is queried using the seed {@link Map} passed to this class. The results
 * of the query are merged into a general result map. After the first DAO this general
 * result map used as the query seed for each DAO and each DAOs results are merged into it.
 * <br>
 * This behavior allows a DAO lower on the list to rely on attributes returned by a DAO
 * higher on the list.
 * <br>
 * The default merger for the general result set is {@link ReplacingAttributeAdder}.
 * <br>
 * Note that most DAOs expect a Map of String->String. Some of the DAOs return a Map of
 * String->Object or String->List. This may cause problems in the DAO if the key for an
 * attribute with a non String value matches a key needed by the DAO for the query it is
 * running.
 * <br>
 * It is <u>highly</u> recomended that the first DAO on the list for this class is
 * the {@link org.jasig.portal.services.persondir.support.EchoPersonAttributeDaoImpl}
 * to ensure the seed gets placed into the general result map.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CascadingPersonAttributeDao extends AbstractAggregatingDefaultQueryPersonAttributeDao {
    public CascadingPersonAttributeDao() {
        this.attrMerger = new ReplacingAttributeAdder();
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
        
        //Denotes that this is the first time we are running a query and the seed should be used
        boolean isFirstQuery = true;
        
        //Iterate through the configured IPersonAttributeDaos, querying each.
        for (final Iterator iter = this.personAttributeDaos.iterator(); iter.hasNext();) {
            final IPersonAttributeDao currentlyConsidering = (IPersonAttributeDao)iter.next();
            
            Map currentAttributes = new HashMap();
            try {
                if (isFirstQuery) {
                    currentAttributes = currentlyConsidering.getUserAttributes(seed);
                    isFirstQuery = false; //Ran a query successfully, stop using the seed
                }
                else
                    currentAttributes = currentlyConsidering.getUserAttributes(resultAttributes);
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