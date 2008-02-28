/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides common functionality for DAOs using a set of attribute values from the seed to
 * perform a query. Ensures the nessesary attributes to run the query exist on the seed and
 * organizes the values into an argument array.
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
 *         <td align="right" valign="top">queryAttributes</td>
 *         <td>
 *             A {@link List} of {@link String} attribute names whos values should be used
 *             when executing the
 *             query via {@link #getUserAttributesIfNeeded(Object[])}. If this {@link List} is set all of the names it contains must be in
 *             the keySet of the seed passed to {@link #getUserAttributes(Map)} or null will
 *             be returned. If the {@link List} is left null the {@link #getDefaultAttributeName()}
 *             will be used as the single argument when calling {@link #getUserAttributesIfNeeded(Object[])}
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * @author Eric Dalquist 
 * @version $Revision$
 */
public abstract class AbstractQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * List of names of uPortal attributes the values of which will be used, in
     * order, to populate the parameters of the query.
     */
    private List<String> queryAttributes = null;

    
    /**
     * Checks the seed for being null, throws IllegalArgumentException if it is.<br>
     * Ensures the seed contains the attributes needed to run the query, returns null if they aren't available.<br>
     * Compiles the Object[] of arguments from the seed based on the queryAttributes.<br>
     *
     * @see org.jasig.services.persondir.IPersonAttributeDao#getMultivaluedUserAttributes(java.util.Map)
     */
    public final Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed) {
        Validate.notNull(seed, "seed may not be null.");
        
        final List<List<Object>> args;
        
        //The queryAttributes are configured and the seed contains all of the needed attributes
        if (this.queryAttributes != null && seed.keySet().containsAll(this.queryAttributes)) {
            // Can't just to a toArray here since the order of the keys in the Map
            // may not match the order of the keys in the List and it is important to
            // the query.
            args = new ArrayList<List<Object>>(this.queryAttributes.size());
            for (final String attrName : this.queryAttributes) {
                final List<Object> value = seed.get(attrName);
                args.add(value);
            }
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Constructed argument array '" + args + "' from the queryAttributes List");
            }
        }
        //No queryAttributes are configured but the seed contains the default attribute
        else if (this.queryAttributes == null && seed.containsKey(this.getDefaultAttributeName())) {
            final String attrName = this.getDefaultAttributeName();
            args = Collections.singletonList(seed.get(attrName));
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Constructed argument array '" + args + "' from the defaultAttributeName='" + attrName + "'");
            }
        }
        //The data needed to run the query isn't in the seed, null is returned
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("The seed does not contain the required information to run the query, returning null.");
            }
            
            return null;
        }

        return this.getUserAttributesIfNeeded(args);
    }
    
    /**
     * Collates the first argument in each sub-list into an Object[] to pass to {@link #getUserAttributesIfNeeded(Object[])}
     * 
     * @see #getUserAttributesIfNeeded(Object[])
     */
    protected Map<String, List<Object>> getUserAttributesIfNeeded(final List<List<Object>> args) {
        final Object[] queryArgumentArray = this.getQueryArgumentArray(args);
        return this.getUserAttributesIfNeeded(queryArgumentArray);
    }
    
    /**
     * Is called by {@link #getUserAttributes(Map)} if the attributes required for the query, as defined
     * by the values of the queryAttributes property, are available in the seed. The implementation of
     * {@link #getUserAttributes(Map)} also compiles the array of query argument values based on the order
     * of items in the queryAttributes property and the values in the seed.
     * 
     * @param args The arguments to execute the query with.
     * @return The results of the query, as specified by {@link org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}
     */
    protected Map<String, List<Object>> getUserAttributesIfNeeded(final Object[] args) {
        throw new UnsupportedOperationException("getUserAttributesIfNeeded(Object[]) is not implemented");
    }

    /**
     * Converts the List of List of value type to an Object[]. This implementation uses the first value of each
     * sub-list as the value for the returned object array.
     * 
     * @param args the List of List of values to generate an object array from.
     * @return An array of arguments based on the lists
     */
    protected Object[] getQueryArgumentArray(final List<List<Object>> args) {
        final List<Object> queryArgs = new ArrayList<Object>(args.size());
        
        for (final List<Object> arg : args) {
            final Object value = arg.get(0);
            queryArgs.add(value);
        }
        
        return queryArgs.toArray();
    }

    /**
     * @return Returns the queryAttributes.
     */
    public final List<String> getQueryAttributes() {
        return this.queryAttributes;
    }

    /**
     * @param queryAttributes The queryAttributes to set.
     */
    public final void setQueryAttributes(List<String> queryAttributes) {
        Validate.notNull(queryAttributes, "queryAttributes cannot be null");
        
        //Create an unmodifiable defensive copy
        this.queryAttributes = Collections.unmodifiableList(new LinkedList<String>(queryAttributes));
    }
}
