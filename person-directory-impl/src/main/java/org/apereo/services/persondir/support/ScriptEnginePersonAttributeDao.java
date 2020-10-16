package org.apereo.services.persondir.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
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
       def currentAttributes = args[2]
       return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
   }
 * </pre>
 * <p>
 * For the script engine to do its job, relevant libraries must be available on the classpath.
 * Options include groovy, python and javascript libraries. The script type is determined by script file extension.
 *
 * @author Misagh Moayyed
 */
@Deprecated
public class ScriptEnginePersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    private static final Logger logger = LoggerFactory.getLogger(ScriptEnginePersonAttributeDao.class);
    private String scriptFile;
    public enum SCRIPT_TYPE {RESOURCE, FILE, CONTENTS}
    private SCRIPT_TYPE scriptType;
    private String engineName;
    private boolean caseInsensitiveUsername = false;

    public String getScriptFile() {
        return scriptFile;
    }

    // Current unit tests re-use an instance of this DAO,
    // this object would be better if engineName and scriptFile couldn't change
    public void setScriptFile(final String scriptFile) {
        this.scriptFile = scriptFile;
        this.scriptType = determineScriptType(scriptFile);
        if (scriptType != SCRIPT_TYPE.CONTENTS) {
            // assume that if adjusting the file, engine name should also be re-calc'd
            // if scriptType is CONTENTS then we can't determine engineName anyway
            this.engineName = getScriptEngineName(scriptFile);
        }
    }

    public String getEngineName() {
        return engineName;
    }

    protected SCRIPT_TYPE getScriptType() {
        return scriptType;
    }

    public void setEngineName(final String engineName) {
        this.engineName = engineName;
        final var engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null) {
            logger.warn("Specified engineName {} is not available in classpath.", engineName);
        }
    }

    public boolean isCaseInsensitiveUsername() {
        return caseInsensitiveUsername;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }

    /**
     * This should probably be deprecated in favor of constructors that guarantee required properties are set
     */
    public ScriptEnginePersonAttributeDao() {
    }

    /**
     * Create DAO with reference to file or the contents of a script. 
     * 
     * @param scriptFile This can be a path to a file, classpath resource, or the script contents as string.
     * If its the string version then engine name must be set using setter.
     */
    public ScriptEnginePersonAttributeDao(String scriptFile) {
        this(scriptFile, getScriptEngineName(scriptFile));
    }

    /**
     * Create DAO with reference to file or the contents of a script. 
     * 
     * @param scriptFile This can be a path to a file, classpath resource, or the script contents as string.
     * If its the string version then engine name must be set using setter.
     * @param engineName Script engine name such as js, groovy, python
     */
    public ScriptEnginePersonAttributeDao(String scriptFile, String engineName) {
        setScriptFile(scriptFile);
        setEngineName(engineName);
    }

    /**
     * Create DAO with reference to file or the contents of a script.
     *
     * @param scriptFile This can be a path to a file, classpath resource, or the script contents as string.
     * If its the string version then engine name must be set using setter.
     * @param engineName Script engine name such as js, groovy, python
     * @param usernameAttributeProvider Attribute provider
     */
    public ScriptEnginePersonAttributeDao(String scriptFile, String engineName, IUsernameAttributeProvider usernameAttributeProvider) {
        this(scriptFile, engineName);
        setUsernameAttributeProvider(usernameAttributeProvider);
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        try {
            if (!this.isEnabled()) {
                return null;
            }
            if (uid == null) {
                logger.warn("Unable to get attributes from script {} because username is null", scriptFile);
                return null;
            }
            final Map<String, Object> attributes = getScriptedAttributesFromFile(uid, Collections.EMPTY_MAP );
            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, MultivaluedPersonAttributeUtils.toMultivaluedMap(attributes));
            }
            return new NamedPersonImpl(uid, MultivaluedPersonAttributeUtils.toMultivaluedMap((attributes)));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter) {
        try {
            if (!this.isEnabled()) {
                return null;
            }
            final var uid = getUsernameAttributeProvider().getUsernameFromQuery(query);
            if (uid == null) {
                logger.warn("Unable to find username in map {} using attribute {}", query, getUsernameAttributeProvider().getUsernameAttribute());
                return null;
            }
            final Set<IPersonAttributes> people = new LinkedHashSet<>();
            IPersonAttributes person;
            final var attributes = getScriptedAttributesFromFile(uid, query );
            if (this.caseInsensitiveUsername) {
                person = new CaseInsensitiveNamedPersonImpl(uid, MultivaluedPersonAttributeUtils.toMultivaluedMap(attributes));
            } else {
                person = new NamedPersonImpl(uid, MultivaluedPersonAttributeUtils.toMultivaluedMap((attributes)));
            }
            people.add(person);
            return people;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

    private Map<String, Object> getScriptedAttributesFromFile(final String uid, final Map<String, List<Object>> queryAttributes) throws Exception {

        if (StringUtils.isBlank(scriptFile)) {
            logger.warn("Script file or contents not set.");
            return new HashMap<>();
        }

        if (StringUtils.isBlank(engineName)) {
            if (scriptType == SCRIPT_TYPE.CONTENTS) {
                // don't log contents of script
                logger.warn("Engine name not specified, not running script.");
            } else {
                logger.warn("Unable to determine engineName for script: {}, not running script", scriptFile);
            }
            return new HashMap<>();
        }

        // ScriptEngineManager().getEngineByName(engineName) will throw NPE if engineName is null
        final var engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null) {
            logger.warn("Script engine is not available in classpath for [{}]", engineName);
            return new HashMap<>();
        }

        logger.debug("Created script engine instance for [{}]", engineName);
        final Object[] args = {uid, logger, queryAttributes};

        switch (scriptType) {
            case RESOURCE:
                try (var in = getClass().getClassLoader().getResourceAsStream(this.scriptFile)) {
                    if (in != null && in.markSupported() && in.available() > 0) {
                        logger.debug("Loading script [{}] from classloader as a stream", this.scriptFile);
                        engine.eval(new InputStreamReader(in));
                    }
                }
                break;
            case FILE:
                final var theScriptFile = new File(this.scriptFile);
                logger.debug("Loading script from [{}]", theScriptFile);
                engine.eval(new FileReader(theScriptFile));
                break;
            case CONTENTS:
                logger.debug("Evaluating script contents [\n{}\n]", this.scriptFile);
                engine.eval(new StringReader(this.scriptFile));
                break;
            default:
                throw new IllegalStateException("Unsupported script type: " + scriptType);

        }

        logger.debug("Executing script's run method, with parameters [{}]", args);
        final var invocable = (Invocable) engine;
        final var personAttributesMap = (Map<String, Object>) invocable.invokeFunction("run", args);

        logger.debug("Final set of attributes determined by the script are [{}]", personAttributesMap);
        return personAttributesMap;
    }

    private SCRIPT_TYPE determineScriptType(String fileName) {
        var f = new File(fileName);
        if (f.exists() && f.isFile()) {
            return SCRIPT_TYPE.FILE;
        }

        var in = ScriptEnginePersonAttributeDao.class.getClassLoader().getResourceAsStream(fileName);
        try {
            if (in != null && in.markSupported() && in.available() > 0) {
                return SCRIPT_TYPE.RESOURCE;
            }
        } catch (IOException e) {
            logger.warn("Error checking if stream exists: {}",e.getMessage(),e);
            return SCRIPT_TYPE.CONTENTS;
        }
        return SCRIPT_TYPE.CONTENTS;
    }




    /**
     * This method is static is available as utility for users that are passing the contents of a script
     * and want to set the engineName property based on a filename.
     *
     * @param filename the filename
     * @return script engine name
     */
    public static String getScriptEngineName(String filename) {
        var extension = FilenameUtils.getExtension(filename);
        if (StringUtils.isBlank(extension)) {
            logger.warn("Can't determine engine name based on filename without extension {}", filename);
            return null;
        }
        var manager = new ScriptEngineManager();
        var engines = manager.getEngineFactories();
        for (var engineFactory : engines) {
            var extensions = engineFactory.getExtensions();
            for (var supportedExt : extensions) {
                if (extension.equals(supportedExt)) {
                    // return first short name
                    return engineFactory.getNames().get(0);
                }
            }
        }
        logger.warn("Can't determine engine name based on filename and available script engines {}", filename);
        return null;
    }
}
