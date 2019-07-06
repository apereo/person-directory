package org.apereo.services.persondir.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to avoid nested Unmodifiable collections.
 *
 * Can't use instanceof b/c classes are not public and there are multiple sub-types of
 * Unmodifiable classes so doing an equality check against the class name doesn't work
 * hence the use of contains.
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
