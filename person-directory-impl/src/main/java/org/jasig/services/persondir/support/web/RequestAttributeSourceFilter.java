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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jasig.services.persondir.support.IAdditionalDescriptors;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * {@link javax.servlet.Filter} that can provide {@link HttpServletRequest} headers and other properties on the request
 * as person attributes. The filter sets attributes on a {@link IAdditionalDescriptors} which it is configured with. To
 * work correctly the {@link IAdditionalDescriptors} object needs to be a session scoped Spring bean so that each user
 * gets only their own attributes correctly.
 * <br>
 * <br>
 * Required Configuration:
 * <ul>
 *     <li>usernameAttribute</li>
 *     <li>additionalDescriptors</li>
 * </ul>
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestAttributeSourceFilter extends GenericFilterBean {
    public enum ProcessingPosition {
        PRE,
        POST,
        BOTH;
    }
    
    private String usernameAttribute;
    private Map<String, Set<String>> cookieAttributeMapping = Collections.emptyMap();
    private Map<String, Set<String>> headerAttributeMapping = Collections.emptyMap();
    private Map<String, Set<String>> parameterAttributeMapping = Collections.emptyMap();
    private Map<String, Set<String>> requestAttributeMapping = Collections.emptyMap();
    private Set<String> headersToIgnoreSemicolons = new HashSet<>(Arrays.asList(new String[] {"User-Agent"}));
    private IAdditionalDescriptors additionalDescriptors;
    private String remoteUserAttribute;
    private String remoteAddrAttribute;
    private String remoteHostAttribute;
    private String serverNameAttribute;
    private String serverPortAttribute;
    private boolean clearExistingAttributes = false;
    private String referringParameterName;
    private String urlCharacterEncoding = StandardCharsets.UTF_8.name();

    private ProcessingPosition processingPosition = ProcessingPosition.POST;
    
    public String getUsernameAttribute() {
        return usernameAttribute;
    }
    /**
     * The name of the attribute from the request (header or property) to use as the username. Required
     * so that Person Directory can later associate these attributes with the user correctly during queries.
     */
    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public String getRemoteUserAttribute() {
        return remoteUserAttribute;
    }
    /**
     * If specified {@link HttpServletRequest#getRemoteUser()} is added as an attribute under the provided name
     */
    public void setRemoteUserAttribute(final String remoteUserAttribute) {
        this.remoteUserAttribute = remoteUserAttribute;
    }

    public String getRemoteAddrAttribute() {
        return remoteAddrAttribute;
    }
    /**
     * If specified {@link HttpServletRequest#getRemoteAddr()} is added as an attribute under the provided name
     */
    public void setRemoteAddrAttribute(final String remoteAddrAttribute) {
        this.remoteAddrAttribute = remoteAddrAttribute;
    }

    public String getRemoteHostAttribute() {
        return remoteHostAttribute;
    }
    /**
     * If specified {@link HttpServletRequest#getRemoteHost()} is added as an attribute under the provided name
     */
    public void setRemoteHostAttribute(final String remoteHostAttribute) {
        this.remoteHostAttribute = remoteHostAttribute;
    }
    
    public String getServerNameAttribute() {
        return serverNameAttribute;
    }
    /**
     * If specified {@link HttpServletRequest#getServerName()} is added as an attribute under the provided name
     */
    public void setServerNameAttribute(final String serverNameAttribute) {
        this.serverNameAttribute = serverNameAttribute;
    }

    public String getServerPortAttribute() {
        return serverPortAttribute;
    }
    /**
     * If specified {@link HttpServletRequest#getServerPort()} is added as an attribute under the provided name
     */
    public void setServerPortAttribute(final String serverPortAttribute) {
        this.serverPortAttribute = serverPortAttribute;
    }
    
    public IAdditionalDescriptors getAdditionalDescriptors() {
        return additionalDescriptors;
    }
    /**
     * The {@link IAdditionalDescriptors} instance to set request attributes on. This should be a Spring session-scoped
     * proxy to allow each session to have its own set of request-populated attributes.
     */
    public void setAdditionalDescriptors(final IAdditionalDescriptors additionalDescriptors) {
        this.additionalDescriptors = additionalDescriptors;
    }

    public boolean isClearExistingAttributes() {
        return clearExistingAttributes;
    }
    /**
     * If true when attributes are found on the request any existing attributes in the provided {@link IAdditionalDescriptors}
     * object will cleared and replaced with the new attributes. If false (default) the new attributes overwrite existing
     * attributes of the same name but attributes in {@link IAdditionalDescriptors} not found on the current request
     * are not touched.
     *
     * @param clearExistingAttributes If existing all attributes should be cleared when any new attributes are found.
     */
    public void setClearExistingAttributes(final boolean clearExistingAttributes) {
        this.clearExistingAttributes = clearExistingAttributes;
    }
    
    public ProcessingPosition getProcessingPosition() {
        return processingPosition;
    }
    /**
     * Sets the pre/post/both position of the processing relative to the doFilter call.
     * PRE  means the attribute processing happens before the doFilter call
     * POST means the attribute processing happens after the doFilter call
     * BOTH means the attribute processing happens before and after the doFilter call
     */
    public void setProcessingPosition(final ProcessingPosition processingPosition) {
        this.processingPosition = processingPosition;
    }

    /**
     * Set the {@link Map} to use for mapping from a cookie name to an attribute name or {@link Set} of attribute
     * names. Cookie names that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set} 
     * of {@link String}.
     * 
     * @param cookieAttributeMapping {@link Map} from cookie names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setCookieAttributeMapping(final Map<String, ?> cookieAttributeMapping) {
        this.cookieAttributeMapping = makeMapValueSetOfStrings(cookieAttributeMapping);
    }

    public Map<String, Set<String>> getCookieAttributeMapping() {
        return cookieAttributeMapping;
    }

    public Map<String, Set<String>> getHeaderAttributeMapping() {
        return headerAttributeMapping;
    }

    /**
     * Set the {@link Map} to use for mapping from a header name to an attribute name or {@link Set} of attribute
     * names. Header names that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set} 
     * of {@link String}.
     * 
     * @param headerAttributeMapping {@link Map} from header names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setHeaderAttributeMapping(final Map<String, ?> headerAttributeMapping) {
        this.headerAttributeMapping = makeMapValueSetOfStrings(headerAttributeMapping);
    }

    public Map<String, Set<String>> getRequestAttributeMapping() {
        return requestAttributeMapping;
    }

    /**
     * Set the {@link Map} to use for mapping from a request attributes to an attribute name or {@link Set} of attribute
     * names. Request attributes that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set}
     * of {@link String}.
     *
     * @param requestAttributeMapping {@link Map} from request attributes to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     */
    public void setRequestAttributeMapping(final Map<String, ?> requestAttributeMapping) {
        this.requestAttributeMapping = makeMapValueSetOfStrings(requestAttributeMapping);
    }

    public Map<String, Set<String>> getParameterAttributeMapping() {
        return parameterAttributeMapping;
    }

    /**
     * Set the {@link Map} to use for mapping from a parameter name to an attribute name or {@link Set} of attribute
     * names. Parameter names that are not specified as keys in this {@link Map} will be ignored.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values of type {@link String} or a {@link Set}
     * of {@link String}.
     *
     * @param parameterAttributeMapping {@link Map} from parameter names to attribute names, may not be null.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     * @see MultivaluedPersonAttributeUtils#parseAttributeToAttributeMapping(Map)
     * @since 1.7.1
     */
    public void setParameterAttributeMapping(final Map<String, ?> parameterAttributeMapping) {
        this.parameterAttributeMapping = makeMapValueSetOfStrings(parameterAttributeMapping);
    }

    private Map<String, Set<String>> makeMapValueSetOfStrings(Map<String, ?> attributeMapping) {
        final Map<String, Set<String>> parsedParameterAttributeMapping = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(attributeMapping);

        if (parsedParameterAttributeMapping.containsKey("")) {
            throw new IllegalArgumentException("The map from attribute names to attributes must not have any empty keys.");
        }
        return parsedParameterAttributeMapping;
    }

    public String getReferringParameterName() {
        return referringParameterName;
    }

    /**
     * Name of a request parameter whose value is decoded and checked for <code>parameterAttributeMapping</code> matches.
     * This is useful when the user's browser follows a redirect path and the request parameters from the user's original
     * request are encoded in a 'refUrl' or similar parameter.  uPortal, for instance, does this. When a user accesses
     * /uPortal?nativeClient=true, a redirect chain encodes this to '/uPortal/Login?refUrl=%2FuPortal%2F%3FnativeClient%3Dtrue'.
     * Has no effect if not specified or if <code>parameterAttributeMapping</code> does not have matches to request
     * parameters in the <code>referringParameterName</code>.  If specified, <code>parameterAttributeMapping</code>
     * matches in first the <code>referringParameterName</code> and then the request parameters (thus would overwrite).
     * @param referringParameterName Name of a request parameter to decode and inspect for
     *                               <code>parameterAttributeMapping</code> matches.
     * @since 1.7.1
     */
    public void setReferringParameterName(String referringParameterName) {
        this.referringParameterName = referringParameterName;
    }

    public String getUrlCharacterEncoding() {
        return urlCharacterEncoding;
    }

    /**
     * Sets the URL character encoding to use to decode the value of the <code>referringParameterName</code> if it
     * was specified.  Defaults to UTF-8.
     * @param urlCharacterEncoding URL character encoding name
     * @since 1.7.1
     */
    public void setUrlCharacterEncoding(String urlCharacterEncoding) {
        this.urlCharacterEncoding = urlCharacterEncoding;
    }

    public Set<String> getHeadersToIgnoreSemicolons() {
        return headersToIgnoreSemicolons;
    }

    /**
     * Set of header values to ignore splitting on semicolons.  Some HTTP Headers, such as the User-Agent string,
     * should not be split on semicolons.  Defaults to User-Agent
     * @param headersToIgnoreSemicolons Set of HTTP Header names to not split its value on semicolons into
     *                                  multiple values
     */
    public void setHeadersToIgnoreSemicolons(Set<String> headersToIgnoreSemicolons) {
        this.headersToIgnoreSemicolons = headersToIgnoreSemicolons;
    }

    /* (non-Javadoc)
             * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
             */
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {
        if (ProcessingPosition.PRE == this.processingPosition || ProcessingPosition.BOTH == this.processingPosition) {
            this.doProcessing(servletRequest);
        }
        
        chain.doFilter(servletRequest, servletResponse);
        
        if (ProcessingPosition.POST == this.processingPosition || ProcessingPosition.BOTH == this.processingPosition) {
            this.doProcessing(servletRequest);
        }
    }

    private void doProcessing(final ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
            
            final Map<String, List<Object>> attributes = new LinkedHashMap<>();
            
            this.addRequestProperties(httpServletRequest, attributes);

            this.addRequestCookies(httpServletRequest, attributes);

            this.addRequestHeaders(httpServletRequest, attributes);

            addRequestParameters(httpServletRequest, attributes);

            addRequestAttributes(httpServletRequest, attributes);

            final String username;
            final List<Object> usernameAttributes = attributes.get(this.usernameAttribute);
            if (usernameAttributes == null || usernameAttributes.isEmpty() || usernameAttributes.get(0) == null) {
                this.logger.info("No username found for attribute '" + this.usernameAttribute + "' among " + attributes);
                username = null;
            }
            else {
                username = usernameAttributes.get(0).toString();
            }
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding attributes for user " + username + ". " + attributes);
            }
            
            this.additionalDescriptors.setName(username);

            if (this.clearExistingAttributes) {
                this.additionalDescriptors.setAttributes(attributes);
            }
            else {
                this.additionalDescriptors.addAttributes(attributes);
            }
        }
    }

    /**
     * Add other properties from the request to the attributes map
     */
    protected void addRequestProperties(final HttpServletRequest httpServletRequest, final Map<String, List<Object>> attributes) {
        if (this.remoteUserAttribute != null) {
            final String remoteUser = httpServletRequest.getRemoteUser();
            attributes.put(this.remoteUserAttribute, list(remoteUser));
        }
        if (this.remoteAddrAttribute != null) {
            final String remoteAddr = httpServletRequest.getRemoteAddr();
            attributes.put(this.remoteAddrAttribute, list(remoteAddr));
        }
        if (this.remoteHostAttribute != null) {
            final String remoteHost = httpServletRequest.getRemoteHost();
            attributes.put(this.remoteHostAttribute, list(remoteHost));
        }
        if (this.serverNameAttribute != null) {
            final String serverName = httpServletRequest.getServerName();
            attributes.put(this.serverNameAttribute, list(serverName));
        }
        if (this.serverPortAttribute != null) {
            final int serverPort = httpServletRequest.getServerPort();
            attributes.put(this.serverPortAttribute, list(serverPort));
        }
    }

    /**
     * Add request cookies to the attributes map
     */
    protected void addRequestCookies(final HttpServletRequest httpServletRequest, final Map<String, List<Object>> attributes) {
        final Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return;
        }

        for (final Cookie cookie : cookies) {
            final String cookieName = cookie.getName();
            if (this.cookieAttributeMapping.containsKey(cookieName)) {
                for (final String attributeName : this.cookieAttributeMapping.get(cookieName)) {
                    attributes.put(attributeName, list(cookie.getValue()));
                }
            }
        }
    }

    /**
     * Add request headers to the attributes map
     */
    protected void addRequestHeaders(final HttpServletRequest httpServletRequest, final Map<String, List<Object>> attributes) {
        for (final Map.Entry<String, Set<String>> headerAttributeEntry : this.headerAttributeMapping.entrySet()) {
            final String headerName = headerAttributeEntry.getKey();
            final String value = httpServletRequest.getHeader(headerName);
            
            if (value != null) {
                for (final String attributeName : headerAttributeEntry.getValue()) {
                    attributes.put(attributeName,
                            headersToIgnoreSemicolons.contains(headerName) ?
                                    list(value)
                                    : splitOnSemiColonHandlingBackslashEscaping(value));
                }
            }
        }
    }

    /**
     * Add specified request parameters to the attributes map.  Some Shibboleth (Apache httpd mod-shib) configurations
     * use environment variables which get passed by AJP as request attributes rather than HTTP Headers to pass
     * attributes from the IDP to the application.
     * @param httpServletRequest Servlet Request
     * @param attributes Map of attributes
     * @since 1.7.1
     */
    protected void addRequestAttributes(final HttpServletRequest httpServletRequest, final Map<String, List<Object>> attributes) {
        for (final Map.Entry<String, Set<String>> attributeMapping : requestAttributeMapping.entrySet()) {
            final String attributeName = attributeMapping.getKey();
            final Object value = httpServletRequest.getAttribute(attributeName);

            if (value instanceof String) {
                if (value != null) {
                    for (final String attrName : attributeMapping.getValue()) {
                        attributes.put(attrName, list(value));
                    }
                }
            } else {
                logger.warn("Specified request attribute " + attributeName + " is not a String");
            }
        }
    }

    /**
     * Add specified request parameters to the attributes map.  The request parameters may be directly in
     * the URL or in a referringUrl parameter (with uPortal accessing /uPortal?nativeClient=true will go through
     * several redirects and get turned into /uPortal/Login?refUrl=%2FuPortal%2F%3FnativeClient%3Dtrue,
     * which when fetched with request.getParameter(refurl) yields '/uPortal/?nativeClient=true').
     * @param httpServletRequest Servlet Request
     * @param attributes Map of attributes
     * @since 1.7.1
     */
    protected void addRequestParameters(final HttpServletRequest httpServletRequest, final Map<String, List<Object>> attributes) {
        // If a referringParameterName is specified, first match against
        if (referringParameterName != null
                && StringUtils.isNotBlank(httpServletRequest.getParameter(referringParameterName))) {
            String referringValue = httpServletRequest.getParameter(referringParameterName);
            Map<String,String> referringParameters = parseRequestParameterString(referringValue);
            for (final Map.Entry<String, Set<String>> parameterMapping : parameterAttributeMapping.entrySet()) {
                final String parameterName = parameterMapping.getKey();
                final String value = referringParameters.get(parameterName);

                if (value != null) {
                    for (final String attributeName : parameterMapping.getValue()) {
                        attributes.put(attributeName, list(value));
                    }
                }
            }
        }
        for (final Map.Entry<String, Set<String>> parameterMapping : parameterAttributeMapping.entrySet()) {
            final String parameterName = parameterMapping.getKey();
            final String value = httpServletRequest.getParameter(parameterName);

            if (value != null) {
                for (final String attributeName : parameterMapping.getValue()) {
                    attributes.put(attributeName, list(value));
                }
            }
        }
    }

    private Map<String,String> parseRequestParameterString (String requestParameterString) {
        Map<String,String> parameters = new HashMap<>();
        if (requestParameterString.indexOf("?") > 0) {
            requestParameterString = requestParameterString.substring(requestParameterString.indexOf("?") + 1);
        }
        String[] parameterStrings = requestParameterString.trim().split("&");
        for (String parameterString : parameterStrings) {
            String[] parts = parameterString.split("=");
            if (parts.length > 1) {
                parameters.put(parts[0], parts[1]);
            } else {
                logger.info("Ignoring encoded parameter " + parts[0]
                        + " in referring url parameter because it has no value");
            }
        }
        return parameters;
    }

    /* Multiple attribute values are separated by a semicolon, and semicolons in values are escaped with a backslash */
    /* (https://wiki.shibboleth.net/confluence/displa)y/SHIB2/NativeSPAttributeAccess) */
    /* transforms "a;b" into list { "a", "b" } */
    /* transforms "a\;b" into list { "a;b" } */
    /* transforms "a;b\;" into list { "a", "b;" } */
    private static List<Object> splitOnSemiColonHandlingBackslashEscaping(final String in) {
	final List<Object> result = new LinkedList<>();

        int i = 1;
        String prefix = "";
        final String[] splitStringArr = in.split(";");
        for (final String s : splitStringArr) {
            final String s2 = s.replaceFirst("\\\\$", ";");
            if (s.equals(s2) || i == splitStringArr.length) {
                result.add(prefix + s2);
                prefix = "";
            } else {
                prefix += s2;
            }
            i++;
        }
        return result;
    }

    private List<Object> list(final Object value) {
        return Collections.singletonList(value);
    }
}
