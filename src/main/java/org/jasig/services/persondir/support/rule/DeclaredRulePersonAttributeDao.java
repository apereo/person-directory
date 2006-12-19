package org.jasig.services.persondir.support.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that evaluates
 * person directory information based on configurable rules.  You may chain as 
 * many rules as you like, but this DAO will apply <b>at most</b> one rule, the
 * first that triggers.
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
 *         <td align="right" valign="top">rules</td>
 *         <td>
 *             The array of {@link AttributeRule}s to use when 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 */
public final class DeclaredRulePersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * List of {@link AttributeRule} objects.
     */
    private List rules;


    /**
     * Creates a new DeclaredRulePersonAttributeDao specifying the attributeName to pass to
     * {@link #setDefaultAttributeName(String)} and the {@link List} of {@link AttributeRule}s
     * to pass to {@link #setRules(AttributeRule[])}
     * 
     * @param attributeName
     * @param rules
     */
    public DeclaredRulePersonAttributeDao(String attributeName, List rules) {
        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Creating DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }

        // Instance Members.
        this.setDefaultAttributeName(attributeName);
        this.setRules(rules);

        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Created DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }
    }
    
    /**
     * @return the rules
     */
    public List getRules() {
        return this.rules;
    }
    /**
     * @param rules the rules to set
     */
    public void setRules(List rules) {
        if (rules == null) {
            throw new IllegalArgumentException("Argument 'rules' cannot be null.");
        }
        if (rules.size() < 1) {
            throw new IllegalArgumentException("Argument 'rules' must contain at least one element.");
        }

        this.rules = Collections.unmodifiableList(new ArrayList(rules));
    }


    /*
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        // Assertions.
        if (seed == null) {
            throw new IllegalArgumentException("Argument 'seed' cannot be null.");
        }
        if (rules == null) {
            throw new IllegalStateException("rules array cannot be null.");
        }
        if (rules.size() < 1) {
            throw new IllegalStateException("rules array must contain at least one element.");
        }

        Map rslt = null;    // default (contract of IPersonAttributeDao)

        for (final Iterator rulesItr = rules.iterator(); rulesItr.hasNext();) {
            final AttributeRule r = (AttributeRule)rulesItr.next();
            if (r.appliesTo(seed)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Evaluating rule='" + r + "' from the rules List");
                }

            	rslt = r.evaluate(seed);
                break;  // We promise to apply at most one rule...
            }
        }

        return rslt;
    }

    /**
     * Aggregates the results of calling {@link AttributeRule#getPossibleUserAttributeNames()}
     * on each {@link AttributeRule} instance in the rules array.
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        Set rslt = new TreeSet();
        for (final Iterator rulesItr = rules.iterator(); rulesItr.hasNext();) {
            final AttributeRule r = (AttributeRule)rulesItr.next();
            rslt.addAll(r.getPossibleUserAttributeNames());
        }

        return rslt;
    }

}
