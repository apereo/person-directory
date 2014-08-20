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
package org.jasig.services.persondir.util;


import java.util.Locale;

import org.apache.commons.lang.StringUtils;

public enum CaseCanonicalizationMode {

    LOWER {
        @Override
        public String canonicalize(String value) {
            return StringUtils.lowerCase(value);
        }

        @Override
        public String canonicalize(String value, Locale locale) {
            return StringUtils.lowerCase(value, locale);
        }
    },
    UPPER {
        @Override
        public String canonicalize(String value) {
            return StringUtils.upperCase(value);
        }

        @Override
        public String canonicalize(String value, Locale locale) {
            return StringUtils.upperCase(value, locale);
        }
    },
    NONE {
        @Override
        public String canonicalize(String value) {
            return value;
        }

        @Override
        public String canonicalize(String value, Locale locale) {
            return value;
        }
    };

    public abstract String canonicalize(String value);
    public abstract String canonicalize(String value, Locale locale);
}
