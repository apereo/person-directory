/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * A stub IPersonAttributeDao to be used for testing.
 * Backed by a single Map which this implementation will always return.
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
 *             This Map will always be returned for any query.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class StubPersonAttributeDao implements IPersonAttributeDao {
    private Map<String, List<Object>> backingMap = null;

    public StubPersonAttributeDao() {
    }

    public StubPersonAttributeDao(Map<String, List<Object>> backingMap) {
        this.setBackingMap(backingMap);
    }

    public Set<String> getPossibleUserAttributeNames() {
        if (this.backingMap == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(this.backingMap.keySet());
    }

    public Map<String, List<Object>> getUserAttributes(final Map<String, List<Object>> seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(Map) with a null argument.");
        }
        return this.backingMap;
    }

    public Map<String, List<Object>> getUserAttributes(final String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(String) with a null argument.");
        }
        return this.backingMap;
    }

    /**
     * Get the Map which this stub object will return for all legal invocations of
     * attributesForUser()
     * 
     * @return Returns the backingMap.
     */
    public Map<String, List<Object>> getBackingMap() {
        return this.backingMap;
    }

    /**
     * Set the Map which this stub object will return for all legal invocations of
     * attributesForUser().
     * 
     * @param backingMap The backingMap to set, may not be null.
     */
    public void setBackingMap(final Map<String, List<Object>> backingMap) {
        this.backingMap = backingMap;
    }
}
