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
package org.apereo.services.persondir;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test conformance to IPersonAttributeDao interface specified
 * IllegalArgumentException throwing for illegal invocations of interface methods.
 *
 * @version $Revision: 43106 $ $Date: 2008-02-14 11:22:40 -0600 (Thu, 14 Feb 2008) $
 */
@SuppressWarnings("deprecation")
public abstract class AbstractPersonAttributeDaoTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ObjectMapper mapper = new ObjectMapper();

    public AbstractPersonAttributeDaoTest() {

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    /**
     * Get an instance of the type of IPersonAttributeDao the implementing
     * testcase is intended to test.
     * <p>
     * This method will be invoked exactly once per invocation of each test method
     * implemented in this abstract class.
     *
     * @return an IPersonAttributeDao instance for us to test
     */
    protected abstract IPersonAttributeDao getPersonAttributeDaoInstance();

    /**
     * Test that invocation of getMultivaluedUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    @Test
    public void testNullSeed() {
        var dao = getPersonAttributeDaoInstance();
        final Map<String, List<Object>> nullMap = null;
        try {
            dao.getPeopleWithMultivaluedAttributes(nullMap, IPersonAttributeDaoFilter.alwaysChoose());
        } catch (final RuntimeException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException or NullPointerException on getMultivaluedUserAttributes((Map)null)");

    }

    /**
     * Test that invocation of getUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    @Test
    public void testMultiNullSeed() {
        var dao = getPersonAttributeDaoInstance();
        final Map<String, Object> nullMap = null;
        try {
            dao.getPeople(nullMap, IPersonAttributeDaoFilter.alwaysChoose());
        } catch (final NullPointerException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes((Map)null)");

    }

    /**
     * Test that invocation of getMultivaluedUserAttributes(String null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    @Test
    public void testMultiNullUid() {
        var dao = getPersonAttributeDaoInstance();
        final String nullString = null;
        try {
            dao.getPerson(nullString, IPersonAttributeDaoFilter.alwaysChoose());
        } catch (final RuntimeException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getMultivaluedUserAttributes((String)null)");
    }

    /**
     * Test that invocation of getUserAttributes(String null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    @Test
    public void testNullUid() {
        var dao = getPersonAttributeDaoInstance();
        final String nullString = null;
        try {
            dao.getPerson(nullString, IPersonAttributeDaoFilter.alwaysChoose());
        } catch (final RuntimeException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes((String)null)");
    }

    /**
     * Test that invocation of getPersonAttributeDaoInstance() is not
     * null and immutable
     */
    @Test
    public void testPossibleSetConstraints() {
        var dao = getPersonAttributeDaoInstance();
        var possibleNames = dao.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        try {
            if (possibleNames != null) {
                var newObj = new String();
                possibleNames.add(newObj);
            }
        } catch (final UnsupportedOperationException e) {

        }
    }

    protected ObjectWriter getJsonWriter() {
        return this.mapper.writer(new DefaultPrettyPrinter());
    }

    protected String serializeJson(final Object obj) {
        try {
            var output = getJsonWriter().writeValueAsString(obj);
            logger.debug(output);
            return output;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T deserializeJson(final String json, final Class<T> clz) {
        try {
            var writer = new StringWriter();
            return this.mapper.readValue(json, clz);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}

