/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * Implementation of {@link IPersonAttributes} for use with 
 * {@link AdditionalDescriptorsPersonAttributeDao}.  Unlike most other 
 * implementations, <code>AdditionalDescriptors</code> is mutable.  An instance 
 * of this class typically lives for the duration of a session or application, 
 * and external components are expected to add attributes to the collection 
 * after creation. 
 * 
 * @author awills
 */
public class AdditionalDescriptors implements IPersonAttributes {

    // Static Members.
    private static final long serialVersionUID = 1L;
    
    // Private Members.
    private String name = null;
    private final Map<String,List<Object>> attributes = new ConcurrentHashMap<String,List<Object>>();

    /*
     * Public API.
     */

    public Object getAttributeValue(String name) {
        List<Object> values = attributes.get(name);
        return values == null || values.size() == 0 ? null 
                                        : values.get(0);
    }

    public List<Object> getAttributeValues(String name) {
        return new LinkedList<Object>(attributes.get(name));
    }
    
    public void setAttributeValues(String name, List<Object> values) {
        
        // Assertions.
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        attributes.put(name, values);
        
    }

    public Map<String, List<Object>> getAttributes() {
        return new HashMap<String, List<Object>>(attributes);
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {

        // Assertions.
        if (name == null) {
            // NB:  This assertion may not be necessary/appropriate...
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        this.name = name;

    }

}
