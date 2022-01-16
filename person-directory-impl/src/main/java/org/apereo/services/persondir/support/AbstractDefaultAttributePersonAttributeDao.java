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
package org.apereo.services.persondir.support;

import org.apache.commons.lang3.Validate;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Abstract class implementing the IPersonAttributeDao method  {@link IPersonAttributeDao#getPerson(String, IPersonAttributeDaoFilter)}
 * by delegation to {@link IPersonAttributeDao#getPeopleWithMultivaluedAttributes(Map, IPersonAttributeDaoFilter)} using a configurable
 * default attribute name. If {@link IPersonAttributeDao#getPeopleWithMultivaluedAttributes(Map, IPersonAttributeDaoFilter)} returnes
 * more than one {@link IPersonAttributes} is returned {@link org.springframework.dao.IncorrectResultSizeDataAccessException} is thrown.
 *
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th>Property</th>
 *         <th>Description</th>
 *         <th>Required</th>
 *         <th>Default</th>
 *     </tr>
 *     <tr>
 *         <td  valign="top">usernameAttributeProvider</td>
 *         <td>
 *             The provider used to determine the username attribute to use when no attribute is specified in the query. This
 *             is primarily used for calls to {@link #getPerson(String, IPersonAttributeDaoFilter)}.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">{@link SimpleUsernameAttributeProvider}</td>
 *     </tr>
 * </table>
 *
 * @author Eric Dalquist

 * @since uPortal 2.5
 */
public abstract class AbstractDefaultAttributePersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
    private IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    public AbstractDefaultAttributePersonAttributeDao() {
        super();
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        Validate.notNull(uid, "uid may not be null.");

        //Generate the seed map for the uid
        var seed = this.toSeedMap(uid);

        //Run the query using the seed
        var people = this.getPeopleWithMultivaluedAttributes(seed, filter);

        //Ensure a single result is returned
        var person = getSinglePerson(people);
        if (person == null) {
            return null;
        }

        //Force set the name of the returned IPersonAttributes if it isn't provided in the return object
        if (person.getName() == null) {
            person = new NamedPersonImpl(uid, person.getAttributes());
        }

        return person;
    }


    /**
     * Converts the uid to a multi-valued seed Map using the value from {@link #getUsernameAttributeProvider()}
     * as the key.
     *
     * @param uid userId
     * @return multi-valued seed Map containing the uid
     */
    protected Map<String, List<Object>> toSeedMap(final String uid) {
        var values = Collections.singletonList((Object) uid);
        var usernameAttribute = this.usernameAttributeProvider.getUsernameAttribute();
        var seed = Collections.singletonMap(usernameAttribute, values);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Created seed map='" + seed + "' for uid='" + uid + "'");
        }
        return seed;
    }


    public IUsernameAttributeProvider getUsernameAttributeProvider() {
        return this.usernameAttributeProvider;
    }

    /**
     * The {@link IUsernameAttributeProvider} to use for determining the username attribute
     * to use when none is provided. The provider is used when calls are made to {@link #getPerson(String, org.apereo.services.persondir.IPersonAttributeDaoFilter)}
     * to build a query Map and then call {@link #getPeopleWithMultivaluedAttributes(Map, org.apereo.services.persondir.IPersonAttributeDaoFilter)}
     *
     * @param usernameAttributeProvider the usernameAttributeProvider to set
     */
    public void setUsernameAttributeProvider(final IUsernameAttributeProvider usernameAttributeProvider) {
        Validate.notNull(usernameAttributeProvider);
        this.usernameAttributeProvider = usernameAttributeProvider;
    }
}
