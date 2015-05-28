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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeScriptDao;
import org.jasig.services.persondir.IPersonAttributes;

/**
 * An implementation of the {@link org.jasig.services.persondir.IPersonAttributeDao} that is able to resolve attributes
 * based on an external Groovy script, Groovy object, or Java object. Changes to the groovy script can be auto-detected
 * in certain use cases.
 * <p/>
 * There are several ways to use this Dao.
 * <p/>
 * Approach 1: Groovy file pre-compiled to Java class file
 * <p/>
 * <pre><code>
Spring configuration:

<bean id="duplicateUsernameAttributeScript" class="org.jasig.portal.persondir.AttributeDuplicatingPersonAttributesScript"/>
<bean id="duplicateUsernameAttributeSource" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"
      c:groovyObject-ref="duplicateUsernameAttributeScript"/>

Groovy file:

class SampleGroovyPersonAttributeDao implements org.jasig.services.persondir.IPersonAttributeScriptDao {

    @Override
    Map<String, Object> getAttributesForUser(String uid, Log log) {
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }

    @Override
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes, Log log) {
        Map<String, List<Object>> newMap = new HashMap<>(attributes)
        newMap.put("foo", Arrays.asList(["value1", "value2"]))
        return newMap
    }

}
 * </code></pre>
 * Notes:<ol>
 * <li>Use maven-antrun-plugin, gmavenplus-plugin, or similar to pre-compile groovy classes in maven build process</li>
 * <li>Separate groovy source file, so can create unit test of groovy code</li>
 * <li>Does not accommodate groovy source code changes</li>
 * </ol>
 * <p/>
 * Approach 2: Groovy script file referenced by change-detecting configuration
 * <p/>
 * <pre><code>
Spring configuration:

<bean id="duplicateUsernameAttributeSource2" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"/>
    c:groovyObject-ref="duplicateUsernameAttributeScript2"/>

<lang:groovy id="duplicateUsernameAttributeScript2" refresh-check-delay="5000"
    script-source="classpath:AttributeDuplicatingPersonAttributesScript.groovy"/>

Groovy file:

Same as Approach 1

 * </code></pre>
 * Notes:<ol>
 * <li>Separate groovy source file, so can create unit test of groovy code</li>
 * <li>Will detect groovy source code changes</li>
 * </ol>
 * <p/>
 * Approach 3: Inline Groovy script
 * <p/>
 * <pre><code>
Spring configuration:

<bean id="duplicateUsernameAttributeSource3" class="org.jasig.services.persondir.support.GroovyPersonAttributeDao"
    c:groovyObject-ref="duplicateUsernameAttributeScript3"/>

<lang:groovy id="duplicateUsernameAttributeScript3">
    <lang:inline-script><![CDATA[
        class AttributeDuplicatingPersonAttributesScript extends org.jasig.services.persondir.support.BaseGroovyScriptDaoImpl {

        @Override
        Map<String, Object> getAttributesForUser(String uid, Log log) {
            return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
        }
    ]]></lang:inline-script>
</lang:groovy>

 * </code></pre>
 * Notes:<ol>
 * <li>Cannot create unit test of groovy source file, will not detect changes</li>
 * <li>Useful for embedded configuration</li>
 * </ol>
 * @author Misagh Moayyed
 * @author James Wennmacher
 * @since 1.6.0
 */
public class GroovyPersonAttributeDao extends BasePersonAttributeDao {

    private IPersonAttributeScriptDao groovyObject;
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
    @SuppressWarnings("unchecked")
    public IPersonAttributes getPerson(final String uid) {
        try {
            logger.debug("Executing groovy script's getAttributesForUser method");

            final Map<String, Object> personAttributesMap = groovyObject.getAttributesForUser(uid);
            logger.debug("Creating person attributes with the username " + uid + " and attributes " +
                    personAttributesMap);

            final Map<String, List<Object>> personAttributes = stuffAttributesIntoListValues(personAttributesMap);

            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, personAttributes);
            }
            return new NamedPersonImpl(uid, personAttributes);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, Object> personAttributesMap) {
        final Map<String, List<Object>> personAttributes = new HashMap<>();

        for (final String key : personAttributesMap.keySet()) {
            final Object value = personAttributesMap.get(key);
            if (value instanceof List) {
                personAttributes.put(key, (List) value);
            } else {
                personAttributes.put(key, Arrays.asList(value));
            }
        }
        return personAttributes;
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> attributes) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoListValues(attributes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> attributes) {
        try {
            logger.debug("Executing groovy script's getPersonAttributesFromMultivaluedAttributes method, with parameters "
                    + attributes);

            @SuppressWarnings("unchecked")
            final Map<String, List<Object>> personAttributesMap =
                    groovyObject.getPersonAttributesFromMultivaluedAttributes(attributes);

            logger.debug("Creating person attributes: " + personAttributesMap);

            return Collections.singleton((IPersonAttributes) new AttributeNamedPersonImpl(personAttributesMap));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void setPossibleUserAttributeNames(final Set<String> possibleUserAttributeNames) {
        this.possibleUserAttributeNames = possibleUserAttributeNames;
    }

    public void setAvailableQueryAttributes(final Set<String> availableQueryAttributes) {
        this.availableQueryAttributes = availableQueryAttributes;
    }

    public Set<String> getAvailableQueryAttributes() {
        return availableQueryAttributes;
    }

    public Set<String> getPossibleUserAttributeNames() {
        return possibleUserAttributeNames;
    }
}
