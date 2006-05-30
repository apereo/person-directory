package org.jasig.portal.services.persondir.support.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sets a specified attribute to a specified value when a specified seed value 
 * matches a specified pattern.
 */
public final class SimpleAttributeRule extends AttributeRule {

    // Instance Members.
    private final String whenKey;
    private final String whenPattern;
    private final String setKey;
    private final String setValue;

    /*
     * Public API.
     */

    public SimpleAttributeRule(String whenKey, String whenPattern,
                            String setKey, String setValue) {

        // Assertions.
        if (whenKey == null) {
            String msg = "Argument 'whenKey' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (whenPattern == null) {
            String msg = "Argument 'whenPattern' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (setKey == null) {
            String msg = "Argument 'setKey' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (setValue == null) {
            String msg = "Argument 'setValue' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.whenKey = whenKey;
        this.whenPattern = whenPattern;
        this.setKey = setKey;
        this.setValue = setValue;

    }

    public boolean appliesTo(Map userInfo) {

        // Assertions.
        if (userInfo == null) {
            String msg = "Argument 'userInfo' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Object value = userInfo.get(whenKey);
        if (value == null) {
            // No problem... but we certainly don't apply in this case.
            return false;
        }

        // Figure out what to look at.
        String[] compare = null;
        if (value instanceof String) {
            compare = new String[] { (String) value };
        } else if (value instanceof String[]) {
            compare = (String[]) value;
        } else if (value instanceof List) {
            List list = (List) value;
            try {
                compare = (String[]) list.toArray(new String[list.size()]);
            } catch (ClassCastException cce) {
                String msg = "List values may contain only String instances.";
                throw new RuntimeException(msg, cce);
            }
        } else {
            // This situation isn't workable...
            String msg = "The value of " + whenKey + " must be a String, String[], or List<String> "
                                        + "instance.  Found:  " + value.getClass().getName();
            throw new RuntimeException(msg);
        }

        boolean rslt = false;   // default...
        for (int i=0; i < compare.length; i++) {
            if (compare[i].matches(whenPattern)) {
                rslt = true;
                break;
            }
        }

        return rslt;

    }

    public Map evaluate(Map userInfo) {

        // Assertions.
        if (userInfo == null) {
            String msg = "Argument 'userInfo' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!appliesTo(userInfo)) {
            String msg = "May not evaluate.  This rule does not apply.";
            throw new IllegalArgumentException(msg);
        }

        Map rslt = new HashMap();
        rslt.put(setKey, setValue);
        return rslt;

    }

    public Set getPossibleUserAttributeNames() {

        Set rslt = new HashSet();
        rslt.add(setKey);
        return rslt;

    }

}
