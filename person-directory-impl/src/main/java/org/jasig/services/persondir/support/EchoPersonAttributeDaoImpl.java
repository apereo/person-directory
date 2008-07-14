/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPerson;


/**
 * Simply returns the seed it is passed.
 * 
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class EchoPersonAttributeDaoImpl extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * Returns a duplicate of the seed it is passed.
     * @return a Map equal to but not the same reference as the seed.
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public Set<IPerson> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        if (query == null) {
            throw new IllegalArgumentException("seed may not be null");
        }

        return Collections.singleton((IPerson)new AttributeNamedPersonImpl(query));
    }

    /**
     * Possible attributes are unknown; will always return <code>null</code>.
     * @return null
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    public Set<String> getAvailableQueryAttributes() {
        return null;
    }
}
