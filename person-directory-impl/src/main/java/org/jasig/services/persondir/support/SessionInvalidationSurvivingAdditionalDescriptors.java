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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;

/**
 * {@link IAdditionalDescriptors} implementation that can survive a request where the HTTP session is invalidated.  
 * This class is meant to be used in place of simply using a session-scoped {@link IAdditionalDescriptors} bean.  The 
 * target use case is a login process, during which the user's existing session (if any) is invalidated and a new 
 * session is created.  Any session scoped {@link IAdditionalDescriptors} would be lost during this flow.  To 
 * address this scenario, this class uses the combination of a session scoped {@link IAdditionalDescriptors} bean and 
 * a request scoped {@link IAdditionalDescriptors} bean.  When attributes are not available from the session scoped 
 * bean they should still be available from the request scoped bean.
 * 
 * This class delegates to both the session and the request beans for all methods that add data (setters and 'add' 
 * methods).  This is done to keep the beans in synch.  Calls to the getters, however, are delegated to the session 
 * bean first.  If no data is found in the session bean (possibly because the session has been invalidated and the 
 * previous session bean has been replaced with a new bean that has no data), then the call is delegated to the 
 * request bean.  For the {@link #getAttributes()} method and all other methods that return a collection of values, 
 * the union of results from both the session and the request is returned.  This is done because at certain times the 
 * session bean may be empty or the request may be empty but one or the other should at all times contain the 
 * necessary attributes.  We do not simply check that the session bean has no attributes and then defer to the request 
 * bean because it is possible that attributes get added during the login process after session invalidation, and this 
 * leaves the session bean with an incomplete set of attributes.
 * 
 * Note that this class does not validate that the delegate {@link IAdditionalDescriptors} objects provided have 
 * the expected scope (session or request).  Therefore, it is important to properly configure this class.
 * 
 * See Example Spring Framework configuration below:
 * 
 * <bean id="requestAdditionalDescriptors"
 *   class="org.jasig.services.persondir.support.SessionInvalidationSurvivingAdditionalDescriptors"
 *   init-method="verifyInitialized">
 *     <property name="sessionScopedAdditionalDescriptors" ref="sessionScopedAdditionalDescriptors" />
 *     <property name="requestScopedAdditionalDescriptors" ref="requestScopedAdditionalDescriptors"/>
 * </bean>
 * 
 *  <bean id="sessionScopedAdditionalDescriptors" 
 *  class="org.jasig.services.persondir.support.AdditionalDescriptors" scope="globalSession">
 *    <aop:scoped-proxy />
 *  </bean>
 *  <bean id="requestScopedAdditionalDescriptors" 
 *  class="org.jasig.services.persondir.support.AdditionalDescriptors" scope="request">
 *    <aop:scoped-proxy />
 *  </bean>
 */
public class SessionInvalidationSurvivingAdditionalDescriptors implements IAdditionalDescriptors {

    private static final long serialVersionUID = 1L;

    private IAdditionalDescriptors sessionScopedAdditionalDescriptors;
    private IAdditionalDescriptors requestScopedAdditionalDescriptors;

    public void setSessionScopedAdditionalDescriptors(final IAdditionalDescriptors additionalDescriptors) {
        Validate.notNull(additionalDescriptors, "additionalDescriptors cannot be null");
        this.sessionScopedAdditionalDescriptors = additionalDescriptors;
    }

    public void setRequestScopedAdditionalDescriptors(final IAdditionalDescriptors additionalDescriptors) {
        Validate.notNull(additionalDescriptors, "descriptor cannot be null");
        this.requestScopedAdditionalDescriptors = additionalDescriptors;
    }

    public void verifyInitialized() {
        if (this.sessionScopedAdditionalDescriptors == null) {
            throw new IllegalStateException("sessionScopedAdditionalDescriptors must be set");
        }
        if (this.requestScopedAdditionalDescriptors == null) {
            throw new IllegalStateException("requestScopedAdditionalDescriptors must be set");
        }
    }

    /**
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#addAttributes(java.util.Map)
     */
    public void addAttributes(final Map<String, List<Object>> attributes) {
        this.sessionScopedAdditionalDescriptors.addAttributes(attributes);
        this.requestScopedAdditionalDescriptors.addAttributes(attributes);
    }

