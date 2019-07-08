package org.apereo.services.persondir.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to avoid deeply nested Unmodifiable collections and accompanying stack overflow.
 *
 * If a collection or map is wrapped with an Unmodifiable wrapper repeatedly, eventually
 * a call to `get()` an item will result in a StackOverflowError.
 * It can be difficult to keep track of whether a collection has already been wrapped
 * since the classes are not public so an instanceof check won't work.
 *
 * There are multiple sub-types of Unmodifiable classes so doing an equality check
 * against the class name doesn't work hence the use of contains looking for the word
 * "Unmodifiable" in the class name.
 *
 * This class should eventually go away if the JDK provides a solution.
 */
public class CollectionsUtil {

    public static boolean isUnmodifiableMap(Map map) {
        return (map != null && map.getClass().getSimpleName().contains("Unmodifiable"));
    }

    public static boolean isUnmodifiableCollection(Collection coll) {
        return (coll != null && coll.getClass().getSimpleName().contains("Unmodifiable"));
    }

    public static <K,V> Map<K, V> safelyWrapAsUnmodifiableMap(Map<K, V> map) {
        return isUnmodifiableMap(map) ? map : Collections.unmodifiableMap(map);
    }

    public static <T> Collection<T> safelyWrapAsUnmodifiableCollection(Collection<T> collection) {
        return isUnmodifiableCollection(collection)? collection : Collections.unmodifiableCollection(collection);
    }

    public static <T> Set<T> safelyWrapAsUnmodifiableSet(Set<T> set) {
        return isUnmodifiableCollection(set) ? set : Collections.unmodifiableSet(set);
    }

    public static <T> List<T> safelyWrapAsUnmodifiableList(List<T> list) {
        return isUnmodifiableCollection(list) ? list : Collections.unmodifiableList(list);
    }
}
