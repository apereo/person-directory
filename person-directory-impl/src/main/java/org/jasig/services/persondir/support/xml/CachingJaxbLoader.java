/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.xml;

/**
 * JAXB Object unmarshalling and caching service
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <T>
 */
public interface CachingJaxbLoader<T> {

    /**
     * @param callback Callback that will be executed after the object is unmarshalled (if it needs to be) but before it is returned
     * @see #getUnmarshalledObject() 
     */
    public T getUnmarshalledObject(UnmarshallingCallback<T> callback);
    
    /**
     * Loads and unmarshalls the XML as needed, returning the unmarshalled object
     */
    public T getUnmarshalledObject();

    public interface UnmarshallingCallback<T> {
        /**
         * Allow for specific handling of of the unmarshalled object before it is returned by a call to
         * {@link JaxbLoader#getUnmarshalledObject(UnmarshallingCallback)} that triggered a reload. If
         * this method throws an exception the loaded object will not be cached and the exception will
         * be propegated to the caller of {@link JaxbLoader#getUnmarshalledObject(UnmarshallingCallback)}.
         */
        public void postProcessUnmarshalling(T unmarshalledObject);
    }
}