package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributeDao;

public class RegexGatewayPersonAttributeDaoTest extends TestCase {

	// Instance Members.
	private final Map<String, List<Object>> attributes;
	private final IPersonAttributeDao enclosed;
	private final IPersonAttributeDao target;
	
	/*
	 * Public API.
	 */
	
	public RegexGatewayPersonAttributeDaoTest() {
		this.attributes = new HashMap<String, List<Object>>();
		attributes.put("phone", Collections.singletonList((Object)"(480) 555-1212"));
		this.enclosed = new StubPersonAttributeDao(attributes);
		this.target = new RegexGatewayPersonAttributeDao("username", ".*@.*", enclosed);
	}

	public void testConstructorParameters() {

		// attributeName.
		try {
			new RegexGatewayPersonAttributeDao(null, ".*@.*", enclosed);
			fail("IllegalArgumentException should have been thrown with null 'attributeName'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

		// pattern.
		try {
			new RegexGatewayPersonAttributeDao("username", null, enclosed);
			fail("IllegalArgumentException should have been thrown with null 'pattern'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

		// enclosed.
		try {
			new RegexGatewayPersonAttributeDao("username", ".*@.*", null);
			fail("IllegalArgumentException should have been thrown with null 'enclosed'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

	}
	
	public void testMatches() {
        Map<String, List<Object>> results = target.getMultivaluedUserAttributes("monkey@yahoo.com");
		assertEquals(results, attributes);
	}
	
	public void testDoesNotMatch() {
        Map<String, List<Object>> results = target.getMultivaluedUserAttributes("monkey");
		assertFalse(attributes.equals(results));
	}
	
	public void testGetPossibleNames() {
		assertEquals(enclosed.getPossibleUserAttributeNames(), target.getPossibleUserAttributeNames());
	}
	
}