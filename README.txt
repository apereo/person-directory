Person Directory Information acquired from various sources (LDAP, XML File, etc....) through a common interface.
The name of the project should have been in lower case but this became historical due to my request.
Until the maven site has all the content to describe the usage of persondir information this readme has the syntax for usage.

Basically 

		Hashtable env = new Hashtable();        
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://mrfrumble.its.yale.edu:389/o=yale.edu");       
        ldapContext = (LdapContext) new InitialLdapContext(env,null);         
        LdapPersonAttributeDaoImpl impl = new LdapPersonAttributeDaoImpl();        
        
        // This is assigned the LDAP context in which to work with.  This context can come from JVM -D parameters, the jndi context,  or from 
		// custom configuration if you manually construct the properties Hashtable as above.

        impl.setLdapContext(ldapContext);
        
        
        