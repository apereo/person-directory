/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support.rule;

import org.apereo.services.persondir.AbstractPersonAttributeDaoTest;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DeclaredRulePersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

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
            Collections.singletonList(this.rule));
    }

    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.target;
    }

    public void testConstructorParameters() {

        // attributeName.
        try {
            new DeclaredRulePersonAttributeDao(null, Collections.singletonList(this.rule));
            fail("IllegalArgumentException should have been thrown with null 'attributeName'.");
        } catch (final NullPointerException iae) {
            // expected...
        }

        // attributeName (empty List).
        try {
            new DeclaredRulePersonAttributeDao(NAME, new ArrayList<>());
            fail("IllegalArgumentException should have been thrown with null 'attributeName'.");
        } catch (final IllegalArgumentException iae) {
            // expected...
        }

        // rules.
        try {
            new DeclaredRulePersonAttributeDao(NAME, null);
            fail("NullPointerException should have been thrown with null 'pattern'.");
        } catch (final NullPointerException iae) {
            // expected...
        }

    }

    public void testMatches() {
        var results = target.getPerson("records-staff", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(results.getAttributes());
        assertEquals(Util.list(VALUE), results.getAttributes().get("fax"));
    }

    public void testDoesNotMatch() {
        var results = target.getPerson("faculty", IPersonAttributeDaoFilter.alwaysChoose());
        assertNull(results);
    }

    public void testGetPossibleNames() {
        final Set<String> s = new HashSet<>();
        s.add("fax");
        assertEquals(s, target.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()));
    }

}
