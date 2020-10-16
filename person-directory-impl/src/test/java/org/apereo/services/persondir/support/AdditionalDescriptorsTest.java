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
package org.apereo.services.persondir.support;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class AdditionalDescriptorsTest extends TestCase {

    private static final List<Object> VALUES_LIST = Arrays.asList(new Object[]{"ONE", "TWO", "THREE"});

    /*
     * Public API.
     */

    public void testGetAttributeValue() {

        final var ad = new AdditionalDescriptors();
        ad.setAttributeValues("foo", VALUES_LIST);

        TestCase.assertEquals(ad.getAttributeValue("foo"), VALUES_LIST.get(0));

        TestCase.assertFalse(VALUES_LIST.get(1).equals(ad.getAttributeValue("foo")));

        TestCase.assertNull(ad.getAttributeValue("bar"));

    }

    public void testGetAttributeValues() {

        final var ad = new AdditionalDescriptors();
        ad.setAttributeValues("foo", VALUES_LIST);

        TestCase.assertEquals(ad.getAttributeValues("foo"), VALUES_LIST);

        TestCase.assertNull(ad.getAttributeValue("bar"));

    }

    public void testSetAttributeValues() {

        final var ad = new AdditionalDescriptors();

        var caught = false;
        try {
            ad.setAttributeValues(null, VALUES_LIST);
        } catch (final IllegalArgumentException iae) {
            caught = true;
        }

        if (!caught) fail();

    }

}
