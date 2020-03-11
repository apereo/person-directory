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
package org.apereo.services.persondir.support.jdbc;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.apereo.services.persondir.util.Util;
import org.springframework.jdbc.BadSqlGrammarException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test the {@link MultiRowJdbcPersonAttributeDao} against a dummy DataSource.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist

 */
public class MultiRowJdbcPersonAttributeDaoTest
        extends AbstractCaseSensitivityJdbcPersonAttributeDaoTest {

    @Override
    protected void setUpSchema(final DataSource dataSource) throws SQLException {
        final Connection con = dataSource.getConnection();

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
    protected void tearDownSchema(final DataSource dataSource) throws SQLException {
        final Connection con = dataSource.getConnection();

        con.prepareStatement("DROP TABLE user_table").execute();

        con.close();
    }

    @Override
    protected AbstractJdbcPersonAttributeDao<Map<String, Object>> newDao(final DataSource dataSource) {
        final MultiRowJdbcPersonAttributeDao dao = new MultiRowJdbcPersonAttributeDao(dataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        dao.setNameValueColumnMappings(ImmutableMap.of("attr_name", "attr_val"));
        return dao;
    }

    @Override
    protected boolean supportsPerDataAttributeCaseSensitivity() {
        return false;
    }

    @Override
    protected void beforeNonUsernameQuery(final AbstractJdbcPersonAttributeDao<Map<String, Object>> dao) {

        // no processing method for caseInsensitiveResultAttributeMappings b/c
        // the mapping from physical data layer attrib name (attr_val) to
        // logical data layer attrib name has already occurred by the time
        // the case canonicalization kicks in
        processQueryAttributeMappingValues_BeforeNonUsernameQuery(dao);
        processCaseInsensitiveDataAttributeMappingValues_BeforeNonUsernameQuery(dao);
        dao.setUnmappedUsernameAttribute("netid");
    }

    protected void processQueryAttributeMappingValues_BeforeNonUsernameQuery(final AbstractJdbcPersonAttributeDao<Map<String, Object>> dao) {
        final Map<String, Set<String>> origMappings = dao.getQueryAttributeMapping();
        if (origMappings == null || origMappings.isEmpty()) {
            return;
        }
        final Map<String, Set<String>> newMappings = new LinkedHashMap<>();
        for (final Map.Entry<String, Set<String>> origMapping : origMappings.entrySet()) {
            final Set<String> newMappingValue = new LinkedHashSet<>();
            // multi-row dao maps all non-username attr values to the same
            // data layer column
            for (final String origMappingValue : origMapping.getValue()) {
                if (!("netid".equals(origMappingValue))) {
                    newMappingValue.add("attr_val");
                } else {
                    newMappingValue.add(origMappingValue);
                }
            }
            newMappings.put(origMapping.getKey(), newMappingValue);
        }
        dao.setQueryAttributeMapping(newMappings);
    }

    protected void processCaseInsensitiveDataAttributeMappingValues_BeforeNonUsernameQuery(final AbstractJdbcPersonAttributeDao<Map<String, Object>> dao) {
        final Map<String, CaseCanonicalizationMode> origMappings = dao.getCaseInsensitiveDataAttributes();
        if (origMappings == null || origMappings.isEmpty()) {
            return;
        }
        final Map<String, CaseCanonicalizationMode> newMappings = new LinkedHashMap<>();
        for (final Map.Entry<String, CaseCanonicalizationMode> origMapping : origMappings.entrySet()) {
            // that's right, it's all or nothing for the multi-row DAO w/r/t
            // case sensitivity of non-username attribs b/c the canonicalization
            // is based on data-layer attribute names, which are all the same for
            // this DAO type.
            if (!("netid".equals(origMapping.getKey()))) {
                newMappings.put("attr_val", origMapping.getValue());
            } else {
                newMappings.put(origMapping.getKey(), origMapping.getValue());
            }
        }
        dao.setCaseInsensitiveDataAttributes(newMappings);
    }


    public void testNoQueryAttributeMapping() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE netid = 'awp9'");
        impl.setUseAllQueryAttributes(false);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");
        final Set<String> emailAttributeNames = new LinkedHashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);


        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));


        final Map<String, List<Object>> attribs = impl.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes();
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        TestCase.assertEquals(Util.list("blue"), attribs.get("dressShirtColor"));
        TestCase.assertNull(attribs.get("shirt_color"));
        TestCase.assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test that the implementation properly reports the attribute names it
     * expects to map.
     */
    public void testPossibleUserAttributeNames() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new LinkedHashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        final Set<String> expectedAttributeNames = new LinkedHashSet<>();
        expectedAttributeNames.add("firstName");
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("emailAddress");
        expectedAttributeNames.add("dressShirtColor");

        final Set<String> attributeNames = impl.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
        TestCase.assertEquals(attributeNames, expectedAttributeNames);
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSingleAttrQuery() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");
        final Set<String> emailAttributeNames = new LinkedHashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);


        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));


        final Map<String, List<Object>> attribs = impl.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes();
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        TestCase.assertEquals(Util.list("blue"), attribs.get("dressShirtColor"));
        TestCase.assertNull(attribs.get("shirt_color"));
        TestCase.assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testInvalidColumnName() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");

        columnsToAttributes.put("email", "emailAddress");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Collections.singletonMap("attr_nam", "attr_val"));

        try {
            impl.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose());
            TestCase.fail("BadSqlGrammarException expected with invalid attribute mapping key");
        } catch (final BadSqlGrammarException bsge) {
            //expected
        }


        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_va"));

        try {
            impl.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose());
            TestCase.fail("BadSqlGrammarException expected with invalid attribute mapping key");
        } catch (final BadSqlGrammarException bsge) {
            //expected
        }
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeMapping() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new LinkedHashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", null);
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        final Map<String, List<Object>> attribs = impl.getPerson("awp9", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes();
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("email"));
        TestCase.assertEquals(Util.list("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        TestCase.assertEquals(Util.list("blue"), attribs.get("shirt_color"));
        TestCase.assertEquals(Util.list("Andrew"), attribs.get("firstName"));
    }

    /**
     * Test for a query with a single attribute
     */
    public void testSetNullAttributeName() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("", "dressShirtColor");

        try {
            impl.setResultAttributeMapping(columnsToAttributes);
            TestCase.fail("IllegalArgumentException if the ColumnsToAttributes Map has an empty Key");
        } catch (final IllegalArgumentException iae) {
            //expected
        }
    }

    /**
     * Test for a query with a null value attribute
     */
    public void testNullAttrQuery() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        final Map<String, List<Object>> attribs = impl.getPerson("susan", IPersonAttributeDaoFilter.alwaysChoose()).getAttributes();
        TestCase.assertEquals(Collections.singletonList(null), attribs.get("dressShirtColor"));
        TestCase.assertEquals(Util.list("Susan"), attribs.get("firstName"));
    }

    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("shirt_color", "color");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        final Map<String, List<Object>> queryMap = new LinkedHashMap<>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("shirtColor", Util.list("blue"));
        queryMap.put("Name", Util.list("John"));

        final Set<IPersonAttributes> attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        TestCase.assertEquals(Util.list("blue"), attribsSet.iterator().next().getAttributes().get("color"));
    }

    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final Map<String, String> queryAttributeMapping = new LinkedHashMap<>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");
        impl.setRequireAllQueryAttributes(true);

        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("name", "firstName");

        final Set<String> emailAttributeNames = new LinkedHashSet<>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));

        final Map<String, List<Object>> queryMap = new LinkedHashMap<>();
        queryMap.put("uid", Util.list("awp9"));
        queryMap.put("Name", Util.list("John"));

        Set<IPersonAttributes> attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap, IPersonAttributeDaoFilter.alwaysChoose());
        TestCase.assertNull(attribsSet);
    }

    public void testProperties() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("shirt"));
        impl.setUnmappedUsernameAttribute("netid");


        final Map<String, Object> columnsToAttributes = new LinkedHashMap<>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        final Map<String, Object> expectedColumnsToAttributes = new LinkedHashMap<>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        TestCase.assertNull(impl.getResultAttributeMapping());
        impl.setResultAttributeMapping(columnsToAttributes);
        TestCase.assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());


        TestCase.assertEquals(null, impl.getNameValueColumnMappings());
        impl.setNameValueColumnMappings(null);
        TestCase.assertEquals(null, impl.getNameValueColumnMappings());
        try {
            impl.setNameValueColumnMappings(Collections.singletonMap("NullValueKey", null));
            TestCase.fail("setNameValueColumnMappings(Collections.singletonMap(\"NullValueKey\", null)) should result in an IllegalArgumentException");
        } catch (final IllegalArgumentException iae) {
            //Expected
        }
        impl.setNameValueColumnMappings(Collections.singletonMap("attr_name", "attr_val"));
        TestCase.assertEquals(Collections.singletonMap("attr_name", Collections.singleton("attr_val")), impl.getNameValueColumnMappings());

    }

    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        final MultiRowJdbcPersonAttributeDao impl = new MultiRowJdbcPersonAttributeDao(this.testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Collections.singletonMap("shirt", "shirt_color"));

        return impl;
    }

}
