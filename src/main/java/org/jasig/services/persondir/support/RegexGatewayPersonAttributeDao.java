package org.jasig.services.persondir.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * This DAO wraps another DAO and only executes the wrapped DAO if the data in the seed matches
 * criteria set out by the configured <code>patterns</code> {@link Map}. Multiple seed attributes
 * can be tested by specifying the attribute name as the key of the <code>patterns</code> {@link Map}
 * and the regular expression pattern as the value.
 * 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">patterns</td>
 *         <td>
 *             A {@link Map} of {@link String} attribute names to {@link String} regular
 *             expression patterns.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">targetPersonAttributeDao</td>
 *         <td>
 *             A the {@link IPersonAttributeDao} to delegate the call to if the pattern matching
 *             criteria is met.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">matchAllPatterns</td>
 *         <td>
 *             If true all patterns in the <code>patterns</code> map must past the value mapping
 *             criteria for the <code>targetPersonAttributeDao</code> to be delegated to. If false
 *             only one of the patterns needs to pass the value mapping criteria.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">matchAllValues</td>
 *         <td>
 *             If true all values for the attribute being tested must match the pattern testing
 *             it for the criteria to be met. If false only one of the values needs to match
 *             the pattern for the criteria to be met.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">false</td>
 *     </tr>
 * </table>
 */
