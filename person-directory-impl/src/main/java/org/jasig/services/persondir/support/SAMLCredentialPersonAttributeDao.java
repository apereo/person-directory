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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributes;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;

/**
 * Captures user attributes <i>via</i> the SAMLCredential for applications that
 * use Spring Security.  Supports the standard attribute mapping paradigm
 * implemented by AbstractQueryPersonAttributeDao.  This implementation of
 * IPersonAttributeDao is similar to AdditionalDescriptorsPersonAttributeDao in
 * that attributes are provided for the current logged in user or not at all.
 *
 * @since 1.7.1
 * @author drewwills
 */
public class SAMLCredentialPersonAttributeDao extends AbstractQueryPersonAttributeDao<SAMLCredentialPersonAttributeDao.QueryBuilder> {

    private ICurrentUserProvider currentUserProvider;

    /**
     * Sets the {@link ICurrentUserProvider} to use when determining if the
     * additional attributes (from the SAMLCredential) should be returned.  They
     * will only be added if the user for whom attributes are requested is also
     * the current logged in user (making the request).
     *
     * @param currentUserProvider current user provider
     */
    public void setCurrentUserProvider(final ICurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Per AbstractQueryPersonAttributeDao, this method returns "unmapped
     * attributes" which are transformed using the resultAttributeMapping
     * collection.  Use Attribute.name (rather than Attribute.friendlyName) in
     * these mapping definitions.
     */
    @Override
    protected List<IPersonAttributes> getPeopleForQuery(QueryBuilder queryBuilder, String queryUserName) {

        final String currentUserName = currentUserProvider.getCurrentUserName();;
        if (currentUserName == null) {
            this.logger.warn("A null name was returned by the currentUserProvider, returning null.");
            return Collections.emptyList();
        }

        if (currentUserName.equals(queryUserName)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Adding attributes from the SAMLCredential for user " + currentUserName);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                SAMLCredential credential = (SAMLCredential) authentication.getCredentials();

                if (credential != null) {

                    // Provide some optional, TRACE-level logging for what we found
                    if (logger.isTraceEnabled()) {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Credential obtained!");
                        for (Attribute a : credential.getAttributes()) {
                            msg.append("\n    a.getName()=").append(a.getName())
                                .append("\n    a.getFriendlyName()=").append(a.getFriendlyName());
                                for (XMLObject xmlo : a.getAttributeValues()) {
                                    String str = extractStringValue(xmlo);
                                    msg.append("\n        value="+str);
                                }
                        }
                        logger.trace(msg.toString());
                    }

                    // Marshall what we found into an (unmapped) IPersonAttributes object
                    final Map<String, List<Object>> attributes = new HashMap<>();
                    for (Attribute a : credential.getAttributes()) {
                        List<Object> list = new ArrayList<Object>();
                        for (XMLObject xmlo : a.getAttributeValues()) {
                            String str = extractStringValue(xmlo);
                            if (str != null) {
                                list.add(str);
                            }
                        }
                        attributes.put(a.getName(), list);
                    }
                    final IPersonAttributes personAttributes = new CaseInsensitiveNamedPersonImpl(currentUserName, attributes);
                    return Collections.singletonList(personAttributes);
                }

            }
        } else {
            // Optionally log the fact that we _didn't_ add attributes
            if (logger.isTraceEnabled()) {
                logger.trace("Skipping this DAO because "
                        + "!currentUserName.equals(queryUserName);  currentUserName="
                        + currentUserName + ", queryUserName=" + queryUserName);
            }
        }

        return Collections.emptyList();

    }

    /**
     * Extracts the string value of the XMLObject depending upon its type.
     * @param xmlo XMLObject
     * @return String value of object. Null if unable to convert object to string.
     */
    private String extractStringValue(XMLObject xmlo) {
        if (xmlo instanceof XSString) {
            return ((XSString)xmlo).getValue();
        } else if (xmlo instanceof XSAnyImpl) {
            return ((XSAnyImpl)xmlo).getTextContent();
        }
        logger.warn("Unable to map attribute class {} to String. Unknown type. Enable TRACE logging to see attribute name",
                xmlo.getClass());
        return null;
    }

    @Override
    protected QueryBuilder appendAttributeToQuery(QueryBuilder queryBuilder, String dataAttribute, List<Object> queryValues) {
        return new QueryBuilder();
    }

    /*
     * Nested Types
     */

    /**
     * Only extending AbstractQueryPersonAttributeDao for the
     * resultAttributeMapping behavior;  for the present we don't need to build
     * queries.  We can extend this object (or replace it) if that changes.
     */
    public static final class QueryBuilder {

    }

}
