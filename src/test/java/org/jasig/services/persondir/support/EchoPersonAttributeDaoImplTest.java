/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.HashMap;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class EchoPersonAttributeDaoImplTest extends AbstractPersonAttributeDaoTest {

    /**
     * @see org.jasig.portal.services.persondir.support.AbstractPersonAttributeDaoTest#getPersonAttributeDaoInstance()
     */
    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return new EchoPersonAttributeDaoImpl();
    }
    
    public void testMapEcho() {
        final EchoPersonAttributeDaoImpl dao = new EchoPersonAttributeDaoImpl();
        
        final Map testMap = new HashMap();
        testMap.put("key1", "val1");
        testMap.put("key2", "val2");
        
        final Map goalMap = new HashMap(testMap);
        
        final Map resultMap = dao.getUserAttributes(testMap);
        
        assertEquals(goalMap, resultMap);
    }

}
