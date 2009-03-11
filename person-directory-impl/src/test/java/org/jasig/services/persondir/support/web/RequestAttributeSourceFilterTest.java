/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.services.persondir.support.web;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
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
        
        final Map<String, Object>  headerAttributeMapping = new LinkedHashMap<String, Object>();
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
        EasyMock.expect(servletRequest.getHeader("user.mail")).andReturn("user1@example.com");
        EasyMock.expect(servletRequest.getHeader("user.name.given")).andReturn("Joe");
        EasyMock.expect(servletRequest.getHeader("user.name.family")).andReturn(null);
        
        filterChain.doFilter(servletRequest, servletResponse);
        EasyMock.expectLastCall();
                
        EasyMock.replay(servletRequest, servletResponse, filterChain);
        
        requestAttributeSourceFilter.doFilter(servletRequest, servletResponse, filterChain);
        
        EasyMock.verify(servletRequest, servletResponse, filterChain);
        

        assertEquals("user1", additionalDescriptors.getName());
        
        final Map<String, List<Object>> expectedAttributes = new LinkedHashMap<String, List<Object>>();
        expectedAttributes.put("remoteUser", Util.list("user1"));
        expectedAttributes.put("remoteAddr", Util.list("127.0.0.1"));
        expectedAttributes.put("remoteHost", Util.list(null));
        expectedAttributes.put("email", Util.list("user1@example.com"));
        expectedAttributes.put("user.mail", Util.list("user1@example.com"));
        expectedAttributes.put("user.name.given", Util.list("Joe"));
        expectedAttributes.put("user.name.given", Util.list("Joe"));
        
        assertEquals(expectedAttributes, additionalDescriptors.getAttributes());
    }
}
