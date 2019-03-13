package org.apereo.services.persondir;

/**
 * This is {@link IPersonAttributeDaoFilter}.
 *
 * @author Misagh Moayyed
 */
@FunctionalInterface
public interface IPersonAttributeDaoFilter {
    boolean choosePersonAttributeDao(IPersonAttributeDao personAttributeDao);

    static IPersonAttributeDaoFilter alwaysChoose() {
        return personAttributeDao -> true;
    }
}
