/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test conformance to IPersonAttributeDao interface specified 
 * IllegalArgumentException throwing for illegal invocations of interface methods.
 * @version $Revision: 43106 $ $Date: 2008-02-14 11:22:40 -0600 (Thu, 14 Feb 2008) $
 */
@SuppressWarnings("deprecation")
public abstract class AbstractPersonAttributeDaoTest extends TestCase {
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
     * 
     * This method will be invoked exactly once per invocation of each test method
     * implemented in this abstract class.
     
     * @return an IPersonAttributeDao instance for us to test
     */
    protected abstract IPersonAttributeDao getPersonAttributeDaoInstance();

    /**
     * Test that invocation of getMultivaluedUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's 
     * interface declaration.
     */
    public void testNullSeed() {
        final IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        final Map<String, List<Object>> nullMap = null;
        try {
            dao.getMultivaluedUserAttributes(nullMap);
        } catch (final RuntimeException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getMultivaluedUserAttributes((Map)null)");

    }

    /**
     * Test that invocation of getUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's 
     * interface declaration.
     */
    public void testMultiNullSeed() {
        final IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        final Map<String, Object> nullMap = null;
        try {
            dao.getUserAttributes(nullMap);
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
    public void testMultiNullUid() {
        final IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        final String nullString = null;
        try {
            dao.getMultivaluedUserAttributes(nullString);
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
    public void testNullUid() {
        final IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        final String nullString = null;
        try {
            dao.getUserAttributes(nullString);
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
    public void testPossibleSetConstraints() {
        final IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        final Set<String> possibleNames = dao.getPossibleUserAttributeNames();
        
        if (possibleNames != null) {
            try {
                final int originalSize = possibleNames.size();
                
                final String newObj = new String();
                possibleNames.add(newObj);
                
                assertEquals(originalSize, possibleNames.size());
            }
            catch (final Exception e) {
                //An exception may be thrown since the Set should be immutable.
            }
        }
    }

    protected ObjectWriter getJsonWriter() {
        return this.mapper.writer(new DefaultPrettyPrinter());
    }

    protected String serializeJson(final Object obj) {
        try {
            final StringWriter writer = new StringWriter();
            getJsonWriter().writeValue(writer, obj);
            final String output = writer.getBuffer().toString();
            logger.debug(output);
            return output;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T deserializeJson(final String json, final Class<T> clz) {
        try {
            final StringWriter writer = new StringWriter();
            return this.mapper.readValue(json, clz);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T evalJson(final T obj) {
        final Class<T> clz = (Class<T>) obj.getClass();
        return deserializeJson(serializeJson(obj), clz);
    }

}

