package org.apereo.services.persondir.support.cache;

import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;

/**
 * <p>
 * Generates a unique key based on the description of an invocation to an
 * intercepted method.
 * </p>
 *
 * @author Alex Ruiz
 */
public interface CacheKeyGenerator {

    /**
     * Generates the key for a cache entry.
     *
     * @param methodInvocation
     *          the description of an invocation to the intercepted method.
     * @return the created key.
     */
    Serializable generateKey(MethodInvocation methodInvocation);
}
