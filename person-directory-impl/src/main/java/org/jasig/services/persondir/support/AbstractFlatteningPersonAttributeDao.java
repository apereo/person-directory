/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPerson;


/**
 * Maps calls to {@link org.jasig.services.persondir.IPersonAttributeDao#getPeople(Map)} to
 * {@link org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(Map)}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractFlatteningPersonAttributeDao extends BasePersonAttributeDao {

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeople(java.util.Map)
     */
    public final Set<IPerson> getPeople(Map<String, Object> query) {
        final Map<String, List<Object>> multivaluedSeed = MultivaluedPersonAttributeUtils.toMultivaluedMap(query);
        return this.getPeopleWithMultivaluedAttributes(multivaluedSeed);
    }
    
    /**
     * @deprecated Use {@link MultivaluedPersonAttributeUtils#toMultivaluedMap(Map)} instead. This will be removed in 1.6
     */
    @Deprecated
    protected Map<String, List<Object>> toMultivaluedSeed(Map<String, Object> seed) {
        return MultivaluedPersonAttributeUtils.toMultivaluedMap(seed);
    }
}
