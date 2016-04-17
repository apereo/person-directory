/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.services.persondir.support;

import org.jasig.services.persondir.AbstractPersonAttributeDaoTest;
import org.jasig.services.persondir.IPersonAttributeDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class RegexGatewayPersonAttributeDaoTest extends AbstractPersonAttributeDaoTest {

    // Instance Members.
    private final Map<String, List<Object>> attributes;
    private final IPersonAttributeDao enclosed;
    private final IPersonAttributeDao target;

    /*
     * Public API.
     */

    public RegexGatewayPersonAttributeDaoTest() {
        this.attributes = new HashMap<>();

        final List list = new ArrayList<>();
        list.add("(480) 555-1212");
        attributes.put("phone", list);
        this.enclosed = new StubPersonAttributeDao(attributes);
        this.target = new RegexGatewayPersonAttributeDao("username", ".*@.*", enclosed);
    }

    public void testConstructorParameters() {

        // attributeName.
        try {
            new RegexGatewayPersonAttributeDao(null, ".*@.*", enclosed);
            fail("NullPointerException should have been thrown with null 'attributeName'.");
        } catch (final NullPointerException iae) {
            // expected...
        }

        // pattern.
        try {
            new RegexGatewayPersonAttributeDao("username", null, enclosed);
            fail("NullPointerException should have been thrown with null 'pattern'.");
        } catch (final NullPointerException iae) {
            // expected...
        }

        // enclosed.
        try {
            new RegexGatewayPersonAttributeDao("username", ".*@.*", null);
            fail("NullPointerException should have been thrown with null 'enclosed'.");
        } catch (final NullPointerException iae) {
            // expected...
        }

    }

    public void testMatches() {
        final Map<String, List<Object>> results = target.getMultivaluedUserAttributes("monkey@yahoo.com");
        assertEquals(results, attributes);
    }

    public void testDoesNotMatch() {
        final Map<String, List<Object>> results = target.getMultivaluedUserAttributes("monkey");
        assertFalse(attributes.equals(results));
    }

    public void testGetPossibleNames() {
        assertEquals(enclosed.getPossibleUserAttributeNames(), target.getPossibleUserAttributeNames());
    }


    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.target;
    }
}
