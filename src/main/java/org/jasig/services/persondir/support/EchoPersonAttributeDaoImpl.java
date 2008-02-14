/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map<String, List<Object>> getUserAttributes(final Map<String, List<Object>> seed) {
        if (seed == null) {
            throw new IllegalArgumentException("seed may not be null");
        }

        return new HashMap<String, List<Object>>(seed);
    }

    /**
     * Possible attributes are unknown; will always return <code>null</code>.
     * @return null
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }

}
