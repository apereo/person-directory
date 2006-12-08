package org.jasig.portal.services.persondir.support.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

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
							"records-staff", "fax", VALUE);
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
			new DeclaredRulePersonAttributeDao(NAME, new ArrayList());
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
        Map results = target.getUserAttributes("records-staff");
        assertTrue(VALUE.equals(results.get("fax")));
	}
	
	public void testDoesNotMatch() {
        Map results = target.getUserAttributes("faculty");
		assertNull(results);
	}

	public void testGetPossibleNames() {
		Set s = new HashSet();
		s.add("fax");
		assertEquals(s, target.getPossibleUserAttributeNames());
	}

}