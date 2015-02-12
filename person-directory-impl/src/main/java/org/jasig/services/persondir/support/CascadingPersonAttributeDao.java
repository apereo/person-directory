/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.support;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;


/**
 * This {@link org.jasig.services.persondir.IPersonAttributeDao}
 * implementation iterates through an ordered {@link java.util.List} of
 * {@link org.jasig.services.persondir.IPersonAttributeDao} impls
 * when getting user attributes.
 * <br/>
 * The first DAO is queried using the seed {@link Map} passed to this class. The results
 * of the query are merged into a general result map. After the first DAO this general
 * result map used as the query seed for each DAO and each DAO's results are merged into it.
 * <ul>
 * <li>If the first DAO returned null/no results and <code>stopIfFirstDaoReturnsNull</code>=true, no subsequent DAO is
 * called and null is the final result.</li>
 * <li>If the first DAO returned null/no results and <code>stopIfFirstDaoReturnsNull</code>=false, each subsequent DAO is
 * called and the first that returns a result is used as the seed to the remaining child DAOs.  This is the default
 * to support legacy behavior.</li>
 * </ul>
 * This behavior allows a DAO lower on the list to rely on attributes returned by a DAO
 * higher on the list.
 * <br/>
 * The default merger for the general result set is {@link ReplacingAttributeAdder}.
 * <br/>
 * Example Use case: Query LDAP for values (LDAP is first DAO), and then feed results into a secondary source
 * such as an SIS system that has a different key than username and LDAP provides the key for the SIS query.
 * <br/>
 * Note that most DAOs expect a Map of String->String. Some of the DAOs return a Map of
 * String->Object or String->List. This may cause problems in the DAO if the key for an
 * attribute with a non String value matches a key needed by the DAO for the query it is
 * running.
 * <br/>
 * It is <u>highly</u> recommended that the first DAO on the list for this class is
 * the {@link org.jasig.services.persondir.support.EchoPersonAttributeDaoImpl}
 * to ensure the seed gets placed into the general result map.
 * 
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CascadingPersonAttributeDao extends AbstractAggregatingDefaultQueryPersonAttributeDao {

    /**
     * Set to true to not invoke child DAOs if first DAO returns null or no results.  Default: false
     * to support legacy behavior.
     * @since 1.6.0
     */
    private boolean stopIfFirstDaoReturnsNull = false;

    public void setStopIfFirstDaoReturnsNull(boolean stopIfFirstDaoReturnsNull) {
        this.stopIfFirstDaoReturnsNull = stopIfFirstDaoReturnsNull;
    }

    public CascadingPersonAttributeDao() {
        this.attrMerger = new ReplacingAttributeAdder();
    }
    
    

    /**
     * If this is the first call, or there are no results in the resultPeople Set and stopIfFirstDaoReturnsNull=false,
     * the seed map is used. If not the attributes of the first user in the resultPeople Set are used for each child
     * dao.  If stopIfFirstDaoReturnsNull=true and the first query returned no results in the resultPeopleSet,
     * return null.
     *  
     * @see org.jasig.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDao#getAttributesFromDao(java.util.Map, boolean, org.jasig.services.persondir.IPersonAttributeDao, java.util.Set)
     */
    @Override
    protected Set<IPersonAttributes> getAttributesFromDao(Map<String, List<Object>> seed, boolean isFirstQuery, IPersonAttributeDao currentlyConsidering, Set<IPersonAttributes> resultPeople) {
        if (isFirstQuery || (!stopIfFirstDaoReturnsNull && (resultPeople == null || resultPeople.size() == 0))) {
            return currentlyConsidering.getPeopleWithMultivaluedAttributes(seed);
        } else if (stopIfFirstDaoReturnsNull && !isFirstQuery && (resultPeople == null || resultPeople.size() == 0)) {
            return null;
        }
        
        Set<IPersonAttributes> mergedPeopleResults = null;
        for (final IPersonAttributes person : resultPeople) {
            final Map<String, List<Object>> queryAttributes = new LinkedHashMap<String, List<Object>>();
            
            //Add the userName into the query map
            final String userName = person.getName();
            if (userName != null) {
                final Map<String, List<Object>> userNameMap = this.toSeedMap(userName);
                queryAttributes.putAll(userNameMap);
            }
            
            //Add the rest of the attributes into the query map
            final Map<String, List<Object>> personAttributes = person.getAttributes();
            queryAttributes.putAll(personAttributes);
            
            final Set<IPersonAttributes> newResults = currentlyConsidering.getPeopleWithMultivaluedAttributes(queryAttributes);
            if (newResults != null) {
                if (mergedPeopleResults == null) {
                    //If this is the first valid result set just use it.
                    mergedPeopleResults = new LinkedHashSet<IPersonAttributes>(newResults);
                }
                else {
                    //Merge the Sets of IPersons
                    mergedPeopleResults = this.attrMerger.mergeResults(mergedPeopleResults, newResults);
                }
            }
        }
        
        return mergedPeopleResults;
    }
}