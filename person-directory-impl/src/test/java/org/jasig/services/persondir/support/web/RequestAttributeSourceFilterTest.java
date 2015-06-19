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
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestAttributeSourceFilterTest extends TestCase {

    private RequestAttributeSourceFilter requestAttributeSourceFilter;
    private AdditionalDescriptors additionalDescriptors;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    private FilterChain filterChain;
    private Map<String, List<Object>> expectedAttributes;

    @Before
    public void setUp() throws Exception {
        requestAttributeSourceFilter = new RequestAttributeSourceFilter();
        additionalDescriptors = new AdditionalDescriptors();
        requestAttributeSourceFilter.setAdditionalDescriptors(additionalDescriptors);
        expectedAttributes = new LinkedHashMap<>();

        servletRequest = createMock(HttpServletRequest.class);
        servletResponse = createMock(HttpServletResponse.class);
        filterChain = createMock(FilterChain.class);
    }

    @Test
    public void testGetAttributesFromRequest() throws Exception {

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

        final Map<String, Object>  requestAttributeMapping = new LinkedHashMap<>();
        requestAttributeMapping.put("nativeClient", new LinkedHashSet<Object>(Arrays.asList("uMobile", "nativeClient")));
        requestAttributeMapping.put("siteProfile", "siteProfile");
        requestAttributeMapping.put("notPresent", "notPresent");
        requestAttributeSourceFilter.setParameterAttributeMapping(requestAttributeMapping);

        requestAttributeSourceFilter.setRemoteUserAttribute("remoteUser");
        requestAttributeSourceFilter.setRemoteAddrAttribute("remoteAddr");
        requestAttributeSourceFilter.setRemoteHostAttribute("remoteHost");
        
        requestAttributeSourceFilter.setUsernameAttribute("remoteUser");

        expect(servletRequest.getRemoteUser()).andReturn("user1");
        expect(servletRequest.getRemoteAddr()).andReturn("127.0.0.1");
        expect(servletRequest.getRemoteHost()).andReturn(null);
        expect(servletRequest.getCookies()).andReturn(new Cookie[] { new Cookie("foo", "bar"), new Cookie("ding", "dong") });
        expect(servletRequest.getHeader("user.mail")).andReturn("user1@example.com");
        expect(servletRequest.getHeader("user.name.given")).andReturn("Joe");
        expect(servletRequest.getHeader("user.name.family")).andReturn(null);

        expect(servletRequest.getParameter("nativeClient")).andReturn("true");
        expect(servletRequest.getParameter("siteProfile")).andReturn("foxtrot");
        expect(servletRequest.getParameter("notPresent")).andReturn(null);
        
        filterChain.doFilter(servletRequest, servletResponse);
        EasyMock.expectLastCall();
                
        EasyMock.replay(servletRequest, servletResponse, filterChain);
        
        requestAttributeSourceFilter.doFilter(servletRequest, servletResponse, filterChain);
        
        EasyMock.verify(servletRequest, servletResponse, filterChain);

        assertEquals("user1", additionalDescriptors.getName());
        
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
        expectedAttributes.put("nativeClient", Util.list("true"));
        expectedAttributes.put("uMobile", Util.list("true"));
        expectedAttributes.put("siteProfile", Util.list("foxtrot"));

        assertEquals(expectedAttributes, additionalDescriptors.getAttributes());
    }

    @Test
    public void testGetAttributesWithRefUrl() throws Exception {
        requestAttributeSourceFilter.setReferringParameterName("refUrl");

        final Map<String, Object>  requestAttributeMapping = new LinkedHashMap<>();
        requestAttributeMapping.put("nativeClient", new LinkedHashSet<Object>(Arrays.asList("uMobile", "nativeClient")));
        requestAttributeMapping.put("siteProfile", "siteProfile");
        requestAttributeMapping.put("notPresent", "notPresent");
        requestAttributeSourceFilter.setParameterAttributeMapping(requestAttributeMapping);

        expect(servletRequest.getCookies()).andReturn(new Cookie[] { new Cookie("foo", "bar"), new Cookie("ding", "dong") });
        expect(servletRequest.getParameter("refUrl")).andReturn("/uPortal/?nativeClient=true").times(2);
        expect(servletRequest.getParameter("nativeClient")).andReturn(null);
        expect(servletRequest.getParameter("siteProfile")).andReturn(null);
        expect(servletRequest.getParameter("notPresent")).andReturn(null);

        filterChain.doFilter(servletRequest, servletResponse);
        EasyMock.expectLastCall();

        EasyMock.replay(servletRequest, servletResponse, filterChain);

        requestAttributeSourceFilter.doFilter(servletRequest, servletResponse, filterChain);

        EasyMock.verify(servletRequest, servletResponse, filterChain);

        assertNull(additionalDescriptors.getName());

        expectedAttributes.put("nativeClient", Util.list("true"));
        expectedAttributes.put("uMobile", Util.list("true"));

        assertEquals(expectedAttributes, additionalDescriptors.getAttributes());
    }

    @Test
    public void testGetAttributesWithRefUrlMultipleValues() throws Exception {
        requestAttributeSourceFilter.setReferringParameterName("refUrl");

        final Map<String, Object>  requestAttributeMapping = new LinkedHashMap<>();
        requestAttributeMapping.put("nativeClient", new LinkedHashSet<Object>(Arrays.asList("uMobile", "nativeClient")));
        requestAttributeMapping.put("siteProfile", "siteProfile");
        requestAttributeMapping.put("misCode", "misCode");
        requestAttributeSourceFilter.setParameterAttributeMapping(requestAttributeMapping);

        expect(servletRequest.getCookies()).andReturn(new Cookie[] { new Cookie("foo", "bar"), new Cookie("ding", "dong") });
        expect(servletRequest.getParameter("refUrl")).andReturn("/uPortal/?nativeClient=true&misCode=ab123").times(2);
        expect(servletRequest.getParameter("nativeClient")).andReturn(null);
        expect(servletRequest.getParameter("siteProfile")).andReturn(null);
        expect(servletRequest.getParameter("misCode")).andReturn(null);

        filterChain.doFilter(servletRequest, servletResponse);
        EasyMock.expectLastCall();

        EasyMock.replay(servletRequest, servletResponse, filterChain);

        requestAttributeSourceFilter.doFilter(servletRequest, servletResponse, filterChain);

        EasyMock.verify(servletRequest, servletResponse, filterChain);

        assertNull(additionalDescriptors.getName());

        expectedAttributes.put("nativeClient", Util.list("true"));
        expectedAttributes.put("uMobile", Util.list("true"));
        expectedAttributes.put("misCode", Util.list("ab123"));

        assertEquals(expectedAttributes, additionalDescriptors.getAttributes());
    }

}
