package org.apereo.services.persondir.support.ldap;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.handler.AbstractEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
     * Password expired flag.
     */
    public static final int PASSWORD_EXPIRED = 0x00800000;

    /**
     * Logger instance.
     **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String decodeLogonBits(byte b) {
        var sb = new StringBuilder();
        sb.append((b & 0x01) > 0 ? "1" : "0");
        sb.append((b & 0x02) > 0 ? "1" : "0");
        sb.append((b & 0x04) > 0 ? "1" : "0");
        sb.append((b & 0x08) > 0 ? "1" : "0");
        sb.append((b & 0x10) > 0 ? "1" : "0");
        sb.append((b & 0x20) > 0 ? "1" : "0");
        sb.append((b & 0x40) > 0 ? "1" : "0");
        sb.append((b & 0x80) > 0 ? "1" : "0");
        return sb.toString();
    }

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
            if ((uac & PASSWORD_EXPIRED) == PASSWORD_EXPIRED) {
                logger.warn("Account has expired");
                return null;
            }
        }

        var accountExpires = ldapEntry.getAttribute("accountExpires");
        if (accountExpires != null) {
            var adDate = Long.parseLong(ldapEntry.getAttribute("accountExpires").getStringValue());
            logger.debug("Current active directory account expiration date {}", adDate);
            if (adDate > 0) {
                var cal = new GregorianCalendar(TimeZone.getDefault());
                cal.set(1601, 0, 1, 0, 0);

                var converted = adDate / 10000;
                var timeStamp = Long.valueOf(converted + cal.getTime().getTime());
                var date = new Date(timeStamp);
                var accountExpiresDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                var now = LocalDateTime.now();
                logger.debug("Now: {}, account expires at {}", now, accountExpiresDate);
                if (accountExpiresDate.isBefore(now)) {
                    logger.warn("Account has expired with date {}", accountExpiresDate);
                    return null;
                }
            }
        }

        if (!isValidLogonHour(ldapEntry)) {
            logger.warn("Logon Hours are invalid and no attributes will be used");
            return null;
        }
        return ldapEntry;
    }

    protected boolean isValidLogonHour(final LdapEntry attr) {
        if (attr.getAttribute("logonHours") != null) {
            byte[] raw = attr.getAttribute("logonHours").getBinaryValue();

            DayOfWeek[] days = new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
            var ret = new ArrayList<String>();

            for (int day = 0; day < days.length; day++) {
                byte[] vBits;
                if (day == 6) {
                    vBits = new byte[]{raw[19], raw[20], raw[0]};
                } else {
                    vBits = new byte[]{raw[day * 3], raw[day * 3 + 1], raw[day * 3 + 2]};
                }

                var sb = new StringBuilder();
                for (int b = 0; b < 3; b++) {
                    sb.append(decodeLogonBits(vBits[b]));
                }
                ret.add(sb.toString());
            }

            var result = new String[ret.size()];
            ret.toArray(result);

            var currentDay = LocalDate.now().getDayOfWeek();
            var currentHour = LocalDateTime.now().getHour() - 1;
            if (currentHour < 0) {
                currentHour = 0;
            }
            logger.debug("Current day {}, current hour {}", currentDay, currentHour);
            for (int day = 0; day < days.length; day++) {
                if (days[day] == currentDay) {
                    var validHours = result[day];
                    logger.debug("Valid hours are {}", validHours);
                    var hourEnabled = String.valueOf(validHours.charAt(currentHour));
                    logger.debug("Hour enabled at {} is {}", currentHour, hourEnabled);
                    if (!hourEnabled.equalsIgnoreCase("1")) {
                        logger.warn("Invalid login hour");
                        return false;
                    }
                }
            }

            return true;
        }
        return true;
    }
}
