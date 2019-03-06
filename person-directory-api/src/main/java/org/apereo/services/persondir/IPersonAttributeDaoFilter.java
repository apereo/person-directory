package org.apereo.services.persondir;

/**
 * This is {@link IPersonAttributeDaoFilter}.
 *
 * @author Misagh Moayyed
 */
public interface IPersonAttributeDaoFilter {
    boolean choosePersonAttributeDao(IPersonAttributeDao personAttributeDao);
}
