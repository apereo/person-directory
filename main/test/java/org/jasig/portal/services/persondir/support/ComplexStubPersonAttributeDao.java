/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implements IPersonAttributeDao by looking up the specified user in a configured
 * Map.  The configured Map is a Map from String userids to Maps.  The Map
 * values are Maps from String user attribute names to user attribute values.
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDao extends AbstractDefaultQueryPersonAttributeDao {
    /**
     * Map from userids to Maps.  The Map values are maps from
     * attribute names to attribute values.
     */
    private Map backingMap;
    
    /**
     * Set of possible all attribute names that map to an attribute
     * value for some user in our backing map.
     */
    private Set possibleUserAttributeNames = null;
    
    public ComplexStubPersonAttributeDao() {
        this.setDefaultAttributeName("uid");
    }
    
    public ComplexStubPersonAttributeDao(Map backingMap) {
        this();
        this.setBackingMap(backingMap);
    }
    
    
    /**
     * @return Returns the backingMap.
     */
    public Map getBackingMap() {
        return this.backingMap;
    }
    /**
     * @param backingMap The backingMap to set.
     */
    public synchronized void setBackingMap(Map backingMap) {
        if (backingMap == null) {
            this.backingMap = null;
            this.possibleUserAttributeNames = Collections.EMPTY_SET;
        }
        else {
            this.backingMap = Collections.unmodifiableMap(backingMap);
            this.initializePossibleAttributeNames();
        }
    }
    
    public Set getPossibleUserAttributeNames() {
        return this.possibleUserAttributeNames;
    }
    
    public Map getUserAttributes(final Map seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(Map) with a null argument.");
        }

        return (Map)this.backingMap.get(seed.get(this.getDefaultAttributeName()));
    }


    /**
     * Compute the set of attribute names that map to a value for at least one
     * user in our backing map and store it as the instance variable 
     * possibleUserAttributeNames.
     */
    private void initializePossibleAttributeNames() {
        final Set possibleAttribNames = new HashSet();
        
        for (Iterator iter = this.backingMap.values().iterator(); iter.hasNext() ; ) {
            final Map attributeMapForSomeUser = (Map)iter.next();
            possibleAttribNames.addAll(attributeMapForSomeUser.keySet());
        }
        
        this.possibleUserAttributeNames = Collections.unmodifiableSet(possibleAttribNames);
    }
}
