/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.services.persondir.support;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base {@link IPersonAttributeDao} that provides implementations of the deprecated methods.
 *
 * @author Eric Dalquist
 */
public abstract class BasePersonAttributeDao implements IPersonAttributeDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int order;

    private String[] id = new String[]{getClass().getSimpleName()};

    private boolean enabled = true;

    private Map<String, Object> tags = new LinkedHashMap<>();

    public BasePersonAttributeDao() {
        super();
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @Override
    public int compareTo(final IPersonAttributeDao o) {
        if (this.order == o.getOrder()) {
            return 0;
        }
        if (this.order > o.getOrder()) {
            return 1;
        }
        return -1;
    }

    @Override
    public String[] getId() {
        return this.id;
    }

    public void setId(final String... id) {
        this.id = id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(final Map<String, Object> tags) {
        this.tags = tags;
    }

    /**
     * Put tag into this DAO and override/remove existing tags by name.
     *
     * @param name  the name
     * @param value the value
     * @return the base person attribute dao
     */
    public BasePersonAttributeDao putTag(final String name, final Object value) {
        this.tags.put(name, value);
        return this;
    }

    protected IPersonAttributes getSinglePerson(final Set<IPersonAttributes> people) {
        IPersonAttributes person;
        try {
            person = DataAccessUtils.singleResult(people);
        } catch (final IncorrectResultSizeDataAccessException e) {
            logger.warn("Unexpected multiple people returned from person attribute DAO: {} : {} ", e.getClass().getName(), e.getMessage());
            people.forEach(p -> logger.debug("Person: {}", p));
            throw e;
        }
        return person;
    }
}
