/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.util;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist

 */
public class PatternHelperTest extends TestCase {
    public void testCompilePattern() {
        var pattern1 = PatternHelper.compilePattern("*f.oo*ba.r*");
        assertEquals(".*\\Qf.oo\\E.*\\Qba.r\\E.*", pattern1.pattern());

        var pattern2 = PatternHelper.compilePattern("f.oo*ba.r*");
        assertEquals("\\Qf.oo\\E.*\\Qba.r\\E.*", pattern2.pattern());

        var pattern3 = PatternHelper.compilePattern("*f.oo*ba.r");
        assertEquals(".*\\Qf.oo\\E.*\\Qba.r\\E", pattern3.pattern());

        var pattern4 = PatternHelper.compilePattern("f.oo*ba.r");
        assertEquals("\\Qf.oo\\E.*\\Qba.r\\E", pattern4.pattern());

        var pattern5 = PatternHelper.compilePattern("f.ooba.r");
        assertEquals("\\Qf.ooba.r\\E", pattern5.pattern());
    }
}
