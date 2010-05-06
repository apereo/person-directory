/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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