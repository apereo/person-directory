/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PatternHelper {
    /**
     * Converts a String using the {@link IPersonAttributeDao#WILDCARD} into a valid regular expression
     * {@link Pattern} with the {@link IPersonAttributeDao#WILDCARD} replaced by .* and the rest of the
     * string escaped using {@link Pattern#quote(String)}
     */
    public static Pattern compilePattern(String queryString) {
        final StringBuilder queryBuilder = new StringBuilder();
        
        final Matcher queryMatcher = IPersonAttributeDao.WILDCARD_PATTERN.matcher(queryString);
        
        if (!queryMatcher.find()) {
            return Pattern.compile(Pattern.quote(queryString));
        }
        
        int start = queryMatcher.start();
        int previousEnd = -1;
        if (start > 0) {
            final String queryPart = queryString.substring(0, start);
            final String quotedQueryPart = Pattern.quote(queryPart);
            queryBuilder.append(quotedQueryPart);
        }
        queryBuilder.append(".*");

        do {
            start = queryMatcher.start();
            
            if (previousEnd != -1) {
                final String queryPart = queryString.substring(previousEnd, start);
                final String quotedQueryPart = Pattern.quote(queryPart);
                queryBuilder.append(quotedQueryPart);
                queryBuilder.append(".*");
            }
            
            previousEnd = queryMatcher.end();
        } while (queryMatcher.find());
        
        if (previousEnd < queryString.length()) {
            final String queryPart = queryString.substring(previousEnd);
            final String quotedQueryPart = Pattern.quote(queryPart);
            queryBuilder.append(quotedQueryPart);
        }
        
        return Pattern.compile(queryBuilder.toString());
    }
}
