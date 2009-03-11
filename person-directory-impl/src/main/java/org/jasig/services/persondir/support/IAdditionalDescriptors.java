/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * Interface that describes what is essentially a mutable {@link IPersonAttributes} object
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IAdditionalDescriptors extends IPersonAttributes {

    /**
     * @param name The user name for the attributes
     */
    public void setName(String name);

    /**
     * @param attributes Attributes to add to the existing attribute Map
     */
    public void addAttributes(Map<String, List<Object>> attributes);

    /**
     * This should be atomic to the view of other methods on this interface.
     * 
     * @param attributes Replace all existing attributes witht he specified Map
     */
    public void setAttributes(Map<String, List<Object>> attributes);

    /**
     * Sets the specified attribute values
     * 
     * @param name Name of the attribute, must not be null
     * @param values Values for the attribute, may be null
     * @return The previous values for the attribute if they existed
     */
    public List<Object> setAttributeValues(String name, List<Object> values);

    /**
     * @param name Removes the specified attribute, must not be null
     * @return The removed values for the attribute if they existed
     */
    public List<Object> removeAttribute(String name);
}