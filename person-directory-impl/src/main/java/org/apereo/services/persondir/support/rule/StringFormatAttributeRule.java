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
package org.apereo.services.persondir.support.rule;

import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a new user attribute by passing a formatString and the values of 
 * existing attributes to <code>String.format()</code>
 *
 * @author awills
 */
public final class StringFormatAttributeRule implements AttributeRule, InitializingBean {

    private String formatString;
    private List<String> formatArguments;
    private String outputAttribute;
    private IUsernameAttributeProvider usernameAttributeProvider;

    public void setFormatString(final String formatString) {
        this.formatString = formatString;
    }

    public void setFormatArguments(final List<String> formatArguments) {
        this.formatArguments = formatArguments;
    }

    public void setOutputAttribute(final String outputAttribute) {
        this.outputAttribute = outputAttribute;
    }

    public void setUsernameAttributeProvider(final IUsernameAttributeProvider usernameAttributeProvider) {
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    @Override
    public boolean appliesTo(final Map<String, List<Object>> userInfo) {

        // Assertions.
        if (userInfo == null) {
            final var msg = "Argument 'userInfo' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        var rslt = true;  // default
        for (final var attributeName : formatArguments) {
            if (!userInfo.containsKey(attributeName)) {
                rslt = false;
                break;
            }
        }

        return rslt;

    }

    @Override
    public Set<IPersonAttributes> evaluate(final Map<String, List<Object>> userInfo) {

        // Assertions.
        if (userInfo == null) {
            final var msg = "Argument 'userInfo' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!appliesTo(userInfo)) {
            final var msg = "May not evaluate.  This rule does not apply.";
            throw new IllegalArgumentException(msg);
        }

        final var args = new Object[formatArguments.size()];
        for (var i = 0; i < formatArguments.size(); i++) {
            final var key = formatArguments.get(i);
            final var values = userInfo.get(key);
            args[i] = values.isEmpty() ? null : values.get(0);
        }

        final var outputAttributeValue = String.format(this.formatString, args);


        final Map<String, List<Object>> rslt = new HashMap<>();
        rslt.put(this.outputAttribute, Arrays.asList(new Object[]{outputAttributeValue}));

        final var username = this.usernameAttributeProvider.getUsernameFromQuery(userInfo);
        final IPersonAttributes person = new NamedPersonImpl(username, rslt);
        return Collections.singleton(person);

    }

    @Override
    public Set<String> getAvailableQueryAttributes() {
        return new HashSet<>(formatArguments);
    }

    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return Collections.singleton(outputAttribute);
    }

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(formatString)) throw new BeanInitializationException("formatString is required");
        if (usernameAttributeProvider == null) throw new BeanInitializationException("usernameAttributeProvider is required");
        if (outputAttribute == null) throw new BeanInitializationException("outputAttribute is required");
    }
}
