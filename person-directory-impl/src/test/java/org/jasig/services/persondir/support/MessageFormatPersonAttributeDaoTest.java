/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
