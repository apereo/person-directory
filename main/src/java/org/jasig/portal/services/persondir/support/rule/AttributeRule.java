package org.jasig.portal.services.persondir.support.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines the contract for a person directory user attribute rule for use with
 * the <code>DeclaredRulePersonAttributeDao</code>.
 */
public interface AttributeRule {

    /**
     * Indicates whether the rule applies to the user described by the specified
     * information.  Implementations of {@link AttributeRule} <strong>must
     * not change the input <code>Map</code></strong>. Implementations dictate
     * the expected types for the Keys and Values of the Map.
     * 
     * @param userInfo immutable Map of attributes to values for the implementation to determine if this rule applies, must not be null.
     * @return TRUE if this rule applies to the Map data, FALSE if not.
     * @throws IllegalArgumentException If <code>userInfo</code> is <code>null.</code>
     */
    public abstract boolean appliesTo(Map userInfo);

    /**
     * Applies the embodied rule to the user described by the specified
     * information and returns the result.
     * 
     * This method follows the same contract as {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(Map)}
     */
    public abstract Map evaluate(Map userInfo);

    /**
     * Indicates the complete set of user attribute names that <em>may</em> be
     * returned by a call to <code>evaluate</code>.
     * 
     * This method follows the same contract as {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()}
     */
    public abstract Set getPossibleUserAttributeNames();

}
