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

package org.jasig.services.persondir.support.jdbc;

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

import javax.sql.DataSource;

import org.hsqldb.jdbcDriver;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.jasig.services.persondir.support.SimpleUsernameAttributeProvider;
import org.jasig.services.persondir.util.Util;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Test the {@link SingleRowJdbcPersonAttributeDao} against a dummy DataSource.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class SingleRowJdbcPersonAttributeDaoTest 
    extends AbstractCaseSensitivityJdbcPersonAttributeDaoTest {

    @Override
    protected void setUpSchema(DataSource dataSource) throws SQLException {
        Connection con = dataSource.getConnection();

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
    protected void tearDownSchema(DataSource dataSource) throws SQLException {
        Connection con = dataSource.getConnection();

        con.prepareStatement("DROP TABLE user_table").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();
    }

    @Override
    protected AbstractJdbcPersonAttributeDao<Map<String, Object>> newDao(DataSource dataSource) {
        return new SingleRowJdbcPersonAttributeDao(dataSource, "SELECT netid, name, email, shirt_color FROM user_table WHERE {0}");
    }

    @Override
    protected boolean supportsPerDataAttributeCaseSensitivity() {
        return true;
    }

    @Override
    protected void beforeNonUsernameQuery(AbstractJdbcPersonAttributeDao<Map<String, Object>> dao) {
        // no-op
    }
    
    public void testNoQueryAttributeMapping() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE netid = 'awp9'");
        impl.setUseAllQueryAttributes(false);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
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
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        Set<String> expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("firstName");
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("emailAddress");
        expectedAttributeNames.add("dressShirtColor");

        Set<String> attributeNames = impl.getPossibleUserAttributeNames();
        assertEquals(attributeNames, expectedAttributeNames);
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSingleAttrQuery() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
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
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE netid = {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", null));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        columnsToAttributes.put("email", "emailAddress");
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }
   
   /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeMapping() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", null);
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("awp9");
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("blue"), attribs.get("shirt_color"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeName() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("", "dressShirtColor");

        try {
            impl.setResultAttributeMapping(columnsToAttributes);
            fail("IllegalArgumentException if the ColumnsToAttributes Map has an empty Key");
        }
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
   
   /**
     * Test for a query with a null value attribute
     */
    public void testNullAttrQuery() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("susan");
        assertNull(attribs.get("dressShirtColor"));
        assertEquals(Util.list("Susan"), attribs.get("firstName"));
    }
   
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        
        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("shirtColor", Util.list("blue"));
        queryMap.put("Name", Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
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
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);
        impl.setRequireAllQueryAttributes(true);
        
        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("Name", Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test for a query with a single attribute
     */
    public void testMultiPersonQuery() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));
        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);

        impl.setResultAttributeMapping(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put("shirt", Util.list("blue"));

        try {
            impl.getMultivaluedUserAttributes(queryMap);
        }
        catch (IncorrectResultSizeDataAccessException irsdae) {
            // good, exception thrown for multiple results
            return;
        }

        fail("JdbcPersonAttributeDao should have thrown IncorrectResultSizeDataAccessException for multiple results");
    }
    
    public void testProperties() {
        SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));
        assertEquals(Collections.singletonMap("shirt", Collections.singleton("shirt_color")), impl.getQueryAttributeMapping());
        
        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        Map<String, Set<String>> expectedColumnsToAttributes = new HashMap<String, Set<String>>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        impl.setResultAttributeMapping(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "shirt";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        
        // shirt_color = ?
        
        final SingleRowJdbcPersonAttributeDao impl = new SingleRowJdbcPersonAttributeDao(this.testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));

        return impl;
    }

}
