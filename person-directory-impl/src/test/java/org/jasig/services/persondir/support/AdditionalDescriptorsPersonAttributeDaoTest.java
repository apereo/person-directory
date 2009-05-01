package org.jasig.services.persondir.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;

import junit.framework.TestCase;

public class AdditionalDescriptorsPersonAttributeDaoTest extends TestCase {
    
    private static final String USERNAME = "user";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final IUsernameAttributeProvider UAP = new SimpleUsernameAttributeProvider(USERNAME_ATTRIBUTE);
    private static final ICurrentUserProvider CUP = new ICurrentUserProvider() {
        public String getCurrentUserName() {
            return USERNAME;
        }
    };
    private static final String ATTRIBUTE_NAME = "attribute";
    private static final List<Object> ATTRIBUTE_VALUES = Arrays.asList(new Object[] {"foo", "bar"});
    

    /*
     * Public API.
     */
    
    public void testGetAvailableQueryAttributes() {
        
        AdditionalDescriptors ad = new AdditionalDescriptors();
        ad.setName(USERNAME);
        
        AdditionalDescriptorsPersonAttributeDao adpad = new AdditionalDescriptorsPersonAttributeDao();
        adpad.setUsernameAttributeProvider(UAP);
        adpad.setCurrentUserProvider(CUP);
        adpad.setDescriptors(ad);
        
        TestCase.assertEquals(adpad.getAvailableQueryAttributes(), Collections.singleton(USERNAME_ATTRIBUTE));
        
    }
    
    public void testGetPeopleWithMultivaluedAttributes() {
        
        AdditionalDescriptors ad = new AdditionalDescriptors();
        ad.setName(USERNAME);
        ad.setAttributeValues(ATTRIBUTE_NAME, ATTRIBUTE_VALUES);
        
        AdditionalDescriptorsPersonAttributeDao adpad = new AdditionalDescriptorsPersonAttributeDao();
        adpad.setUsernameAttributeProvider(UAP);
        adpad.setCurrentUserProvider(CUP);
        adpad.setDescriptors(ad);
        
        Map<String,List<Object>> query = new HashMap<String,List<Object>>();
        query.put(ATTRIBUTE_NAME, ATTRIBUTE_VALUES);

        Set<IPersonAttributes> rslt = adpad.getPeopleWithMultivaluedAttributes(query);
        TestCase.assertNull(rslt);

        query.put(USERNAME_ATTRIBUTE, Collections.singletonList((Object) USERNAME));

        rslt = adpad.getPeopleWithMultivaluedAttributes(query);
        TestCase.assertNotNull(rslt);
        TestCase.assertTrue(rslt.size() == 1);
        TestCase.assertTrue(rslt.contains(ad));

    }

}
