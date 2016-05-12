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
package org.apereo.services.persondir.support.rule;

import junit.framework.TestCase;
import org.apereo.services.persondir.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleAttributeRuleTest extends TestCase {

    private static final String WHEN_KEY = "eduPersonPrimaryAffiliation";
    private static final String WHEN_PATTERN = "records-staff";
    private static final String SET_UID = "username";
    private static final String SET_KEY = "fax";
    private static final String SET_VALUE = "(480) 555-1212";

	/*
     * Public API.
	 */

    public SimpleAttributeRuleTest() {
    }

    public void testConstructorParameters() {

        // whenKey.
        try {
            new SimpleAttributeRule(null, WHEN_PATTERN, SET_UID, SET_KEY, SET_VALUE);
            fail("IllegalArgumentException should have been thrown with null 'whenKey'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }

        // whenPattern.
        try {
            new SimpleAttributeRule(WHEN_KEY, null, SET_UID, SET_KEY, SET_VALUE);
            fail("IllegalArgumentException should have been thrown with null 'whenPattern'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }

        // setKey.
        try {
            new SimpleAttributeRule(WHEN_KEY, WHEN_PATTERN, SET_UID, null, SET_VALUE);
            fail("IllegalArgumentException should have been thrown with null 'setKey'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }

        // setValue.
        try {
            new SimpleAttributeRule(WHEN_KEY, WHEN_PATTERN, SET_UID, SET_KEY, null);
            fail("IllegalArgumentException should have been thrown with null 'setValue'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }


    }

    public void testAppliesToParameters() {

        final AttributeRule r = new SimpleAttributeRule(WHEN_KEY, WHEN_PATTERN, SET_UID, SET_KEY, SET_VALUE);

        // null.
        try {
            r.appliesTo(null);
            fail("IllegalArgumentException should have been thrown with null 'userInfo'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }

        final Map<String, List<Object>> m = new HashMap<>();

        // String --> true.
        m.put(WHEN_KEY, Util.list(WHEN_PATTERN));
        assertTrue(r.appliesTo(m));

        // String --> false.
        m.put(WHEN_KEY, Util.list("monkey"));
        assertFalse(r.appliesTo(m));

    }

}
