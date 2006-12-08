package org.jasig.portal.services.persondir.support;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that will only
 * execute if the specified seed attribute matches the indcated pattern.  Each
 * instance of <code>RegexGatewayPersonAttributeDao</code> is backed by another
 * DAO instance that implements the desired behavior should there be a match.
 */
public final class RegexGatewayPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    // Instance Members.
    private final String pattern;
    private final IPersonAttributeDao enclosed;

    /*
     * Public API.
     */

    public RegexGatewayPersonAttributeDao(String attributeName, String pattern,
                                            IPersonAttributeDao enclosed) {

        // Assertions.
        if (attributeName == null) {
            String msg = "Argument 'attributeName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (pattern == null) {
            String msg = "Argument 'pattern' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (enclosed == null) {
            String msg = "Argument 'enclosed' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.setDefaultAttributeName(attributeName);
        this.pattern = pattern;
        this.enclosed = enclosed;

        // PersonDirectory won't stop for anything... we need decent logging.
        if (log.isDebugEnabled()) {
            log.debug("RegexGatewayPersonAttributeDao --> <init>");
            log.debug("RegexGatewayPersonAttributeDao --> this.getDefaultAttributeName()="+this.getDefaultAttributeName());
            log.debug("RegexGatewayPersonAttributeDao --> this.pattern="+this.pattern);
        }

    }

    public Map getUserAttributes(final Map seed) {

        // Assertions.
        if (seed == null) {
            String msg = "Argument 'seed' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        final String attributeName = this.getDefaultAttributeName();
        Object value = seed.get(attributeName);
        if (value == null) {
            // Contract for IPersonAttributeDao says return null where "user doesn't exist."
            return null;
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
            String msg = "The value of " + attributeName + " must be a String, String[], or List<String> "
                                                + "instance.  Found:  " + value.getClass().getName();
            throw new RuntimeException(msg);
        }

        // See if we have a reason to proceed...
        boolean proceed = false;
        for (int i=0; i < compare.length; i++) {
            if (compare[i].matches(pattern)) {
                proceed = true;
                break;
            }
        }

        // Log the result of the comparison...
        if (log.isDebugEnabled()) {
            String matches = null;
            if (proceed) {
                matches = "*DOES MATCH*";
            } else {
                matches = "*DOES NOT MATCH*";
            }
            log .debug("RegexGatewayPersonAttributeDao --> value '"+value+"' "+matches+" pattern '"+pattern+"'");
        }

        Map rslt = null;    // default (again...contract of IPersonAttributeDao)
        if (proceed) {
            rslt = enclosed.getUserAttributes(seed);
        }

        // PersonDirectory won't stop for anything... we need decent logging.
        if (log.isDebugEnabled()) {
            log.debug("RegexGatewayPersonAttributeDao --> getUserAttributes(Map)");
            log.debug("RegexGatewayPersonAttributeDao --> spilling attributes...");
            if (rslt != null) {
                for (Iterator it=rslt.keySet().iterator(); it.hasNext();) {
                    String key = (String) it.next();
                    log.debug("RegexGatewayPersonAttributeDao --> "+key+"="+rslt.get(key));
                }
            } else {
                log.debug("RegexGatewayPersonAttributeDao --> rslt Map is null.");
            }
        }

        return rslt;

    }

    public Set getPossibleUserAttributeNames() {

        return enclosed.getPossibleUserAttributeNames();

    }

}
