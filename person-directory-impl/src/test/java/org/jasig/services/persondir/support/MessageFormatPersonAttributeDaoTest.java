/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.util.Util;

import junit.framework.TestCase;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MessageFormatPersonAttributeDaoTest extends TestCase {
    public void testMessageFormatAttributes() throws Exception {
        final MessageFormatPersonAttributeDao messageFormatPersonAttributeDao = new MessageFormatPersonAttributeDao();
        
        final MessageFormatPersonAttributeDao.FormatAttribute formatAttribute = new MessageFormatPersonAttributeDao.FormatAttribute();
        formatAttribute.setAttributeNames(Collections.singleton("displayName"));
        formatAttribute.setFormat("{0} {1}");
        formatAttribute.setSourceAttributes(Util.genList("firstName", "lastName"));
        
        messageFormatPersonAttributeDao.setFormatAttributes(Collections.singleton(formatAttribute));
        
        final Map<String, List<Object>> query = new LinkedHashMap<String, List<Object>>();
        query.put("firstName", Util.list("Eric"));
        query.put("lastName", Util.list("Dalquist"));
        
        final Map<String, List<Object>> result = messageFormatPersonAttributeDao.getMultivaluedUserAttributes(query);
        
        
        final Map<String, List<Object>> expectedResult = new LinkedHashMap<String, List<Object>>();
        expectedResult.put("displayName", Util.list("Eric Dalquist"));
        
        assertEquals(expectedResult, result);
    }
}
