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
import org.jasig.services.persondir.support.BaseGroovyScriptDaoImpl

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
class SampleGroovyPersonAttributeDao extends BaseGroovyScriptDaoImpl {

    @Override
    Map<String, Object> getAttributesForUser(String uid) {
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }

    @Override
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes) {
        Map<String, List<Object>> newMap = new HashMap<>(attributes)
        newMap.put("foo", Arrays.asList(["value1", "value2"]))
        return newMap
    }

}