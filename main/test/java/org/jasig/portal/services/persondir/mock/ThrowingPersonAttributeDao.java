package org.jasig.portal.services.persondir.mock;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * A mock, test implementation of ThrowingPersonAttributeDao which always
 * throws a RuntimeException.
 */
public class ThrowingPersonAttributeDao implements IPersonAttributeDao {

	/**
	 * @throws RuntimeException always
	 */
    public Map getUserAttributes(String uid) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
    
    /**
     * @throws RuntimeException always
     */
    public Map getUserAttributes(Map queryMap) {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }

    /**
     * @throws RuntimeException always
     */
    public Set getPossibleUserAttributeNames() {
        throw new RuntimeException("ThrowingPersonAttributeDao always throws");
    }
}