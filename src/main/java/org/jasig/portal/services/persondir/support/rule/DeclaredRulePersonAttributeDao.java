package org.jasig.portal.services.persondir.support.rule;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.portal.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that evaluates
 * person directory information based on configurable rules.  You may chain as 
 * many rules as you like, but this DAO will apply <b>at most</b> one rule, the
 * first that triggers.
 */
public final class DeclaredRulePersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    // Instance Members.
    private final AttributeRule[] rules;

    /*
     * Public API.
     */

    public DeclaredRulePersonAttributeDao(String attributeName, List rules) {

        // Assertions.
        if (attributeName == null) {
            String msg = "Argument 'attributeName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules.size() == 0) {
            String msg = "Argument 'rules' must contain at least one element.";
            throw new IllegalArgumentException(msg);
        }

        // PersonDirectory won't stop for anything... we need decent logging.
        if (log.isDebugEnabled()) {
            log.debug("DeclaredRulePersonAttributeDao --> <init>");
            log.debug("DeclaredRulePersonAttributeDao --> attributeName="+attributeName);
            log.debug("DeclaredRulePersonAttributeDao --> rules.size()="+rules.size());
            log.debug("DeclaredRulePersonAttributeDao --> rules.get(0).getClass().getName()="+rules.get(0).getClass().getName());
        }

        // Instance Members.
        this.setDefaultAttributeName(attributeName);
        this.rules = (AttributeRule[]) rules.toArray(new AttributeRule[rules.size()]);

        // PersonDirectory won't stop for anything... we need decent logging.
        if (log.isDebugEnabled()) {
            log.debug("DeclaredRulePersonAttributeDao --> <init>");
            log.debug("DeclaredRulePersonAttributeDao --> this.getDefaultAttributeName()="+this.getDefaultAttributeName());
            log.debug("DeclaredRulePersonAttributeDao --> this.rules.length="+this.rules.length);
        }

    }

    public Map getUserAttributes(final Map seed) {

        // Assertions.
        if (seed == null) {
            String msg = "Argument 'seed' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Map rslt = null;    // default (contract of IPersonAttributeDao)

        for (int i=0; i < rules.length; i++) {
            AttributeRule r = rules[i];
            if (r.appliesTo(seed)) {

            	rslt = r.evaluate(seed);
                break;  // We promise to apply at most one rule...
            }
        }

        return rslt;

    }

    public Set getPossibleUserAttributeNames() {

        Set rslt = new TreeSet();
        for (int i=0; i < rules.length; i++) {
            rslt.addAll(rules[i].getPossibleUserAttributeNames());
        }

        return rslt;

    }

}
