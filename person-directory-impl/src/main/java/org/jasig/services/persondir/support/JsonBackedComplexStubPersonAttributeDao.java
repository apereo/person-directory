package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A convenient wrapper around <code>ComplexStubPersonAttributeDao</code> that reads the configuration for its <i>backingMap</i>
 * property from an external JSON configuration resource. 
 * 
 * <p>Sample JSON file:
 * <pre><code>
 * {
        "u1":{
            "firstName":["Json1"],
            "lastName":["One"],
            "additionalAttribute":["here I AM!!!"],
            "additionalAttribute2":["attr2"],
            "eduPersonAffiliation":["alumni", "staff"]
        },
        "u2":{
            "firstName":["Json2"],
            "lastName":["Two"],
            "eduPersonAffiliation":["employee", "student"]
        },
        "u3":{
            "firstName":["Json3"],
            "lastName":["Three"],
            "eduPersonAffiliation":["alumni", "student", "employee", "some other attr"]
        }   
    }
 * </code></pre>
 * 
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 */
public class JsonBackedComplexStubPersonAttributeDao extends ComplexStubPersonAttributeDao {

    /**
     * A configuration file containing JSON representation of the stub person attributes. REQUIRED.
     */
    private final Resource personAttributesConfigFile;

    private final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBackedComplexStubPersonAttributeDao.class);

    private final Object synchronizationMonitor = new Object();

    public JsonBackedComplexStubPersonAttributeDao(final Resource personAttributesConfigFile) {
        this.personAttributesConfigFile = personAttributesConfigFile;
    }

    /**
     * Init method un-marshals JSON representation of the person attributes.
     */
    public void init() throws Exception {
        /* If we get to this point, the JSON file is well-formed, but its structure does not map into
         * PersonDir backingMap generic type - fail fast.
         */
        try {
            unmarshalAndSetBackingMap();
        } catch (final ClassCastException ex) {
            throw new BeanCreationException(String.format("The semantic structure of the person attributes"
                    + "JSON config is not correct. Please fix it in this resource: [%s]", this.personAttributesConfigFile.getURI()));
        }
    }

    @SuppressWarnings("unchecked")
    private void unmarshalAndSetBackingMap() throws Exception {
        LOGGER.info("Un-marshaling person attributes from the config file [{}] ...", this.personAttributesConfigFile.getFile());
        final Map<String, Map<String, List<Object>>> backingMap = this.jacksonObjectMapper.readValue(
                this.personAttributesConfigFile.getFile(), Map.class);
        LOGGER.debug("Person attributes have been successfully read into a Map<String, Map<String, List<Object>>>: {}", backingMap);
        synchronized (this.synchronizationMonitor) {
            super.setBackingMap(backingMap);
        }
    }

}
