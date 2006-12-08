/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Map;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;


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
     * If this is the first call, isFirstQuery == true, the query run against the current IPersonAttributeDao is done using
     * the seed Map. Otherwise the query is run using the resultAttributes Map. 
     * 
     * @see org.jasig.portal.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDao#getAttributesFromDao(java.util.Map, boolean, org.jasig.portal.services.persondir.IPersonAttributeDao, java.util.Map)
     */
    protected Map getAttributesFromDao(final Map seed, final boolean isFirstQuery, final IPersonAttributeDao currentlyConsidering, final Map resultAttributes) {
        if (isFirstQuery) {
            return currentlyConsidering.getUserAttributes(seed);
        }
        else {
            return currentlyConsidering.getUserAttributes(resultAttributes);
        }
    }
}