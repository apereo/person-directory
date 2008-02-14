/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.jdbc;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.rdbm.TransientDatasource;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.jasig.services.persondir.util.Util;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * Test the {@link SingleRowJdbcPersonAttributeDao} against a dummy DataSource.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class SingleRowJdbcPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {
    
    private DataSource testDataSource;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        this.testDataSource = new TransientDatasource();
        Connection con = testDataSource.getConnection();
        
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
    protected void tearDown() throws Exception {
        super.tearDown();
        
        Connection con = this.testDataSource.getConnection();
        
        con.prepareStatement("DROP TABLE user_table").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();
        
        this.testDataSource = null;
    }
    
    public void testNullAttributeList() {
        try {
            new SingleRowJdbcPersonAttributeDao(testDataSource, null, "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");
            fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
    

   /**
    * Test that the implementation properly reports the attribute names it
    * expects to map.
    */
   public void testPossibleUserAttributeNames() {
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");
       
       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       Set<String> emailAttributeNames = new HashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

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
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       Set<String> emailAttributeNames = new HashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

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
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       columnsToAttributes.put("emai", "emailAddress");
       impl.setColumnsToAttributes(columnsToAttributes);

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
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");

       Set<String> emailAttributeNames = new HashSet<String>();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", null);
       impl.setColumnsToAttributes(columnsToAttributes);

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
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("", "dressShirtColor");
       
       try {
           impl.setColumnsToAttributes(columnsToAttributes);
           fail("IllegalArgumentException if the ColumnsToAttributes Map has an empty Key");
       }
       catch (IllegalArgumentException iae) {
           //expected
       }
   }
   
   /**
    * Test for a query with a single attribute
    */
   public void testSetEmptyAttributeMapping() {
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       try {
           impl.setColumnsToAttributes(null);
           fail("setColumnsToAttributes(null) should throw IllegalArgumentException");
       }
       catch (IllegalArgumentException iae) {
           //Expected;
       }
   }
   
   
   /**
    * Test for a query with a null value attribute
    */
   public void testNullAttrQuery() {
       final String queryAttr = "uid";
       final List<String> queryAttrList = new LinkedList<String>();
       queryAttrList.add(queryAttr);

       SingleRowJdbcPersonAttributeDao impl = 
           new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
       columnsToAttributes.put("name", "firstName");
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

       Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes("susan");
       assertNull(attribs.get("dressShirtColor"));
       assertEquals(Util.list("Susan"), attribs.get("firstName"));
   }
   
   
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        SingleRowJdbcPersonAttributeDao impl = 
            new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT name, email FROM user_table WHERE netid = ? AND shirt_color = ?");

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setColumnsToAttributes(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr1, Util.list("awp9"));
        queryMap.put(queryAttr2, Util.list("blue"));
        queryMap.put("Name", Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        SingleRowJdbcPersonAttributeDao impl = 
            new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT name, email FROM user_table WHERE netid = ? AND shirt_color = ?");

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setColumnsToAttributes(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr1, Util.list("awp9"));
        queryMap.put("Name", Util.list("John"));

        Map<String, List<Object>> attribs = impl.getMultivaluedUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test for a query with a single attribute
     */
    public void testMultiPersonQuery() {
        final String queryAttr = "shirt";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);

        SingleRowJdbcPersonAttributeDao impl = 
            new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");

        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        Set<String> emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        
        impl.setColumnsToAttributes(columnsToAttributes);

        Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
        queryMap.put(queryAttr, Util.list("blue"));
        
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
        final String queryAttr = "shirt";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);

        SingleRowJdbcPersonAttributeDao impl = 
            new SingleRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");
        
        Map<String, Object> columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");
        
        Map<String, Set<String>> expectedColumnsToAttributes = new HashMap<String, Set<String>>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        impl.setColumnsToAttributes(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getColumnsToAttributes());
    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "shirt";
        final List<String> queryAttrList = new LinkedList<String>();
        queryAttrList.add(queryAttr);
        SingleRowJdbcPersonAttributeDao impl = 
            new SingleRowJdbcPersonAttributeDao(this.testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");

        return impl;
    }

}
