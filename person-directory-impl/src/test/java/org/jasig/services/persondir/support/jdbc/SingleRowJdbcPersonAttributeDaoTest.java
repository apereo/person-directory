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
package org.jasig.services.persondir.support.jdbc;

import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.SimpleUsernameAttributeProvider;
import org.jasig.services.persondir.util.Util;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test the {@link SingleRowJdbcPersonAttributeDao} against a dummy DataSource.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist

 */
public class SingleRowJdbcPersonAttributeDaoTest
        extends AbstractCaseSensitivityJdbcPersonAttributeDaoTest {

    @Override
    protected void setUpSchema(final DataSource dataSource) throws SQLException {
        final Connection con = dataSource.getConnection();

        con.prepareStatement("CREATE TABLE user_table " +
                "(netid VARCHAR, " +
                "name VARCHAR, " +
                "email VARCHAR, " +
                "shirt_color VARCHAR)").execute();

        con.prepareStatement("INSERT INTO user_table " +
                "(netid, name, email, shirt_color) " +
                "VALUES ('awp9', 'Andrew', 'andrew.petro@yale.edu', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                "(netid, name, email, shirt_color) " +
                "VALUES ('edalquist', 'Eric', 'edalquist@unicon.net', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                "(netid, name, email, shirt_color) " +
                "VALUES ('atest', 'Andrew', 'andrew.test@test.net', 'red')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                "(netid, name, email, shirt_color) " +
                "VALUES ('susan', 'Susan', 'susan.test@test.net', null)").execute();

        con.close();
    }

    @Override
    protected void tearDownSchema(final DataSource dataSource) throws SQLException {
        final Connection con = dataSource.getConnection();

        con.prepareStatement("DROP TABLE user_table").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();
    }

    @Override
    protected AbstractJdbcPersonAttributeDao<Map<String, Object>> newDao(final DataSource dataSource) {
        return new SingleRowJdbcPersonAttributeDao(dataSource, "SELECT netid, name, email, shirt_color FROM user_table WHERE {0}");
    }

    @Override
    protected boolean supportsPerDataAttributeCaseSensitivity() {
        return true;
    }

    @Override
    protected void beforeNonUsernameQuery(final AbstractJdbcPersonAttributeDao<Map<String, Object>> dao) {
        // no-op
    }

    public void testNoQueryAttributeMapping() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE netid = 'awp9'");
        impl.setUseAllQueryAttributes(false);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("blue"), attribs.get("dressShirtColor"));
        assertNull(attribs.get("shirt_color"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test that the implementation properly reports the attribute names it
     * expects to map.
     */
    public void testPossibleUserAttributeNames() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Set<String> expectedAttributeNames = new HashSet<>();
        expectedAttributeNames.add("firstName");
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("emailAddress");
        expectedAttributeNames.add("dressShirtColor");

        final Set<String> attributeNames = impl.getPossibleUserAttributeNames();
        assertEquals(attributeNames, expectedAttributeNames);
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSingleAttrQuery() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("blue"), attribs.get("dressShirtColor"));
        assertNull(attribs.get("shirt_color"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testNullColumnName() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE netid = {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", null));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        columnsToAttributes.put("email", "emailAddress");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeMapping() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", null);
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("blue"), attribs.get("shirt_color"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeName() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("", "dressShirtColor");

        try {
            impl.setResultAttributeMapping(columnsToAttributes);
            fail("IllegalArgumentException if the ColumnsToAttributes Map has an empty Key");
        } catch (final IllegalArgumentException iae) {
            //expected
        }
    }

    /**
     * Test for a query with a null value attribute
     */
    public void testNullAttrQuery() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("susan");
        assertNull(attribs.get("dressShirtColor"));
        assertEquals(Util.list("Susan"), attribs.get("firstName"));
    }

    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);


        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("shirtColor", Util.list("blue"));
        queryMap.put("Name", Util.list("John"));

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNotNull(attribs);
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);
        impl.setRequireAllQueryAttributes(true);

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("Name", Util.list("John"));

        final Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }

    /**
     * Test for a query with a single attribute
     */
    public void testMultiPersonQuery() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));
        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new HashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);

        impl.setResultAttributeMapping(columnsToAttributes);

        final Map<String, List<Object>> queryMap = new HashMap<>();
        queryMap.put("shirt", Util.list("blue"));

        try {
            impl.getMultivaluedUserAttributes(queryMap);
        } catch (final IncorrectResultSizeDataAccessException irsdae) {
            // good, exception thrown for multiple results
            return;
        }

        fail("JdbcPersonAttributeDao should have thrown IncorrectResultSizeDataAccessException for multiple results");
    }

    public void testProperties() {
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");

        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));
        assertEquals(Collections.singletonMap("shirt", Collections.singleton("shirt_color")), impl.getQueryAttributeMapping());

        final Map<String, Object> columnsToAttributes = new HashMap<>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        final Map<String, Set<String>> expectedColumnsToAttributes = new HashMap<>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        impl.setResultAttributeMapping(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "shirt";
        final List<String> queryAttrList = new LinkedList<>();
        queryAttrList.add(queryAttr);

        // shirt_color = ?

        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(this.testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));

        return impl;
    }

}
