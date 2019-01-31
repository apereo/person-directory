package org.apereo.services.persondir.support.merger;

import java.util.List;
import java.util.Map;

/**
 * This is {@link ReturnOriginalAdditiveAttributeMerger}.
 *
 * @author Misagh Moayyed
 */
public class ReturnOriginalAdditiveAttributeMerger extends BaseAdditiveAttributeMerger {
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        return toModify;
    }
}