public final class RegexGatewayPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    private boolean matchAllPatterns = false;
    private boolean matchAllValues = false;
    private Map patterns = null;
    private IPersonAttributeDao targetPersonAttributeDao = null;

    /**
     * Creates a RegexGatewayPersonAttributeDao that will test a single attribute. The specified
     * attribute is also configured as the {@link #setDefaultAttributeName(String)}.
     * 
     * @param attributeName The attribute to test, is also set as the defaultAttributeName.
     * @param pattern The pattern to test the specified attribute with.
     * @param enclosed The IPersonAttributeDao to delegate to if the pattern matches.
     */
    public RegexGatewayPersonAttributeDao(String attributeName, String pattern, IPersonAttributeDao enclosed) {
        if (attributeName == null) {
            throw new IllegalArgumentException("Argument 'attributeName' cannot be null.");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("Argument 'pattern' cannot be null.");
        }

        // Instance Members.
        this.setDefaultAttributeName(attributeName);
        this.setPatterns(Collections.singletonMap(this.getDefaultAttributeName(), pattern));
        this.setTargetPersonAttributeDao(enclosed);

        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Created RegexGatewayPersonAttributeDao with defaultAttributeName='" + this.getDefaultAttributeName() + "' and patterns=" + this.patterns);
        }
    }
    
    /**
     * @return the patterns
     */
    public Map getPatterns() {
        return this.patterns;
    }
    /**
     * @param patterns the patterns to set
     */
    public void setPatterns(Map patterns) {
        if (patterns == null || patterns.size() < 1) {
            throw new IllegalArgumentException("patterns Map may not be null and must contain at least 1 mapping.");
        }

        this.patterns = Collections.unmodifiableMap(new HashMap(patterns));
    }

    /**
     * @return the targetPersonAttributeDao
     */
    public IPersonAttributeDao getTargetPersonAttributeDao() {
        return this.targetPersonAttributeDao;
    }
    /**
     * @param targetPersonAttributeDao the targetPersonAttributeDao to set
     */
    public void setTargetPersonAttributeDao(IPersonAttributeDao targetPersonAttributeDao) {
        if (targetPersonAttributeDao == null) {
            throw new IllegalArgumentException("targetPersonAttributeDao may not be null");
        }

        this.targetPersonAttributeDao = targetPersonAttributeDao;
    }
    
    /**
     * @return the matchAllPatterns
     */
    public boolean isMatchAllPatterns() {
        return this.matchAllPatterns;
    }
    /**
     * @param matchAllPatterns the matchAllPatterns to set
     */
    public void setMatchAllPatterns(boolean matchAllPatterns) {
        this.matchAllPatterns = matchAllPatterns;
    }

    /**
     * @return the matchAllValues
     */
    public boolean isMatchAllValues() {
        return this.matchAllValues;
    }
    /**
     * @param matchAllValues the matchAllValues to set
     */
    public void setMatchAllValues(boolean matchAllValues) {
        this.matchAllValues = matchAllValues;
    }

    /*
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Argument 'seed' cannot be null.");
        }
        if (patterns == null || patterns.size() < 1) {
            throw new IllegalStateException("patterns Map may not be null and must contain at least 1 mapping.");
        }
        if (targetPersonAttributeDao == null) {
            throw new IllegalStateException("targetPersonAttributeDao may not be null");
        }

        //Flag for patterns that match
        boolean matchedPatterns = false;
        
        //Iterate through all attributeName/pattern pairs
        for (final Iterator patternEntryItr = this.patterns.entrySet().iterator(); patternEntryItr.hasNext();) {
            final Map.Entry patternEntry = (Map.Entry)patternEntryItr.next();
            final String attributeName = (String)patternEntry.getKey();
            final Object attributeValue = seed.get(attributeName);
            
            //Check if the value exists
            if (attributeValue == null) {
                if (this.matchAllPatterns) {
                    //Need to match ALL patters, if the attribute isn't in the seed it can't be matched, return null
                    if (this.logger.isInfoEnabled()) {
                        this.logger.info("All patterns must match and attribute='" + attributeName + "' does not exist in the seed, returning null.");
                    }

                    return null;
                }
                else {
                    //Don't need to match all, just go to the next attribute and see if it exists
                    continue;
                }
            }
            
            //Convert the value to a List
            final List valueList;
            if (attributeValue instanceof List) {
                valueList = (List)attributeValue;
            }
            else {
                valueList = Collections.singletonList(attributeValue);
            }
            
            //The pattern to test the attribute's value(s) with
            final String pattern = (String)patternEntry.getValue();
            if (pattern == null) {
                throw new IllegalStateException("Attribute '" + attributeName + "' has a null pattern");
            }
            
            //Compile the regex for better performance if there are a lot of values
            final Pattern compiledPattern = Pattern.compile(pattern);

            //Flag for matching the pattern on the values
            boolean matchedValues = false;
            
            //Iterate over the values for the attribute, testing each against the pattern
            for (final Iterator valueItr = valueList.iterator(); valueItr.hasNext();) {
                final String value;
                try {
                    value = (String)valueItr.next();
                }
                catch (ClassCastException cce) {
                    final IllegalArgumentException iae = new IllegalArgumentException("RegexGatewayPersonAttributeDao can only accept seeds who's values are String or List of String. Attribute '" + attributeName + "' has a non-String value.");
                    iae.initCause(cce);
                    throw iae;
                }
                
                //Check if the value matches the pattern
                final Matcher valueMatcher = compiledPattern.matcher(value);
                matchedValues = valueMatcher.matches();
                
                //Only one value needs to be matched, this one matched so no need to test the rest, break out of the loop
                if (matchedValues && !this.matchAllValues) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("value='" + value + "' matched pattern='" + pattern + "' and only one value match is needed, leaving value matching loop.");
                    }

                    break;
                }
                //Need to match all values, this one failed so no need to test the rest, break out of the loop
                else if (!matchedValues && this.matchAllValues) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("value='" + value + "' did not match pattern='" + pattern + "' and all values need to match, leaving value matching loop.");
                    }
                    
                    break;
                }
                //Extra logging
                else if (this.logger.isDebugEnabled()) {
                    if (matchedValues) {
                        this.logger.debug("value='" + value + "' matched pattern='" + pattern + "' and all values need to match, continuing value matching loop.");
                    }
                    else {
                        this.logger.debug("value='" + value + "' did not match pattern='" + pattern + "' and only one value match is needed, continuing value matching loop.");
                    }
                }
            }
            
            matchedPatterns = matchedValues;
            
            //Only one pattern needs to be matched, this one matched so no need to test the rest, break out of the loop
            if (matchedPatterns && !this.matchAllPatterns) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("pattern='" + pattern + "' found a match and only one pattern match is needed, leaving pattern matching loop.");
                }

                break;
            }
            //Need to match all patterns, this one failed so no need to test the rest, break out of the loop
            else if (!matchedPatterns && this.matchAllPatterns) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("pattern='" + pattern + "' did not find a match and all patterns need to match, leaving pattern matching loop.");
                }

                break;
            }
            //Extra logging
            else if (this.logger.isDebugEnabled()) {
                if (matchedPatterns) {
                    this.logger.debug("pattern='" + pattern + "' found a match and all patterns need to match, continuing pattern matching loop.");
                }
                else {
                    this.logger.debug("pattern='" + pattern + "' did not find a match and only one pattern match is needed, continuing pattern matching loop.");
                }
            }
        }
        
        //Execute the wrapped DAO if the match criteria was met
        if (matchedPatterns) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Matching criteria was met, delegating call to the targetPersonAttributeDao='" + targetPersonAttributeDao + "'");
            }
            
            return this.targetPersonAttributeDao.getUserAttributes(seed);
        }
        else {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Matching criteria was not met, return null");
            }
            
            return null;
        }
    }

    /*
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return targetPersonAttributeDao.getPossibleUserAttributeNames();
    }
}
