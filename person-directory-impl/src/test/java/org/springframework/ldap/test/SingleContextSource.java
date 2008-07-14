/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.springframework.ldap.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.transaction.compensating.LdapTransactionUtils;

/**
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class SingleContextSource implements ContextSource {
    private final DirContext ctx;

    public SingleContextSource(DirContext ctx) {
        this.ctx = ctx;
    }

    /*
     * @see org.springframework.ldap.ContextSource#getReadOnlyContext()
     */
    public DirContext getReadOnlyContext() throws NamingException {
        return getNonClosingDirContextProxy(ctx);
    }

    /*
     * @see org.springframework.ldap.ContextSource#getReadWriteContext()
     */
    public DirContext getReadWriteContext() throws NamingException {
        return getNonClosingDirContextProxy(ctx);
    }

    private DirContext getNonClosingDirContextProxy(DirContext context) {
        return (DirContext) Proxy.newProxyInstance(DirContextProxy.class.getClassLoader(),
                new Class[] { LdapTransactionUtils.getActualTargetClass(context), DirContextProxy.class },
                new NonClosingDirContextInvocationHandler(context));

    }
    
    public static class NonClosingDirContextInvocationHandler implements InvocationHandler {
        private final DirContext target;

        public NonClosingDirContextInvocationHandler(DirContext target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String methodName = method.getName();
            if (methodName.equals("getTargetContext")) {
                return target;
            }
            else if (methodName.equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            }
            else if (methodName.equals("hashCode")) {
                // Use hashCode of Connection proxy.
                return new Integer(proxy.hashCode());
            }
            else if (methodName.equals("close")) {
                // Never close the target context, as this class will only be
                // used for operations concerning the compensating transactions.
                return null;
            }

            try {
                return method.invoke(target, args);
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
