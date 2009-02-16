/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.ldap;

import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.springframework.ldap.core.AttributesMapper;

/**
 * Creates a Map for each Attributes result with attribute names as keys
 * and Lists of Attribute values for values.
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
class AttributeMapAttributesMapper implements AttributesMapper {
    private final boolean ignoreNull;
    
    public AttributeMapAttributesMapper() {
        this(false);
    }
    
    public AttributeMapAttributesMapper(boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
    }

    /* (non-Javadoc)
     * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        final int attributeCount = attributes.size();
        final Map<String, Object> mapOfAttrValues = this.createAttributeMap(attributeCount);
        
        for (final NamingEnumeration<? extends Attribute> attributesEnum = attributes.getAll(); attributesEnum.hasMore();) {
            final Attribute attribute = attributesEnum.next();
            
            if (!this.ignoreNull || attribute.size() > 0) {
                final String attrName = attribute.getID();
                final String key = this.getAttributeKey(attrName);

                final NamingEnumeration<?> valuesEnum = attribute.getAll();
                final List<?> values = this.getAttributeValues(valuesEnum);
                
                mapOfAttrValues.put(key, values);
            }
        }
        
        return mapOfAttrValues;
    }

    /**
     * Create a Map instance to be used as attribute map.
     * <br/>
     * By default, a linked case-insensitive Map will be created
     * 
     * @param attributeCount the attribute count, to be used as initial capacity for the Map
     * @return the new Map instance
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> createAttributeMap(int attributeCount) {
        return ListOrderedMap.decorate(new CaseInsensitiveMap(attributeCount > 0 ? attributeCount : 1));
    }

    /**
     * Determine the key to use for the given attribute in the attribute Map.
     * 
     * @param attributeName the attribute name as returned by the Attributes
     * @return the attribute key to use
     */
    protected String getAttributeKey(String attributeName) {
        return attributeName;
    }

    /**
     * Convert the Attribute's NamingEnumeration of values into a List
     * 
     * @param values The enumeration of Attribute values
     * @return The List version of the values enumeration
     */
    protected List<?> getAttributeValues(NamingEnumeration<?> values) {
        return EnumerationUtils.toList(values);
    }
}