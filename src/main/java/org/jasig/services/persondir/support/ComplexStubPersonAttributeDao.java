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


/**
 * Looks up the user's attribute Map in the backingMap. If using the {@link IPersonAttributeDao#getUserAttributes(Map)}
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
 *             by {@link IPersonAttributeDao#getUserAttributes(Map)}.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link Collections#EMPTY_MAP}</td>
 *     </tr>
 * </table>
 * 
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
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
            this.backingMap = Collections.unmodifiableMap(new HashMap<String, Map<String, List<Object>>>(backingMap));
            this.initializePossibleAttributeNames();
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.possibleUserAttributeNames;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map<String, List<Object>> getUserAttributes(Map<String, List<Object>> seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(Map) with a null argument.");
        }

        final String defaultAttrName = this.getDefaultAttributeName();
        final List<Object> seedValues = seed.get(defaultAttrName);
        if (seedValues == null) {
            return null;
        }

        final String seedValue =  String.valueOf(seedValues.get(0));
        return this.backingMap.get(seedValue);
    }

    /**
     * Compute the set of attribute names that map to a value for at least one
     * user in our backing map and store it as the instance variable 
     * possibleUserAttributeNames.
     */
    private void initializePossibleAttributeNames() {
        final Set<String> possibleAttribNames = new HashSet<String>();
        
        for (final Map<String, List<Object>> attributeMapForSomeUser : this.backingMap.values()) {
            final Set<String> keySet = attributeMapForSomeUser.keySet();
            possibleAttribNames.addAll(keySet);
        }
        
        this.possibleUserAttributeNames = Collections.unmodifiableSet(possibleAttribNames);
    }
}

