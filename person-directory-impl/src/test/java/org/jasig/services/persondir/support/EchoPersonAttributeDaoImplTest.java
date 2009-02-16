/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.AbstractPersonAttributeDaoTest;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.Util;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EchoPersonAttributeDaoImplTest extends AbstractPersonAttributeDaoTest {

    /**
     * @see org.jasig.services.persondir.support.AbstractPersonAttributeDaoTest#getPersonAttributeDaoInstance()
     */
    @Override
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return new EchoPersonAttributeDaoImpl();
    }
    
    public void testMapEcho() {
        final EchoPersonAttributeDaoImpl dao = new EchoPersonAttributeDaoImpl();
        
        final Map<String, List<Object>> testMap = new HashMap<String, List<Object>>();
        testMap.put("key1", Util.list("val1"));
        testMap.put("key2", Util.list("val2"));
        
        final Map<String, List<Object>> goalMap = new HashMap<String, List<Object>>(testMap);
        
        final Map<String, List<Object>> resultMap = dao.getMultivaluedUserAttributes(testMap);
        
        assertEquals(goalMap, resultMap);
    }

}
