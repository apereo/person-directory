package org.apereo.services.persondir.support.ldap;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.handler.AbstractEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link ActiveDirectoryLdapEntryHandler}.
 *
 * @author Misagh Moayyed
 */
public class ActiveDirectoryLdapEntryHandler extends AbstractEntryHandler<LdapEntry> implements LdapEntryHandler {
    /*
     * The user account is disabled
     */
    public static final int ACCOUNT_DISABLED = 0x00000002;

    /*
     * The account is currently locked out.
     */
    public static final int LOCKOUT = 0x00000010;

    /**
     * Logger instance.
     **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public boolean equals(Object o) {
        return o instanceof ActiveDirectoryLdapEntryHandler;
    }

    @Override
    public int hashCode() {
        return LdapUtils.computeHashCode(753, new Object[0]);
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getName() + "@" + this.hashCode() + "]";
    }

    @Override
    public LdapEntry apply(final LdapEntry ldapEntry) {
        var attr = ldapEntry.getAttribute("userAccountControl");
        if (attr != null) {
            var uac = Integer.parseInt(attr.getStringValue());
            if ((uac & LOCKOUT) == LOCKOUT) {
                logger.warn("Account is disabled with UAC {} for entry {}", uac, ldapEntry);
                return null;
            }
            if ((uac & ACCOUNT_DISABLED) == ACCOUNT_DISABLED) {
                logger.warn("Account is disabled with UAC {} for entry {}", uac, ldapEntry);
                return null;
            }
        }
        return ldapEntry;
    }
}
