/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support;

import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Eric Dalquist

 * @since uPortal 2.5
 */
public final class MultivaluedPersonAttributeUtils {

    /**
     * Translate from a more flexible Attribute to Attribute mapping format to a Map
     * from String to Set of Strings.
     *
     * The point of the map is to map from attribute names in the underlying data store
     * (e.g., JDBC column names, LDAP attribute names) to uPortal attribute names. 
     * Any given underlying data store attribute might map to zero uPortal
     * attributes (not appear in the map at all), map to exactly one uPortal attribute
     * (appear in the Map as a mapping from a String to a String or as a mapping
     * from a String to a Set containing just one String), or map to several uPortal
     * attribute names (appear in the Map as a mapping from a String to a Set
     * of Strings).
     *
     * This method takes as its argument a {@link Map} that must have keys of 
     * type {@link String} and values of type {@link String} or {@link Set} of 
     * {@link String}s.  The argument must not be null and must have no null
     * keys.  It must contain no keys other than Strings and no values other
     * than Strings or Sets of Strings.  This method will convert any non-string
     * values to a String using the object's toString() method.
     *
     * This method returns a Map equivalent to its argument except whereever there
     * was a String value in the Map there will instead be an immutable Set containing
     * the String value.  That is, the return value is normalized to be a Map from
     * String to Set (of String).
     *
     * @param mapping {@link Map} from String names of attributes in the underlying store 
     * to uP attribute names or Sets of such names.
     * @return a Map from String to Set of Strings
     */
    public static Map<String, Set<String>> parseAttributeToAttributeMapping(final Map<String, ? extends Object> mapping) {
        //null is assumed to be an empty map
        if (mapping == null) {
            return new HashMap<>();
        }

        final Map<String, Set<String>> mappedAttributesBuilder = new LinkedHashMap<>();

        for (final Map.Entry<String, ? extends Object> mappingEntry : mapping.entrySet()) {
            final String sourceAttrName = mappingEntry.getKey();
            Validate.notNull(sourceAttrName, "attribute name can not be null in Map");

            final Object mappedAttribute = mappingEntry.getValue();

            //Create a mapping to null
            if (mappedAttribute == null) {
                mappedAttributesBuilder.put(sourceAttrName, null);
            }
            //Create a single item set for the string mapping
            else if (mappedAttribute instanceof String) {
                final Set<String> mappedSet = new HashSet<>();
                mappedSet.add(mappedAttribute.toString());
                mappedAttributesBuilder.put(sourceAttrName, mappedSet);
            }
            //Create a defenisve copy of the mapped set & verify its contents are strings
            else if (mappedAttribute instanceof Collection) {
                final Collection<?> sourceSet = (Collection<?>) mappedAttribute;

                //Ensure the collection only contains strings.
                final Set<String> mappedSet = new LinkedHashSet<>();
                for (final Object sourceObj : sourceSet) {
                    if (sourceObj != null) {
                        mappedSet.add(sourceObj.toString());
                    } else {
                        mappedSet.add(null);
                    }
                }

                mappedAttributesBuilder.put(sourceAttrName, mappedSet);
            }
            //Not a valid type for the mapping
            else {
                throw new IllegalArgumentException("Invalid mapped type. key='" + sourceAttrName + "', value type='" + mappedAttribute.getClass().getName() + "'");
            }
        }

        return new HashMap<>(mappedAttributesBuilder);
    }

    /**
     * Adds a key/value pair to the specified {@link Map}, creating multi-valued
     * values when appropriate.
     * <br>
     * Since multi-valued attributes end up with a value of type
     * {@link List}, passing in a {@link List} of any type will
     * cause its contents to be added to the <code>results</code>
     * {@link Map} directly under the specified <code>key</code>
     *
     * @param <K> Key type (extends Object)
     * @param <V> Value type (extends Object)
     * @param results The {@link Map} to modify.
     * @param key The key to add the value for.
     * @param value The value to add for the key.
     * @throws IllegalArgumentException if any argument is null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void addResult(final Map<K, List<V>> results, final K key, final Object value) {
        Validate.notNull(results, "Cannot add a result to a null map.");
        Validate.notNull(key, "Cannot add a result with a null key.");

        // don't put null values into the Map.
        if (value == null) {
            return;
        }

        List<V> currentValue = results.get(key);
        if (currentValue == null) {
            currentValue = new LinkedList<>();
            results.put(key, currentValue);
        }

        if (value instanceof List) {
            currentValue.addAll((List<V>) value);
        } else {
            currentValue.add((V) value);
        }
    }

    /**
     * Takes a {@link Collection} and creates a flattened {@link Collection} out
     * of it.
     *
     * @param <T> Type of collection (extends Object)
     * @param source The {@link Collection} to flatten.
     * @return A flattened {@link Collection} that contains all entries from all levels of <code>source</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> flattenCollection(final Collection<? extends Object> source) {
        Validate.notNull(source, "Cannot flatten a null collection.");

        final Collection<T> result = new LinkedList<>();

        for (final Object value : source) {
            if (value instanceof Collection) {
                final Collection<Object> flatCollection = flattenCollection((Collection<Object>) value);
                result.addAll((Collection<T>) flatCollection);
            } else {
                result.add((T) value);
            }
        }

        return result;
    }

    /**
     * Convert the &lt;String, Object&gt; map to a &lt;String, List&lt;Object&gt;&gt; map by simply wrapping
     * each value in a List, unless Object is already a List in which cas it is returned.
     *
     * @param seed Map of objects
     * @return Map where each value is a List with the value(s) in it
     */
    public static Map<String, List<Object>> toMultivaluedMap(final Map<String, Object> seed) {
        Validate.notNull(seed, "seed can not be null");

        final Map<String, List<Object>> multiSeed = new LinkedHashMap<>(seed.size());
        for (final Map.Entry<String, Object> seedEntry : seed.entrySet()) {
            final String seedName = seedEntry.getKey();
            final Object seedValue = seedEntry.getValue();
            if (seedValue instanceof List) {
                multiSeed.put(seedName, (List<Object>) seedValue);
            } else {
                multiSeed.put(seedName, Collections.singletonList(seedValue));
            }
        }
        return multiSeed;
    }

    public static Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, ?> personAttributesMap,
                                                                           final IPersonAttributeDaoFilter filter) {
        final Map<String, List<Object>> personAttributes = new HashMap<>();

        for (final Map.Entry<String, ?> stringObjectEntry : personAttributesMap.entrySet()) {
            final Object value = stringObjectEntry.getValue();
            if (value instanceof List) {
                personAttributes.put(stringObjectEntry.getKey(), (List<Object>) value);
            } else {
                personAttributes.put(stringObjectEntry.getKey(), new ArrayList<>(Arrays.asList(value)));
            }
        }
        return personAttributes;
    }

    /**
     * This class is not meant to be instantiated.
     */
    private MultivaluedPersonAttributeUtils() {
        // private constructor makes this static utility method class
        // uninstantiable.
    }
}
