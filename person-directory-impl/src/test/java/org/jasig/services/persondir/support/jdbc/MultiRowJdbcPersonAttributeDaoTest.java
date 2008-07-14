/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.hsqldb.jdbcDriver;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.jasig.services.persondir.util.Util;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Test the {@link MultiRowJdbcPersonAttributeDao} against a dummy DataSource.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class MultiRowJdbcPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {
    
    private DataSource testDataSource;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.testDataSource = new SimpleDriverDataSource(new jdbcDriver(), "jdbc:hsqldb:mem:adhommemds", "sa", "");

        
        Connection con = testDataSource.getConnection();
        
        con.prepareStatement("CREATE TABLE user_table " +
                             "(netid VARCHAR, " +
                             "attr_name VARCHAR, " +
                             "attr_val VARCHAR)").execute();

        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('awp9', 'name', 'Andrew')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('awp9', 'email', 'andrew.petro@yale.edu')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('awp9', 'shirt_color', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('edalquist', 'name', 'Eric')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('edalquist', 'email', 'edalquist@unicon.net')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('edalquist', 'shirt_color', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('atest', 'name', 'Andrew')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('atest', 'email', 'andrew.test@test.net')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('atest', 'shirt_color', 'red')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('susan', 'name', 'Susan')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('susan', 'email', 'susan.test@test.net')").execute();
        con.prepareStatement("INSERT INTO user_table " +
                             "(netid, attr_name, attr_val) " +
                             "VALUES ('susan', 'shirt_color', null)").execute();
        
        con.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        Connection con = this.testDataSource.getConnection();
        
        con.prepareStatement("DROP TABLE user_table").execute();
//        con.prepareStatement("SHUTDOWN").execute();

        con.close();
        
        this.testDataSource = null;
    }

    //TODO this is no longer a FAILURE
//    public void testNullAttributeList() {
//        try {
//            new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT attr_name, attr_val FROM user_table WHERE {0}");
//            fail("IllegalArgumentException should have been thrown");
//        }
//        catch (IllegalArgumentException iae) {
//            //expected
//        }
//    }
//    

   /**
    * Test that the implementation properly reports the attribute names it
    * expects to map.
    */
   public void testPossibleUserAttributeNames() {
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));
       
       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       Set<String> emailAttributeNames = new LinkedHashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setResultAttributeMapping(columnsToAttributes);

       Set<String> expectedAttributeNames = new LinkedHashSet<String>();
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
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

       impl.setDefaultAttributeName("uid");
       impl.setUserNameAttribute("netid");
       
       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");
       Set<String> emailAttributeNames = new LinkedHashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setResultAttributeMapping(columnsToAttributes);
       
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));
       

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
   public void testInvalidColumnName() {
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

       impl.setDefaultAttributeName("uid");
       impl.setUserNameAttribute("netid");

       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       columnsToAttributes.put("email", "emailAddress");
       impl.setResultAttributeMapping(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_nam", "attr_val"));

       try {
           impl.getMultivaluedUserAttributes("awp9");
           fail("BadSqlGrammarException expected with invalid attribute mapping key");
       }
       catch (BadSqlGrammarException bsge) {
           //expected
       }
       
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_va"));

       try {
           impl.getMultivaluedUserAttributes("awp9");
           fail("BadSqlGrammarException expected with invalid attribute mapping key");
       }
       catch (BadSqlGrammarException bsge) {
           //expected
       }
   }
   
   /**
    * Test for a query with a single attribute
    */
   public void testSetNullAttributeMapping() {
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

       impl.setDefaultAttributeName("uid");
       impl.setUserNameAttribute("netid");
       
       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       Set<String> emailAttributeNames = new LinkedHashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", null);
       impl.setResultAttributeMapping(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

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
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

       impl.setDefaultAttributeName("uid");
       impl.setUserNameAttribute("netid");

       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
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
       MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
       impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

       impl.setDefaultAttributeName("uid");
       impl.setUserNameAttribute("netid");
       
       Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setResultAttributeMapping(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

       Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("susan");
       assertEquals(Collections.singletonList(null), attribs.get("dressShirtColor"));
       assertEquals(Util.list("Susan"), attribs.get("firstName"));
   }
   
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setDefaultAttributeName("uid");
        impl.setUserNameAttribute("netid");
        
        Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("shirt_color", "color");
        impl.setResultAttributeMapping(columnsToAttributes);
        
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        Map<String, List<Object>> queryMap = new LinkedHashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("shirtColor", Util.list("blue"));
        queryMap.put("Name",Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertEquals(Util.list("blue"), attribs.get("color"));
    }
    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setDefaultAttributeName("uid");
        impl.setUserNameAttribute("netid");
        impl.setRequireAllQueryAttributes(true);

        Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);
        
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        Map<String, List<Object>> queryMap = new LinkedHashMap<String, List<Object>>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("Name", Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    public void testProperties() {
        MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "netid"));

        impl.setDefaultAttributeName("shirt");
        impl.setUserNameAttribute("netid");
        
        
        Map<String, Object> columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");
        
        Map<String, Object> expectedColumnsToAttributes = new LinkedHashMap<String, Object>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        assertNull(impl.getResultAttributeMapping());
        impl.setResultAttributeMapping(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());
        
        
        assertEquals(null, impl.getNameValueColumnMappings());
        impl.setNameValueColumnMappings(null);
        assertEquals(null, impl.getNameValueColumnMappings());
        try {
            impl.setNameValueColumnMappings(Collections.singletonMap("NullValueKey", null));
            fail("setNameValueColumnMappings(Collections.singletonMap(\"NullValueKey\", null)) should result in an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));
        assertEquals(Collections.singletonMap("attr_name", Collections.singleton("attr_val")), impl.getNameValueColumnMappings());
        
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(this.testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));

        return impl;
    }

}
