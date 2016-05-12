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
package org.apereo.services.persondir.support

/**
 * Test of CaseInsensitiveNamedPersonImpl
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

class CaseInsensitiveNamedPersonImplTest extends GroovyTestCase {
    // See https://issues.jasig.org/browse/PERSONDIR-89
    // Demonstrates that the case-insensitive attribute class is really case insensitive because
    // BasePersonImpl returns a Immutable map that wraps the ListOrderedMap wrapper which wraps the CaseInsensitiveMap
    // and ultimately allows the case insensitive key comparison to be retained without mutating the attribute names to
    // all lower case.
    void testShowAttributeMapIsCaseInSensitive() {
        def attrs = ["Fifi":["HoHo", "HeHe"], "SAM":["I AM"]]
        CaseInsensitiveNamedPersonImpl obj = new CaseInsensitiveNamedPersonImpl("john", attrs)
        assertTrue obj.attributes.containsKey("Fifi")
        assertEquals ("HoHo", obj.attributes.get("Fifi")[0])
        assertEquals ("HoHo", obj.attributes.get("fifi")[0])
        assertTrue obj.attributes.containsKey("fifi")
        assertTrue obj.getAttributes().containsKey("FiFi")  // Match what AbstractQueryPersonAttributeDao.mapPersonAttributes does
        assertTrue obj.getAttributes().containsKey("fifi")  // Match what AbstractQueryPersonAttributeDao.mapPersonAttributes does
    }

}
