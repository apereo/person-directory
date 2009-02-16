/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;

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
public class StubPersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
    private IPersonAttributes backingPerson = null;

    public StubPersonAttributeDao() {
    }

    public StubPersonAttributeDao(Map<String, List<Object>> backingMap) {
        this.setBackingMap(backingMap);
    }

    public Set<String> getPossibleUserAttributeNames() {
        if (this.backingPerson == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(this.backingPerson.getAttributes().keySet());
    }
    
    public Set<String> getAvailableQueryAttributes() {
        return null;
    }

    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        if (query == null) {
            throw new IllegalArgumentException("Illegal to invoke getPeople(Map) with a null argument.");
        }
        
        if (this.backingPerson == null) {
            return null;
        }

        return Collections.singleton(this.backingPerson);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPerson(java.lang.String)
     */
    public IPersonAttributes getPerson(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getPerson(String) with a null argument.");
        }
        return this.backingPerson;
    }

    /**
     * Get the Map which this stub object will return for all legal invocations of
     * attributesForUser()
     * 
     * @return Returns the backingMap.
     */
    public Map<String, List<Object>> getBackingMap() {
        return this.backingPerson.getAttributes();
    }

    /**
     * Set the Map which this stub object will return for all legal invocations of
     * attributesForUser().
     * 
     * @param backingMap The backingMap to set, may not be null.
     */
    public void setBackingMap(final Map<String, List<Object>> backingMap) {
        this.backingPerson = new AttributeNamedPersonImpl(backingMap);
    }
}
