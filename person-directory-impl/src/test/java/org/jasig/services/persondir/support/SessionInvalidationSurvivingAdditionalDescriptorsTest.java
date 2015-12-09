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
package org.jasig.services.persondir.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * JUnit test class for {@link SessionScopedFallThroughToRequestScopedAdditionalDescriptors}.
 */
public class SessionInvalidationSurvivingAdditionalDescriptorsTest {

    @Mock private IAdditionalDescriptors sessionScopedAdditionalDescriptors;
    @Mock private IAdditionalDescriptors requestScopedAdditionalDescriptors;

    private SessionInvalidationSurvivingAdditionalDescriptors additionalDescriptors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.additionalDescriptors = new SessionInvalidationSurvivingAdditionalDescriptors();
        this.additionalDescriptors.setSessionScopedAdditionalDescriptors(this.sessionScopedAdditionalDescriptors);
        this.additionalDescriptors.setRequestScopedAdditionalDescriptors(this.requestScopedAdditionalDescriptors);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAttributesMethodShouldReturnUnionOfSessionAndRequestAttributes() {
        // given
        final Map<String, List<Object>> sessionAttributes = this.createAttributeValueMap("a", "alpha", "b", "beta");
        final Map<String, List<Object>> requestAttributes = this.createAttributeValueMap("b", "betamax", "g", "gamma");
        given(this.sessionScopedAdditionalDescriptors.getAttributes()).willReturn(sessionAttributes);
        given(this.requestScopedAdditionalDescriptors.getAttributes()).willReturn(requestAttributes);
        // when
        final Map<String, List<Object>> results = this.additionalDescriptors.getAttributes();
        // then
        this.assertExpectedResultsFound(results, "a", "alpha");
        this.assertExpectedResultsFound(results, "b", "beta", "betamax");
        this.assertExpectedResultsFound(results, "g", "gamma");
    }

    @Test
    public void getAttributeValueMethodShouldReturnValueFromSessionIfValueFound() {
        // given
        this.sessionAndRequestBeansBothHaveKeyWithValue("a", "alpha", "omega");
        // when
        final Object result = this.additionalDescriptors.getAttributeValue("a");
        // then
        assertEquals("alpha", (String)result);
    }

    @Test
    public void getAttributeValueMethodShouldReturnValueFromRequestIfSessionHasNoValue() {
        // given
        this.sessionDoesNotContainKey("a");
        this.requestHasKeyWithValue("a", "omega");
        // when
        final Object result = this.additionalDescriptors.getAttributeValue("a");
        // then
        assertEquals("omega", (String)result);
    }

    private void sessionAndRequestBeansBothHaveKeyWithValue(final String key, final String sessionValue, final String requestValue) {
        this.sessionHasKeyWithValue(key, sessionValue);
        this.requestHasKeyWithValue(key, requestValue);
    }

    private void sessionDoesNotContainKey(final String key) {
        final Map<String, List<Object>> sessionAttributes = Collections.emptyMap();
        given(this.sessionScopedAdditionalDescriptors.getAttributes()).willReturn(sessionAttributes);
    }

    private void sessionHasKeyWithValue(final String key, final String value) {
        final Map<String, List<Object>> sessionAttributes = this.createAttributeValueMap(key, value);
        given(this.sessionScopedAdditionalDescriptors.getAttributeValue(key)).willReturn(value);
        given(this.sessionScopedAdditionalDescriptors.getAttributes()).willReturn(sessionAttributes);
    }

    private void requestHasKeyWithValue(final String key, final String value) {
        final Map<String, List<Object>> requestAttributes = this.createAttributeValueMap(key, value);
        given(this.requestScopedAdditionalDescriptors.getAttributeValue(key)).willReturn(value);
        given(this.requestScopedAdditionalDescriptors.getAttributes()).willReturn(requestAttributes);
    }

    private void assertExpectedResultsFound(final Map<String, List<Object>> results, final String key, final String...expectedValues) {
       assertNotNull(results);
       final List<Object> resultsForKey = results.get(key);
       assertNotNull(resultsForKey);
       final Set<String> values = this.convertToSet(expectedValues);
       for (String value : values) {
           assertTrue(resultsForKey.contains(value));
       }
    }

    private Set<String> convertToSet(final String...strings) {
        final Set<String> results = new HashSet<String>(strings.length);
        for (String str : strings) {
            results.add(str);
        }
        return results;
    }

    private Map<String, List<Object>> createAttributeValueMap(final String key, final String value) {
        final Map<String, List<Object>> result = new HashMap<String, List<Object>>();
        result.put(key, Collections.singletonList((Object)value));
        return result;
    }

    private Map<String, List<Object>> createAttributeValueMap(
            final String key1, final String value1,
            final String key2, final String value2) {
        final Map<String, List<Object>> result = new HashMap<String, List<Object>>();
        result.put(key1, Collections.singletonList((Object)value1));
        result.put(key2, Collections.singletonList((Object)value2));
        return result;
    }

}
