package org.jasig.services.persondir.support

/**
 * Test of CaseInsensitiveNamedPersonImpl
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

class CaseInsensitiveNamedPersonImplTest extends GroovyTestCase {
    // See https://issues.jasig.org/browse/PERSONDIR-89
    // Demonstrates that the case-insensitive attribute class is really case insensitive because
    // BasePersonImpl returns a Immutable map that wraps the ListOrderedMap wrapper which wraps the CaseInsensitiveMap
    // and ultimately allows the case insensitive aspect to be retained without mutating the attribute names to
    // all lower case.
    void testShowAttributeMapIsCaseInSensitive() {
        def attrs = ["Fifi":["HoHo", "HeHe"], "SAM":["I AM"]]
        CaseInsensitiveNamedPersonImpl obj = new CaseInsensitiveNamedPersonImpl("john", attrs)
        assertTrue obj.attributes.containsKey("Fifi")
        assertEquals ("HoHo", obj.attributes.get("Fifi")[0])
        assertTrue obj.attributes.containsKey("fifi")
    }

}
