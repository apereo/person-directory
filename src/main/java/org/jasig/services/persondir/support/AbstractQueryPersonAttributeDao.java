/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides common functionality for DAOs using a set of attribute values from the seed to
 * perform a query. Ensures the nessesary attributes to run the query exist on the seed and
 * organizes the values into an argument array.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public abstract class AbstractQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * List of names of uPortal attributes the values of which will be used, in
     * order, to populate the parameters of the LDAP query.
     */
    private List queryAttributes = null;

    
    /**
     * Checks the seed for being null, throws IllegalArgumentException if it is.<br>
     * Ensures the seed contains the attributes needed to run the query, returns null if they aren't available.<br>
     * Compiles the Object[] of arguments from the seed based on the queryAttributes.<br>
     *
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public final Map getUserAttributes(final Map seed) {
        // Checks to make sure the argument & state is valid
        if (seed == null)
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        
        final Object[] args;
        
        //The queryAttributes are configured and the seed contains all of the needed attributes
        if (this.queryAttributes != null && seed.keySet().containsAll(this.queryAttributes)) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Constructing argument name array from the queryAttributes List");
            }

            // Can't just to a toArray here since the order of the keys in the Map
            // may not match the order of the keys in the List and it is important to
            // the query.
            args = new Object[this.queryAttributes.size()];
            for (int index = 0; index < args.length; index++) {
                final String attrName = (String) this.queryAttributes.get(index);
                args[index] = seed.get(attrName);
            }
        }
        //No queryAttributes are configured but the seed contains the default attribute
        else if (this.queryAttributes == null && seed.containsKey(this.getDefaultAttributeName())) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Constructing argument name array from the defaultAttributeName");
            }

            final String attrName = this.getDefaultAttributeName();
            args = new Object[] { seed.get(attrName) };
        }
        //The data needed to run the query isn't in the seed, null is returned
        else {
            if (this.log.isDebugEnabled()) {
                this.log.debug("The seed does not contain the required information to run the query, returning null.");
            }
            
            return null;
        }

        return this.getUserAttributesIfNeeded(args);
    }
    
    /**
     * Is called by {@link #getUserAttributes(Map)} if the attributes required for the query, as defined
     * by the values of the queryAttributes property, are available in the seed. The implementation of
     * {@link #getUserAttributes(Map)} also compiles the array of query argument values based on the order
     * of items in the queryAttributes property and the values in the seed.
     * 
     * @param args The arguments to execute the query with.
     * @return The results of the query, as specified by {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}
     */
    protected abstract Map getUserAttributesIfNeeded(final Object[] args);

    /**
     * @return Returns the queryAttributes.
     */
    public List getQueryAttributes() {
        return this.queryAttributes;
    }

    /**
     * @param queryAttributes The queryAttributes to set.
     */
    public void setQueryAttributes(List queryAttributes) {
        this.queryAttributes = Collections.unmodifiableList(new LinkedList(queryAttributes));
    }
}
