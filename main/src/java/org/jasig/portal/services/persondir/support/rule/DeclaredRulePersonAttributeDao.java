package org.jasig.portal.services.persondir.support.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that evaluates
 * person directory information based on configurable rules.  You may chain as 
 * many rules as you like, but this DAO will apply <b>at most</b> one rule, the
 * first that triggers.
 */
public final class DeclaredRulePersonAttributeDao implements IPersonAttributeDao {

    // Instance Members.
    private final String attributeName;
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
        Log log2 = LogFactory.getLog(this.getClass());
        if (log2.isDebugEnabled()) {
            log2.debug("DeclaredRulePersonAttributeDao --> <init>");
            log2.debug("DeclaredRulePersonAttributeDao --> attributeName="+attributeName);
            log2.debug("DeclaredRulePersonAttributeDao --> rules.size()="+rules.size());
            log2.debug("DeclaredRulePersonAttributeDao --> rules.get(0).getClass().getName()="+rules.get(0).getClass().getName());
        }

        // Instance Members.
        this.attributeName = attributeName;
        this.rules = (AttributeRule[]) rules.toArray(new AttributeRule[rules.size()]);

        // PersonDirectory won't stop for anything... we need decent logging.
        Log log = LogFactory.getLog(this.getClass());
        if (log.isDebugEnabled()) {
            log.debug("DeclaredRulePersonAttributeDao --> <init>");
            log.debug("DeclaredRulePersonAttributeDao --> this.attributeName="+this.attributeName);
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

    public Map getUserAttributes(final String attributeValue) {

        // Assertions.
        if (attributeValue == null) {
            String msg = "Argument 'attributeValue' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Map seed = new HashMap();
        seed.put(attributeName, attributeValue);
        return getUserAttributes(seed);

    }

    public Set getPossibleUserAttributeNames() {

        Set rslt = new TreeSet();
        for (int i=0; i < rules.length; i++) {
            rslt.addAll(rules[i].getPossibleUserAttributeNames());
        }

        return rslt;

    }

}
