/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPerson;


/**
 * Looks up the user's attribute Map in the backingMap. If using the {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}
 * method the attribute value returned for the key {@link #getDefaultAttributeName()} will
 * be used as the key for the backingMap.
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
 *         <td align="right" valign="top">backingMap</td>
 *         <td>
 *             Sets the backing map to use to return user attributes from. The backing map
 *             should have keys of type {@link String} which are the uid for the user. The
 *             values should be of type {@link Map} which follow the Map restrictions decribed
 *             by {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link Collections#EMPTY_MAP}</td>
 *     </tr>
 * </table>
 * 
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDao extends AbstractQueryPersonAttributeDao<String> {
    /*
     * Map from userids to Maps.  The Map values are maps from
     * attribute names to attribute values.
     */
    private Map<String, Map<String, List<Object>>> backingMap = Collections.emptyMap();
    
    /*
     * Set of possible all attribute names that map to an attribute
     * value for some user in our backing map.
     */
    private Set<String> possibleUserAttributeNames = Collections.emptySet();
    
    /**
     * Creates a new, empty, dao.
     */
    public ComplexStubPersonAttributeDao() {
    }
    
    /**
     * Creates a new DAO with the specified backing map.
     * @param backingMap The backingMap to call {@link #setBackingMap(Map)} with.
     */
    public ComplexStubPersonAttributeDao(Map<String, Map<String, List<Object>>> backingMap) {
        this.setBackingMap(backingMap);
    }
    
    
    /**
     * @return Returns the backingMap.
     */
    public Map<String, Map<String, List<Object>>> getBackingMap() {
        return this.backingMap;
    }
    /**
     * @param backingMap The backingMap to set.
     */
    public void setBackingMap(Map<String, Map<String, List<Object>>> backingMap) {
        if (backingMap == null) {
            this.backingMap = Collections.emptyMap();
            this.possibleUserAttributeNames = Collections.emptySet();
        }
        else {
            this.backingMap = Collections.unmodifiableMap(new LinkedHashMap<String, Map<String, List<Object>>>(backingMap));
            this.initializePossibleAttributeNames();
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getPossibleUserAttributeNames()
     */
    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return this.possibleUserAttributeNames;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getAvailableQueryAttributes()
     */
    @Override
    public Set<String> getAvailableQueryAttributes() {
        return Collections.singleton(this.getDefaultAttributeName());
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#appendAttributeToQuery(java.lang.Object, java.lang.String, java.util.List)
     */
    @Override
    protected String appendAttributeToQuery(String queryBuilder, String dataAttribute, List<Object> queryValues) {
        if (queryBuilder != null) {
            return queryBuilder;
        }
        
        final String defaultAttrName = this.getDefaultAttributeName();
        if (defaultAttrName.equals(dataAttribute)) {
            return String.valueOf(queryValues.get(0));
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getPeopleForQuery(java.lang.Object)
     */
    @Override
    protected List<IPerson> getPeopleForQuery(String seedValue) {
        final Map<String, List<Object>> attributes = this.backingMap.get(seedValue);
        
        if (attributes == null) {
            return null;
        }
        
        final String defaultAttrName = this.getDefaultAttributeName();
        final String userNameAttribute = this.getConfiguredUserNameAttribute();
        
        final IPerson person;
        if (defaultAttrName.equals(userNameAttribute)) {
            person = new NamedPersonImpl(seedValue, attributes);
        }
        else {
            person = new AttributeNamedPersonImpl(userNameAttribute, attributes);
        }
        
        return Collections.singletonList(person);
    }

    /**
     * Compute the set of attribute names that map to a value for at least one
     * user in our backing map and store it as the instance variable 
     * possibleUserAttributeNames.
     */
    private void initializePossibleAttributeNames() {
        final Set<String> possibleAttribNames = new LinkedHashSet<String>();
        
        for (final Map<String, List<Object>> attributeMapForSomeUser : this.backingMap.values()) {
            final Set<String> keySet = attributeMapForSomeUser.keySet();
            possibleAttribNames.addAll(keySet);
        }
        
        this.possibleUserAttributeNames = Collections.unmodifiableSet(possibleAttribNames);
    }
}

