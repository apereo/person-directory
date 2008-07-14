package org.jasig.services.persondir.support.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.Util;

@SuppressWarnings("deprecation")
public class DeclaredRulePersonAttributeDaoTest extends TestCase {

	private static final String NAME = "eduPersonPrimaryAffiliation"; 
	private static final String VALUE = "(480) 555-1212"; 
	
	// Instance Members.
	private final AttributeRule rule;
	private final IPersonAttributeDao target;
	
	/*
	 * Public API.
	 */
	
	public DeclaredRulePersonAttributeDaoTest() {
		this.rule = new SimpleAttributeRule(NAME, 
							"records-staff", "userName", "fax", VALUE);
		this.target = new DeclaredRulePersonAttributeDao(NAME, 
								Arrays.asList(new AttributeRule[] { this.rule }));
	}

	public void testConstructorParameters() {

		// attributeName.
		try {
			new DeclaredRulePersonAttributeDao(null, Arrays.asList(new AttributeRule[] { this.rule }));
			fail("IllegalArgumentException should have been thrown with null 'attributeName'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

		// attributeName (empty List).
		try {
			new DeclaredRulePersonAttributeDao(NAME, new ArrayList<AttributeRule>());
			fail("IllegalArgumentException should have been thrown with null 'attributeName'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

		// rules.
		try {
			new DeclaredRulePersonAttributeDao(NAME, null);
			fail("IllegalArgumentException should have been thrown with null 'pattern'.");
		} catch (IllegalArgumentException iae) {
			// expected...
		}

	}

	public void testMatches() {
        Map<String, List<Object>> results = target.getMultivaluedUserAttributes("records-staff");
        assertNotNull(results);
        assertEquals(Util.list(VALUE), results.get("fax"));
	}
	
	public void testDoesNotMatch() {
        Map<String, List<Object>> results = target.getMultivaluedUserAttributes("faculty");
		assertNull(results);
	}

	public void testGetPossibleNames() {
		Set<String> s = new HashSet<String>();
		s.add("fax");
		assertEquals(s, target.getPossibleUserAttributeNames());
	}

}