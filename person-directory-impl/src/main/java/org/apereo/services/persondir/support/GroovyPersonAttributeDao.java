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

import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributeScriptDao;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of the {@link IPersonAttributeDao} that is able to resolve attributes
 * based on an external Groovy script, Groovy object, or Java object. Changes to the groovy script can be auto-detected
 * in certain use cases.
 * <br><br>
 * There are several ways to use this Dao.
 * <br><br>
 * Approach 1: Groovy file pre-compiled to Java class file
 * <br><br>
 * <pre>
 Spring configuration:

 &lt;bean id="duplicateUsernameAttributeScript" class="org.jasig.portal.persondir.AttributeDuplicatingPersonAttributesScript"/&gt;
 &lt;bean id="duplicateUsernameAttributeSource" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"
 c:groovyObject-ref="duplicateUsernameAttributeScript"/&gt;

 Groovy file:

 class SampleGroovyPersonAttributeDao implements org.jasig.services.persondir.IPersonAttributeScriptDao {

 {@literal @}Override
 Map&lt;String, Object&gt; getAttributesForUser(String uid, Log log) {
 return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
 }

 {@literal @}Override
 Map&lt;String, List&lt;Object&gt;&gt; getPersonAttributesFromMultivaluedAttributes(Map&lt;String, List&lt;Object&gt;&gt; attributes, Log log) {
 Map&lt;String, List&lt;Object&gt;&gt; newMap = new HashMap&lt;&gt;(attributes)
 newMap.put("foo", Arrays.asList(["value1", "value2"]))
 return newMap
 }

 }
 * </pre>
 * Notes:<ol>
 * <li>Use maven-antrun-plugin, gmavenplus-plugin, or similar to pre-compile groovy classes in maven build process</li>
 * <li>Separate groovy source file, so can create unit test of groovy code</li>
 * <li>Does not accommodate groovy source code changes</li>
 * </ol>
 * <br><br>
 * Approach 2: Groovy script file referenced by change-detecting configuration
 * <br><br>
 * <pre>
 Spring configuration:

 &lt;bean id="duplicateUsernameAttributeSource2" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"/&gt;
 c:groovyObject-ref="duplicateUsernameAttributeScript2"/&gt;

 &lt;lang:groovy id="duplicateUsernameAttributeScript2" refresh-check-delay="5000"
 script-source="classpath:AttributeDuplicatingPersonAttributesScript.groovy"/&gt;

 Groovy file:

 Same as Approach 1

 * </pre>
 * Notes:<ol>
 * <li>Separate groovy source file, so can create unit test of groovy code</li>
 * <li>Will detect groovy source code changes</li>
 * </ol>
 * <br><br>
 * Approach 3: Inline Groovy script
 * <br><br>
 * <pre>
 Spring configuration:

 &lt;bean id="duplicateUsernameAttributeSource3" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"
 c:groovyObject-ref="duplicateUsernameAttributeScript3"/&gt;

 &lt;lang:groovy id="duplicateUsernameAttributeScript3"&gt;
 &lt;lang:inline-script&gt;&lt;![CDATA[
 class AttributeDuplicatingPersonAttributesScript extends org.jasig.services.persondir.support.BaseGroovyScriptDaoImpl {

 {@literal @}Override
 Map&lt;String, Object&gt; getAttributesForUser(String uid, Log log) {
 return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
 }
 ]]&gt;&lt;/lang:inline-script&gt;
 &lt;/lang:groovy&gt;

 * </pre>
 * Notes:<ol>
 * <li>Cannot create unit test of groovy source file, will not detect changes</li>
 * <li>Useful for embedded configuration</li>
 * </ol>
 * @author Misagh Moayyed
 * @author James Wennmacher
 * @since 1.6.0
 */
public class GroovyPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    private final IPersonAttributeScriptDao groovyObject;
    private Set<String> possibleUserAttributeNames = null;
    private Set<String> availableQueryAttributes = null;

    private boolean caseInsensitiveUsername = false;

    public GroovyPersonAttributeDao(final IPersonAttributeScriptDao groovyObject) {
        this.groovyObject = groovyObject;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final Set<IPersonAttributes> resultPeople, final IPersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }

        logger.debug("Executing groovy script's getAttributesForUser method");
        var personAttributesMap = groovyObject.getAttributesForUser(Objects.requireNonNull(uid));
        if (personAttributesMap != null) {
            logger.debug("Creating person attributes with the username {} and attributes {}", uid, personAttributesMap);
            var personAttributes = MultivaluedPersonAttributeUtils.toMultivaluedMap(personAttributesMap);

            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, personAttributes);
            }
            return new NamedPersonImpl(uid, personAttributes);
        }
        logger.debug("Groovy script returned null for uid={}", uid);
        return null;
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> attributes,
                                                                     final IPersonAttributeDaoFilter filter,
                                                                     final Set<IPersonAttributes> resultPeople) {
        logger.debug("Executing groovy script's getPersonAttributesFromMultivaluedAttributes method, with parameters {}", attributes);
        var personAttributesMap = groovyObject.getPersonAttributesFromMultivaluedAttributes(attributes, resultPeople);

        if (personAttributesMap != null) {
            logger.debug("Creating person attributes: {}", personAttributesMap);
            return Collections.singleton(new AttributeNamedPersonImpl(personAttributesMap));
        }
        return null;
    }

    public void setPossibleUserAttributeNames(final Set<String> possibleUserAttributeNames) {
        this.possibleUserAttributeNames = possibleUserAttributeNames;
    }

    public void setAvailableQueryAttributes(final Set<String> availableQueryAttributes) {
        this.availableQueryAttributes = availableQueryAttributes;
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return availableQueryAttributes;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return possibleUserAttributeNames;
    }
}