    /**
     * Returns list of all removed values.  If none are removed, returns empty list.
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#removeAttribute(java.lang.String)
     */
    public List<Object> removeAttribute(final String name) {
        final List<Object> sessionRemovedValues = this.sessionScopedAdditionalDescriptors.removeAttribute(name);
        final List<Object> requestRemovedValues = this.requestScopedAdditionalDescriptors.removeAttribute(name);
        final Set<Object> removedValues = new HashSet<>();
        if (sessionRemovedValues != null) {
            removedValues.addAll(sessionRemovedValues);
        }
        if (requestRemovedValues != null) {
            removedValues.addAll(requestRemovedValues);
        }
        return new ArrayList<Object>(removedValues);
    }

    /**
     * Returns list of all values set.  If none are set, returns empty list.
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setAttributeValues(java.lang.String, java.util.List)
     */
    public List<Object> setAttributeValues(final String name, final List<Object> values) {
        final List<Object> sessionPreviousValues = this.sessionScopedAdditionalDescriptors.removeAttribute(name);
        final List<Object> requestPreviousValues = this.requestScopedAdditionalDescriptors.removeAttribute(name);
        final Set<Object> previousValues = new HashSet<>();
        if (sessionPreviousValues != null) {
            previousValues.addAll(sessionPreviousValues);
        }
        if (requestPreviousValues != null) {
            previousValues.addAll(requestPreviousValues);
        }
        return new ArrayList<Object>(previousValues);
    }

    /**
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setAttributes(java.util.Map)
     */
    public void setAttributes(final Map<String, List<Object>> attributes) {
        this.sessionScopedAdditionalDescriptors.setAttributes(attributes);
        this.requestScopedAdditionalDescriptors.setAttributes(attributes);
    }

    /**
     * @see org.jasig.services.persondir.support.IAdditionalDescriptors#setName(java.lang.String)
     */
    public void setName(final String name) {
        this.sessionScopedAdditionalDescriptors.setName(name);
        this.requestScopedAdditionalDescriptors.setName(name);
    }

    /**
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(final String name) {
        final Object sessionResult = this.sessionScopedAdditionalDescriptors.getAttributeValue(name);
        return sessionResult == null ? this.requestScopedAdditionalDescriptors.getAttributeValue(name) : sessionResult;
    }

    /**
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributeValues(java.lang.String)
     */
    public List<Object> getAttributeValues(final String name) {
        return this.getUnionOfSessionAndRequestAttributeValues(name);
    }

    /**
     * @see org.jasig.services.persondir.IPersonAttributes#getAttributes()
     */
    public Map<String, List<Object>> getAttributes() {
        return this.getUnionOfSessionAndRequestAttributes();
    }

    /**
     * @see java.security.Principal#getName()
     */
    public String getName() {
        final String sessionDescriptorsName = this.sessionScopedAdditionalDescriptors.getName();
        return sessionDescriptorsName == null ? 
                this.requestScopedAdditionalDescriptors.getName() : sessionDescriptorsName;
    }

    private List<Object> getUnionOfSessionAndRequestAttributeValues(final String name) {
        final List<Object> sessionResults = this.sessionScopedAdditionalDescriptors.getAttributeValues(name);
        final List<Object> requestResults = this.requestScopedAdditionalDescriptors.getAttributeValues(name);
        if (sessionResults == null && requestResults == null) {
            return null;
        } else {
            final Set<Object> results = new HashSet<Object>();
            if (sessionResults != null) {
               results.addAll(sessionResults);
            }
            if (requestResults != null) {
                results.addAll(requestResults);
            }
            return results.isEmpty() ? null : new ArrayList<Object>(results);
        }
    }

    private Map<String, List<Object>> getUnionOfSessionAndRequestAttributes() {
        final Map<String, List<Object>> results = new HashMap<String, List<Object>>();
        final Map<String, List<Object>> sessionAttributes = this.sessionScopedAdditionalDescriptors.getAttributes();
        final Map<String, List<Object>> requestAttributes = this.requestScopedAdditionalDescriptors.getAttributes();
        if (sessionAttributes == null) {
            if (requestAttributes != null) {
                results.putAll(requestAttributes);
            }
        } else {
            results.putAll(sessionAttributes);
            if (requestAttributes != null) {
                for (Map.Entry<String, List<Object>> entry : requestAttributes.entrySet()) {
                    if (results.containsKey(entry.getKey())) {
                        final List<Object> values = results.get(entry.getKey());
                        final Set<Object> unionOfValues = new HashSet<Object>();
                        unionOfValues.addAll(values);
                        for (Object obj : entry.getValue()) {
                            unionOfValues.add(obj);
                        }
                        results.put(entry.getKey(), new ArrayList<Object>(unionOfValues));
                    } else {
                        results.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return results;
    }

}
