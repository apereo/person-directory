/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.jdbc;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.portal.rdbm.TransientDatasource;
import org.jasig.portal.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.portal.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * Test the {@link MultiRowJdbcPersonAttributeDao} against a dummy DataSource.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class MultiRowJdbcPersonAttributeDaoTest 
    extends AbstractDefaultQueryPersonAttributeDaoTest {
    
    private DataSource testDataSource;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        this.testDataSource = new TransientDatasource();
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
            new MultiRowJdbcPersonAttributeDao(testDataSource, null, "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");
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
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");

       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setAttributeNameMappings(columnsToAttributes);

       Set expectedAttributeNames = new HashSet();
       expectedAttributeNames.add("firstName");
       expectedAttributeNames.add("email");
       expectedAttributeNames.add("emailAddress");
       expectedAttributeNames.add("dressShirtColor");
       
       Set attributeNames = impl.getPossibleUserAttributeNames();
       assertEquals(attributeNames, expectedAttributeNames);
   }

   /**
    * Test for a query with a single attribute
    */
   public void testSingleAttrQuery() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");
       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setAttributeNameMappings(columnsToAttributes);
       
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));
       
       
       
       

       Map attribs = impl.getUserAttributes("awp9");
       assertEquals("andrew.petro@yale.edu", attribs.get("email"));
       assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
       assertEquals("blue", attribs.get("dressShirtColor"));
       assertNull(attribs.get("shirt_color"));
       assertEquals("Andrew", attribs.get("firstName"));
   }

   /**
    * Test for a query with a single attribute
    */
   public void testInvalidColumnName() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");

       columnsToAttributes.put("email", "emailAddress");
       impl.setAttributeNameMappings(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_nam", "attr_val"));

       try {
           impl.getUserAttributes("awp9");
           fail("BadSqlGrammarException expected with invalid attribute mapping key");
       }
       catch (BadSqlGrammarException bsge) {
           //expected
       }
       
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_va"));

       try {
           impl.getUserAttributes("awp9");
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
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");

       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", null);
       impl.setAttributeNameMappings(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

       Map attribs = impl.getUserAttributes("awp9");
       assertEquals("andrew.petro@yale.edu", attribs.get("email"));
       assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
       assertEquals("blue", attribs.get("shirt_color"));
       assertEquals("Andrew", attribs.get("firstName"));
   }

   /**
    * Test for a query with a single attribute
    */
   public void testSetNullAttributeName() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("", "dressShirtColor");
       
       try {
           impl.setAttributeNameMappings(columnsToAttributes);
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
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       try {
           impl.setAttributeNameMappings(null);
           fail("setAttributeNameMappings(null) should throw IllegalArgumentException");
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
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       MultiRowJdbcPersonAttributeDao impl = 
           new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
               "SELECT attr_name, attr_val FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setAttributeNameMappings(columnsToAttributes);
       
       impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

       Map attribs = impl.getUserAttributes("susan");
       assertNull(attribs.get("dressShirtColor"));
       assertEquals("Susan", attribs.get("firstName"));
   }
   
   
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        MultiRowJdbcPersonAttributeDao impl = 
            new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT attr_name, attr_val FROM user_table WHERE netid = ? AND attr_val = ?");

        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("shirt_color", "color");
        impl.setAttributeNameMappings(columnsToAttributes);
        
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "awp9");
        queryMap.put(queryAttr2, "blue");
        queryMap.put("Name", "John");

        Map attribs = impl.getUserAttributes(queryMap);
        assertEquals("blue", attribs.get("color"));
    }

    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        MultiRowJdbcPersonAttributeDao impl = 
            new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT name, email FROM user_table WHERE netid = ? AND shirt_color = ?");

        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("name", "firstName");

        Set emailAttributeNames = new HashSet();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setAttributeNameMappings(columnsToAttributes);
        
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "awp9");
        queryMap.put("Name", "John");

        Map attribs = impl.getUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    public void testProperties() {
        final String queryAttr = "shirt";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);

        MultiRowJdbcPersonAttributeDao impl = 
            new MultiRowJdbcPersonAttributeDao(testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");
        
        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");
        
        Map expectedColumnsToAttributes = new HashMap();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        assertEquals(Collections.EMPTY_MAP, impl.getAttributeNameMappings());
        impl.setAttributeNameMappings(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getAttributeNameMappings());
        
        
        assertEquals(Collections.EMPTY_MAP, impl.getNameValueColumnMappings());
        try {
            impl.setNameValueColumnMappings(null);
            fail("setNameValueColumnMappings(null) should result in an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //Expected
        }
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

    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final String queryAttr = "shirt";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        MultiRowJdbcPersonAttributeDao impl = 
            new MultiRowJdbcPersonAttributeDao(this.testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");

        return impl;
    }

}
