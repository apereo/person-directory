/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * An immutable representation of a person with a uid (userName) and attributes. A user's attributes can be of any type
 * and can be multi-valued.
 * 
 * {@link Principal#getName()} is used for the uid (userName).
 * 
 * The equality and hashCode of an IPersonAttributes should ONLY include the name property and none of the attributes.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPersonAttributes extends Principal, Serializable {
    /**
     * @return The immutable Map of all attributes for the person.
     */
    public Map<String, List<Object>> getAttributes();
    
    /**
     * The value for the attribute, null if no value exists or the first value is null, if there are multiple values
     * the first is returned.
     * 
     * @param name The name of the attribute to get the value for
     * @return The first value for the attribute
     */
    public Object getAttributeValue(String name);
    
    /**
     * All values of the attribute, null if no values exist.
     * 
     * @param name The name of the attribute to get the values for
     * @return All values for the attribute
     */
    public List<Object> getAttributeValues(String name);
}
