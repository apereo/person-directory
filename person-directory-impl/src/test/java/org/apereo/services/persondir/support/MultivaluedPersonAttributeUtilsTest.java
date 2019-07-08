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
import org.apereo.services.persondir.util.CollectionsUtil;
import org.apereo.services.persondir.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JUnit testcase for MultivaluedPersonAttributeUtils.

 */
public class MultivaluedPersonAttributeUtilsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that an attempt to parse a null Map results in an empty Map.
     */
    public void testParseNullMapping() {
        final Map<String, Set<String>> emptyMap = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(null);
        assertEquals(Collections.EMPTY_MAP.size(), emptyMap.size());
    }

    /**
     * Test that an attempt to parse a Map with a null key results in
     * IllegalArgumentException.
     */
    public void testNullKeyMapping() {
        final Map<String, String> nullKeyMap = new HashMap<>();
        nullKeyMap.put("A", "B");
        nullKeyMap.put(null, "wombat");

        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (final NullPointerException iae) {
            // good
            return;
        }
        fail("Should have rejected map argument containing null key.");
    }

    /**
     * Test that an attempt to parse a Map with a null value results in
     * IllegalArgumentException.
     */
    public void testNullValueMapping() {
        final Map<String, String> nullKeyMap = new HashMap<>();
        nullKeyMap.put("A", "B");
        nullKeyMap.put("wombat", null);

        final Map<String, Set<String>> attrMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);

        final Map<String, Set<String>> expected = new HashMap<>();
        expected.put("A", Collections.singleton("B"));
        expected.put("wombat", null);

        assertEquals(expected, attrMapping);
    }

    /**
     * Test that an attempt to parse a Map containing as a value a Set containing
     * something other than a String fails with an IllegalArgumentException.
     */
    public void testNonStringContentsInValueSet() {
        final Map<String, Object> nullKeyMap = new HashMap<>();
        nullKeyMap.put("A", "B");

        final Set<Object> badSet = new HashSet<>();
        badSet.add("goodString");
        badSet.add(new Long(1234));
        badSet.add("anotherGoodString");

        nullKeyMap.put("wombat", badSet);

        final Map<String, Set<String>> attributeToAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);

        final Map<String, HashSet<String>> expected = new HashMap<>();
        expected.put("A", new HashSet<>(Arrays.asList("B")));
        expected.put("wombat", new HashSet<>(Arrays.asList("goodString", "1234", "anotherGoodString")));
        assertEquals(expected, attributeToAttributeMapping);
    }

    /**
     * Test a mapping for which no change is required.
     */
    public void testSimpleMapping() {
        final Map<String, Object> simpleMapping = new HashMap<>();
        simpleMapping.put("displayName", Collections.singleton("display_name"));

        final Set<String> uPortalEmailAttributeNames = new HashSet<>();
        uPortalEmailAttributeNames.add("mail");
        uPortalEmailAttributeNames.add("user.home-info.online.email");

        simpleMapping.put("email", uPortalEmailAttributeNames);

        assertEquals(simpleMapping, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping));
        assertNotSame(simpleMapping, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping));

        // test that the returned Map is immutable

        final Map<String, Set<String>> returnedMap = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping);
        returnedMap.put("foo", Collections.singleton("bar"));
        assertTrue(MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping).size() !=
                returnedMap.size());

    }

    /**
     * Test parsing a more complex mapping in which Sets need to be created.
     */
    public void testComplexMapping() {
        final Map<String, Object> testMap = new HashMap<>();
        final Map<String, Set<String>> expectedResult = new HashMap<>();

        // we expect translation from Strings to Set containing the String
        testMap.put("display_name", "displayName");
        expectedResult.put("display_name", Collections.singleton("displayName"));

        // we expect Sets containing a String to be left alone
        testMap.put("template_name", Collections.singleton("uPortalTemplateUserName"));
        expectedResult.put("template_name", Collections.singleton("uPortalTemplateUserName"));

        final Set<String> severalAttributes = new HashSet<>();

        severalAttributes.add("user.name.given");
        severalAttributes.add("givenName");

        // we expect Sets containing several Strings to be left alone
        testMap.put("given_name", severalAttributes);
        expectedResult.put("given_name", severalAttributes);

        assertEquals(expectedResult, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(testMap));

    }


    /**
     * Test that attempting to add a result to a null map yields IllegalArgumentException.
     */
    public void testAddResultToNullMap() {
        try {
            MultivaluedPersonAttributeUtils.addResult(null, "key", "value");
        } catch (final NullPointerException iae) {
            // good
            return;
        }
        fail("Attempting to addResult on a null Map should yield IllegalArgumentException.");
    }

    /**
     * Test that attempting to add a result with a null key yields IllegalArgumentException.
     */
    public void testAddResultNullKey() {
        try {
            MultivaluedPersonAttributeUtils.addResult(new HashMap<String, List<String>>(), null, "value");
        } catch (final NullPointerException iae) {
            // good
            return;
        }
        fail("Attempting to add a result with a null key should yield IllegalArgumentException.");
    }

    /**
     * Test that attempting to add a result with a null value yields no change.
     */
    public void testAddResultNullValue() {
        final Map<String, List<String>> immutableMap = CollectionsUtil.safelyWrapAsUnmodifiableMap(new HashMap<String, List<String>>());
        MultivaluedPersonAttributeUtils.addResult(immutableMap, "key", null);
    }

    /**
     * Test a simple non-colliding add.
     */
    public void testSimpleAdd() {
        final Map<String, List<String>> testMap = new HashMap<>();
        final Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("mail", Collections.singletonList("andrew.petro@yale.edu"));

        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "andrew.petro@yale.edu");

        assertEquals(expectedResult, testMap);

    }

    /**
     * Test that adding a second value for a given attribute converts that
     * attribute value to a List.
     */
    public void testCollidingAdd() {
        final Map<String, List<String>> testMap = new HashMap<>();

        final List<String> phoneNumbers = new ArrayList<>();
        phoneNumbers.add("555-1234");
        phoneNumbers.add("555-4321");
        testMap.put("phone", phoneNumbers);

        final List<Object> emailAddys = new ArrayList<>();
        emailAddys.add("andrew.petro@yale.edu");
        emailAddys.add("awp9@pantheon.yale.edu");

        final Map<String, List<Object>> expectedMap = new HashMap<>();
        expectedMap.put("mail", Util.list("andrew.petro@yale.edu"));
        expectedMap.put("phone", new ArrayList<Object>(phoneNumbers));

        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "andrew.petro@yale.edu");
        assertEquals(expectedMap, testMap);

        expectedMap.put("mail", emailAddys);
        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "awp9@pantheon.yale.edu");
        assertEquals(expectedMap, testMap);

        expectedMap.get("phone").add("555-0000");
        MultivaluedPersonAttributeUtils.addResult(testMap, "phone", "555-0000");
        assertEquals(expectedMap, testMap);

        final List<String> moreNumbers = new ArrayList<>();
        moreNumbers.add("555-3145");
        moreNumbers.add("555-1337");

        expectedMap.get("phone").addAll(moreNumbers);
        MultivaluedPersonAttributeUtils.addResult(testMap, "phone", moreNumbers);
        assertEquals(expectedMap, testMap);
    }

    /**
     * Test adding a List where the current attribute value is not a List.
     */
    public void testAddListToNonList() {

        final Map<String, List<Object>> testMap = new HashMap<>();
        final Map<String, List<Object>> expectedMap = new HashMap<>();

        testMap.put("mail", Util.list("andrew.petro@yale.edu"));

        final List<Object> additionalEmails = new ArrayList<>();
        additionalEmails.add("awp9@pantheon.yale.edu");
        additionalEmails.add("awp9@tp.its.yale.edu");

        final List<Object> expectedList = new ArrayList<>();
        expectedList.add("andrew.petro@yale.edu");
        expectedList.addAll(additionalEmails);

        expectedMap.put("mail", expectedList);

        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", additionalEmails);

        assertEquals(expectedMap, testMap);

    }

    /**
     * Test adding a non-List to an attribute that is currently a List.
     */
    public void testAddStringToList() {
        final Map<String, List<Object>> testMap = new HashMap<>();
        final Map<String, List<Object>> expectedMap = new HashMap<>();

        final Date loginDate = new Date();

        expectedMap.put("loginTimes", Util.list(loginDate));

        MultivaluedPersonAttributeUtils.addResult(testMap, "loginTimes", loginDate);

        assertEquals(expectedMap, testMap);

        final Date anotherLoginDate = new Date();

        final Date yetAnotherLoginDate = new Date();

        final List<Object> dateList = new ArrayList<>();
        dateList.add(anotherLoginDate);
        dateList.add(yetAnotherLoginDate);

        final List<Object> expectedDateList = new ArrayList<>();
        expectedDateList.add(loginDate);
        expectedDateList.add(anotherLoginDate);
        expectedDateList.add(yetAnotherLoginDate);

        expectedMap.put("loginTimes", expectedDateList);

        MultivaluedPersonAttributeUtils.addResult(testMap, "loginTimes", dateList);

        assertEquals(expectedMap, testMap);
    }

    /**
     * Test that attempting to flatten a null Collection yields
     * IllegalArgumentException.
     */
    public void testFlattenNullCollection() {
        try {
            MultivaluedPersonAttributeUtils.flattenCollection(null);
        } catch (final NullPointerException iae) {
            // good
            return;
        }
        fail("Attempt to flatten a null collection should yield IllegalArgumentException.");
    }

    /**
     * Test flattening a Collection containing collections (and collections of collections).
     *
     */
    public void testFlattenCollection() {
        final Set<Object> setOfSets = new HashSet<>();
        final Set<Object> setOfLists = new HashSet<>();
        final List<Object> listOfStrings = new ArrayList<>();
        listOfStrings.add("wombat");
        listOfStrings.add("fido");
        listOfStrings.add("foo");
        listOfStrings.add("bar");

        final List<Object> listOfDates = new ArrayList<>();
        final Date date1 = new Date();
        final Date date2 = new Date();
        // ensure that date2 does not equal date1.
        date2.setTime(date1.getTime() + 100);

        listOfDates.add(date1);
        listOfDates.add(date2);

        setOfLists.add(listOfStrings);
        setOfLists.add(listOfDates);

        setOfSets.add(setOfLists);

        final Set<Object> expectedResult = new HashSet<>();
        expectedResult.addAll(listOfStrings);
        expectedResult.addAll(listOfDates);

        final Collection<Object> flattened = MultivaluedPersonAttributeUtils.flattenCollection(setOfSets);
        assertTrue(expectedResult.containsAll(flattened));
        assertTrue(flattened.containsAll(expectedResult));
        assertEquals(expectedResult.size(), flattened.size());

    }

}

