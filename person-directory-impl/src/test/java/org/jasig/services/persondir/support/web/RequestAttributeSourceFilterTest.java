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
package org.jasig.services.persondir.support.web;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.services.persondir.support.AdditionalDescriptors;
import org.jasig.services.persondir.util.Util;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestAttributeSourceFilterTest extends TestCase {
    
    
    public void testGetAttributesFromRequest() throws Exception {
        final RequestAttributeSourceFilter requestAttributeSourceFilter = new RequestAttributeSourceFilter();
        
        final AdditionalDescriptors additionalDescriptors = new AdditionalDescriptors();
        requestAttributeSourceFilter.setAdditionalDescriptors(additionalDescriptors);
        
        final Map<String, Object>  cookieAttributeMapping = new LinkedHashMap<>();
        cookieAttributeMapping.put("foo", new LinkedHashSet<Object>(Arrays.asList("foo", "baz")));
        cookieAttributeMapping.put("ding", "ding");
        cookieAttributeMapping.put("boo", "boo");
        requestAttributeSourceFilter.setCookieAttributeMapping(cookieAttributeMapping);

        final Map<String, Object>  headerAttributeMapping = new LinkedHashMap<>();
        headerAttributeMapping.put("user.mail", new LinkedHashSet<Object>(Arrays.asList("user.mail", "email")));
        headerAttributeMapping.put("user.name.given", "user.name.given");
        headerAttributeMapping.put("user.name.family", "user.name.family");
        requestAttributeSourceFilter.setHeaderAttributeMapping(headerAttributeMapping);
        
        requestAttributeSourceFilter.setRemoteUserAttribute("remoteUser");
        requestAttributeSourceFilter.setRemoteAddrAttribute("remoteAddr");
        requestAttributeSourceFilter.setRemoteHostAttribute("remoteHost");
        
        requestAttributeSourceFilter.setUsernameAttribute("remoteUser");
        
        final HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);
        final HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        final FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        
        EasyMock.expect(servletRequest.getRemoteUser()).andReturn("user1");
        EasyMock.expect(servletRequest.getRemoteAddr()).andReturn("127.0.0.1");
        EasyMock.expect(servletRequest.getRemoteHost()).andReturn(null);
        EasyMock.expect(servletRequest.getCookies()).andReturn(new Cookie[] { new Cookie("foo", "bar"), new Cookie("ding", "dong") });
        EasyMock.expect(servletRequest.getHeader("user.mail")).andReturn("user1@example.com");
        EasyMock.expect(servletRequest.getHeader("user.name.given")).andReturn("Joe");
        EasyMock.expect(servletRequest.getHeader("user.name.family")).andReturn(null);
        
        filterChain.doFilter(servletRequest, servletResponse);
        EasyMock.expectLastCall();
                
        EasyMock.replay(servletRequest, servletResponse, filterChain);
        
        requestAttributeSourceFilter.doFilter(servletRequest, servletResponse, filterChain);
        
        EasyMock.verify(servletRequest, servletResponse, filterChain);
        

        assertEquals("user1", additionalDescriptors.getName());
        
        final Map<String, List<Object>> expectedAttributes = new LinkedHashMap<>();
        expectedAttributes.put("remoteUser", Util.list("user1"));
        expectedAttributes.put("remoteAddr", Util.list("127.0.0.1"));
        expectedAttributes.put("remoteHost", Util.list((Object[])null));
        expectedAttributes.put("foo", Util.list("bar"));
        expectedAttributes.put("baz", Util.list("bar"));
        expectedAttributes.put("ding", Util.list("dong"));
        expectedAttributes.put("ding", Util.list("dong"));
        expectedAttributes.put("email", Util.list("user1@example.com"));
        expectedAttributes.put("user.mail", Util.list("user1@example.com"));
        expectedAttributes.put("user.name.given", Util.list("Joe"));
        expectedAttributes.put("user.name.given", Util.list("Joe"));
        
        assertEquals(expectedAttributes, additionalDescriptors.getAttributes());
    }
}
