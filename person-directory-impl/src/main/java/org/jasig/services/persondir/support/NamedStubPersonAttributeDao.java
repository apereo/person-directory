package org.jasig.services.persondir.support;

import org.jasig.services.persondir.IPersonAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extension of the {@link StubPersonAttributeDao} that is able to identify itself
 * by populating the backing map with the received username. This allows for static attributes
 * to be merged with other DAOs via {@link org.jasig.services.persondir.support.MergingPersonAttributeDaoImpl}.
 * Without the unique identifier that is username, the merge would fail resulting in two distinct attribute sets
 * for the same principal in the ultimate attribute map.
 * @author Misagh Moayyed
 */
public class NamedStubPersonAttributeDao extends StubPersonAttributeDao {

    public NamedStubPersonAttributeDao() {
        super();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public NamedStubPersonAttributeDao(final Map backingMap) {
        super(backingMap);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {

        final List<?> list = query.get("username");
        final Map m = new HashMap(this.getBackingMap());

        m.put("username", list);

        this.setBackingMap(m);
        return super.getPeopleWithMultivaluedAttributes(query);
    }
}
