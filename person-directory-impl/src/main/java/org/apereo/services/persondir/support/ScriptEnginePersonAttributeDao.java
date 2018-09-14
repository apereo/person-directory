package org.apereo.services.persondir.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributes;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A DAO implementation that takes advantage of Java's scripting engine functionality
 * to load an arbitrary script to resolve attributes. The script is loaded as an external file,
 * a resource on the classpath or in native "raw/string" format
 * and is passed the username plus a logger object at a minimum. It's expected that the outcome
 * of the script be a map of user attributes, with attributes being multi-valued in form of collections.
 * <p>
 * The script must also return an attribute named <code>username</code>.
 * <p>
 * A sample script implementation in Groovy follows:
 * <p>
 * <pre>
   import java.util.*
   def Map&lt;String, List&lt;Object&gt;&gt; run(final Object... args) {
       def uid = args[0]
       def logger = args[1]
       return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
   }
 * </pre>
 * <p>
 * For the script engine to do its job, relevant libraries must be available on the classpath.
 * Options include groovy, python and javascript libraries. The script type is determined by script file extension.
 *
 * @author Misagh Moayyed
 */
public class ScriptEnginePersonAttributeDao extends BasePersonAttributeDao {
    private String scriptFile;
    private boolean caseInsensitiveUsername = false;
    private final IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(final String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public boolean isCaseInsensitiveUsername() {
        return caseInsensitiveUsername;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }

    @Override
    public IPersonAttributes getPerson(final String uid) {
        try {
            final Map attributes = getScriptedAttributesFromFile(uid);
            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoListValues(attributes));
            }
            return new NamedPersonImpl(uid, stuffAttributesIntoListValues(attributes));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoListValues(query));
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        final Set<IPersonAttributes> people = new LinkedHashSet<>();
        final String username = usernameAttributeProvider.getUsernameFromQuery(query);
        final IPersonAttributes person = getPerson(username);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames() {
        return Collections.EMPTY_SET;
    }

    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes() {
        return Collections.EMPTY_SET;
    }

    private Map<String, Object> getScriptedAttributesFromFile(final String uid) throws Exception {
        final String engineName = getScriptEngineName();
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null || StringUtils.isBlank(engineName)) {
            logger.warn("Script engine is not available for [{}]", engineName);
            return new HashMap<>();
        }

        logger.debug("Created groovy script engine instance for [{}]", engineName);
        final Object[] args = {uid, logger};

        final File theScriptFile = new File(this.scriptFile);
        if (theScriptFile.exists()) {
            logger.debug("Loading script from [{}]", theScriptFile);
            engine.eval(new FileReader(theScriptFile));
        } else {
            boolean foundStream = false;
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(this.scriptFile)) {
                if (in != null && in.markSupported() && in.available() > 0) {
                    logger.debug("Loading script [{}] from classloader as a stream", theScriptFile);
                    engine.eval(new InputStreamReader(in));
                    foundStream = true;
                }
            }
            if (!foundStream) {
                logger.debug("Loading script [{}] in raw text format", theScriptFile);
                engine.eval(new StringReader(this.scriptFile));
            }
        }

        logger.debug("Executing script's run method, with parameters [{}]", args);
        final Invocable invocable = (Invocable) engine;
        final Map<String, Object> personAttributesMap = (Map<String, Object>) invocable.invokeFunction("run", args);

        logger.debug("Final set of attributes determined by the script are [{}]", personAttributesMap);
        return personAttributesMap;
    }

    private String getScriptEngineName() {
        String engineName = null;
        if (this.scriptFile.endsWith(".py")) {
            engineName = "python";
        } else if (this.scriptFile.endsWith(".js")) {
            engineName = "js";
        } else if (this.scriptFile.endsWith(".groovy")) {
            engineName = "groovy";
        }
        return engineName;
    }

    private static Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, Object> personAttributesMap) {
        final Map<String, List<Object>> personAttributes = new HashMap<>();
        for (final Map.Entry<String, Object> stringObjectEntry : personAttributesMap.entrySet()) {
            final Object value = stringObjectEntry.getValue();
            if (value instanceof List) {
                personAttributes.put(stringObjectEntry.getKey(), (List) value);
            } else {
                personAttributes.put(stringObjectEntry.getKey(), Arrays.asList(value));
            }
        }
        return personAttributes;
    }
}
