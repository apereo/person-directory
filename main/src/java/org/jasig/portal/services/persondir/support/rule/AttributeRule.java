package org.jasig.portal.services.persondir.support.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines the contract for a person directory user attribute rule for use with
 * the <code>DeclaredRulePersonAttributeDao</code>.
 */
public abstract class AttributeRule {

    /**
     * Indicates whether the rule applies to the user described by the specified
     * information.  Implementations of <code>IAttributeRule</code> <strong>must
     * not change the input <code>Map</code></strong>.
     */
    public abstract boolean appliesTo(Map userInfo);

    /**
     * Applies the embodied rule to the user described by the specified
     * information and returns the result.
     */
    public abstract Map evaluate(Map userInfo);

    /**
     * Indicates the complete set of user attribute names that <em>may</em> be
     * returned by a call to <code>evaluate</code>.
     */
    public abstract Set getPossibleUserAttributeNames();

}
