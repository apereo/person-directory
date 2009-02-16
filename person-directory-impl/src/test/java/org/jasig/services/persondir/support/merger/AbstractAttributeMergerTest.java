/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.merger;

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

/**
 * Abstract test for the IAttributeMerger interface.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public abstract class AbstractAttributeMergerTest extends TestCase {

    /**
     * Test that attempting to merge attributes into a null Map results in
     * an illegal argument exception.
     */
    public void testNullToModify() {
        try {
            getAttributeMerger().mergeAttributes(null, new HashMap<String, List<Object>>());
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have thrown IAE on null argument.");
    }
    
    /**
     * Test that attempting to merge attributes into a null Map results in
     * an illegal argument exception.
     */
    public void testNullToConsider() {
        try {
            getAttributeMerger().mergeAttributes(new HashMap<String, List<Object>>(), null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have thrown IAE on null argument.");
    }
    
    protected abstract IAttributeMerger getAttributeMerger();
    
}