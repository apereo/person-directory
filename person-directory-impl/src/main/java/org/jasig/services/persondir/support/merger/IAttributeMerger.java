/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.merger;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * Interface for merging attributes from sibling PersonAttributeDaos. 
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface IAttributeMerger {
    /**
     * Merge the results of a {@link org.jasig.services.persondir.search.IPersonLookupDao#getUserIds(java.util.Map)} call
     * and a compiled results map.
     * 
     * @param toModify The compiled results map, this will be modified based on the values in toConsider.
     * @param toConsider The query results map, this will not be modified.
     */
    public Set<IPersonAttributes> mergeResults(Set<IPersonAttributes> toModify, Set<IPersonAttributes> toConsider);

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" argument. Return the resulting Set which may
     * or may not be the same reference as the "toModify" argument.
     * 
     * The modification performed is implementation-specific -- implementations of this interface exist to perform some
     * particular transformation on the toModify argument given the toConsider argument.
     * 
     * @param toModify Modify this set
     * @param toConsider In consideration of this set
     * @return The modified set
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    public Set<String> mergePossibleUserAttributeNames(Set<String> toModify, Set<String> toConsider);
    
    /**
     * Modify the "toModify" argument in consideration of the "toConsider" argument. Return the resulting Set which may
     * or may not be the same reference as the "toModify" argument.
     * 
     * The modification performed is implementation-specific -- implementations of this interface exist to perform some
     * particular transformation on the toModify argument given the toConsider argument.
     * 
     * @param toModify Modify this set
     * @param toConsider In consideration of this set
     * @return The modified set
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    public Set<String> mergeAvailableQueryAttributes(Set<String> toModify, Set<String> toConsider);
    

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" 
     * argument.  Return the resulting Map, which may or may not be the same
     * reference as the "toModify" argument.
     * The modification performed is implementation-specific -- implementations
     * of this interface exist to perform some particular transformation on
     * the toModify argument given the toConsider argument.
     * 
     * @param toModify - modify this map
     * @param toConsider - in consideration of this map
     * @return the modified Map
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    public Map<String, List<Object>> mergeAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider);
}