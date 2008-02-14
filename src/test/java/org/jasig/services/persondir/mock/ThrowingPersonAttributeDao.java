package org.jasig.services.persondir.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * A mock, test implementation of ThrowingPersonAttributeDao which always
 * throws a RuntimeException.
 */
public class ThrowingPersonAttributeDao implements IPersonAttributeDao {

    /**
     * @throws RuntimeException always
     */
    public Map<String, List<Object>> getMultivaluedUserAttributes(Map<String, List<Object>> seed) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /**
     * @throws RuntimeException always
     */
    public Map<String, List<Object>> getMultivaluedUserAttributes(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /**
     * @throws RuntimeException always
     */
    public Set<String> getPossibleUserAttributeNames() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /**
     * @throws RuntimeException always
     */
    public Map<String, Object> getUserAttributes(Map<String, Object> seed) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /**
     * @throws RuntimeException always
     */
    public Map<String, Object> getUserAttributes(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
    
}