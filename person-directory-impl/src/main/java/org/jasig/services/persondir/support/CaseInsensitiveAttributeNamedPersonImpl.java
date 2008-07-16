package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.CaseInsensitiveMap;
import org.apache.commons.collections15.map.ListOrderedMap;

/**
 * Custom IPersonAttributes that uses a case insensitive Map to hide attribute name case
 */
public class CaseInsensitiveAttributeNamedPersonImpl extends AttributeNamedPersonImpl {
    private static final long serialVersionUID = 1L;

    public CaseInsensitiveAttributeNamedPersonImpl(Map<String, List<Object>> attributes) {
        super(attributes);
    }

    public CaseInsensitiveAttributeNamedPersonImpl(String userNameAttribute, Map<String, List<Object>> attributes) {
        super(userNameAttribute, attributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.BasePersonImpl#createImmutableAttributeMap(int)
     */
    @Override
    protected Map<String, List<Object>> createImmutableAttributeMap(int size) {
        return ListOrderedMap.decorate(new CaseInsensitiveMap<List<Object>>(size > 0 ? size : 1));
    }
}