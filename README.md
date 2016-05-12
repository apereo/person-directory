Person Directory
===========================

## Intro
A framework for resolving persons and attributes from a variety of underlying sources. 
It consists of a collection of DAOs that retrieve, cache, resolve, aggregate, merge person attributes from JDBC, LDAP and more. 

## Maven

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.service.persondir/person-directory-parent/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo.service.persondir/person-directory-parent)

```xml
<dependency>
    <groupId>org.apereo.service.persondir</groupId>
    <artifactId>person-directory-api</artifactId>
    <version>${person.directory.version}</version>
</dependency>
<dependency>
    <groupId>org.apereo.service.persondir</groupId>
    <artifactId>person-directory-impl</artifactId>
    <version>${person.directory.version}</version>
</dependency>
```

## Configuration

### Attribute Caching

#### CachingPersonAttributeDaoImpl

Delegates queries to the configured child IPersonAttributeDao and caches the results based using a key 
generated from the query using attributes specified in the configuration.

Setting up a `CachingPersonAttributeDaoImpl` in Spring to would look like the following:

```xml
<bean id="cachingPersonAttributeDao" class="org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl">
    <property name="cachedPersonAttributesDao" ref="mergingPersonAttributeDao" />
    <property name="userInfoCache" ref="userInfoCacheMap" />
    <property name="cacheNullResults" value="true" />
</bean>
```

This configuration will cache results of method calls on mergingPersonAttributeDao in userInfoCacheMap.
The cache keys are generated using AttributeBasedCacheKeyGenerator by default and in the default configuration the username attribute is used for the key. Also null results will be cached, meaning if a query against `mergingPersonAttributeDao` is a miss or cannot be run (null returned) an marker will be cached to avoid repeated null lookups.

#### Configuration

| Property | Type | Default Value | Description |
| ---------|-------|----------|-------------- |
| defaultAttribute | String | username | The attribute name to use for calls to IPersonAttributes getPerson(String). 
A query Map is generated for these calls using the defaultAttribute and the value passed in.
| cachedPersonAttributesDao | IPersonAttributeDao | null | The IPersonAttributeDao to cache results from.
| cacheKeyGenerator | CacheKeyGenerator | `new AttributeBasedCacheKeyGenerator()` | An implementation of the Spring-Modules Caching CacheKeyGenerator API to use to generate cache keys. The use of this interface also allows AOP based caching using the CacheKeyGenerator implementation directly.
| userInfoCache | `Map<Serializable, Set<IPersonAttributes>>` | null | The cache to store results in. Only the get, set and remove methods are used on the Map interface so most commonly a wrapper around a real caching interface is used.
| cacheNullResults | boolean | false | If null results (meaning a child DAO could not complete the query) are returned should they be cached as well to avoid multiple 'failure' lookups.

#### AttributeBasedCacheKeyGenerator
Implements the Spring-Modules Cache CacheKeyGenerator API and is used by the CachingPersonAttributeDaoImpl to generate cache keys for queries.
 
| Property | Type | Default Value | Description |
| ---------|-------|----------|-------------- |
| cacheKeyAttributes | Set<String> | null | Query Map attributes to be used when building the cache key.
| defaultAttribute | String | username | The attribute name to use for calls to IPersonAttributes getPerson(String).


### Attribute Aggregation

Many instances require searching for users and retrieving attributes from multiple sources and merging those results. There are two options for using multiple attribute sources. MergingPersonAttributeDaoImpl allows multiple child IPersonAttributesAttributeDao implementations to be queried and merges their results into a single result set. CascadingPersonAttributeDao is similar but it folds the results of previous IPersonAttributeDaos into the query for the next IPersonAttributesAttributeDao in the list. Cascading is useful if an attribute from system A is needed to retrieve attributes from system B.

#### MergingPersonAttributeDaoImpl
Designed to query multiple IPersonAttributeDaos in order and merge the results into a single result set.
Setting up a MergingPersonAttributeDaoImpl in Spring to would look like the following:

```xml
<bean id="mergingPersonAttributeDao" class="org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl">
    <property name="personAttributeDaos">
        <list>
            <ref bean="jdbcPersonAttributeDao" />
            <ref bean="studentLdapPersonAttributeDao" />
            <ref bean="facStaffLdapPersonAttributeDao" />
        </list>
    </property>
</bean>
```

This configuration will query three IPersonAttributeDaos in order and merge their results using the default `IAttributeMerger` 
which is the `MultivaluedAttributeMerger`.


#### CascadingPersonAttributeDao
Designed to query multiple IPersonAttributeDaos in order and merge the results into a single result set. As each IPersonAttributesAttributeDao 
is queried the attributes from the first IPersonAttributes in the result set are used as the query for the next IPersonAttributesAttributeDao.
Setting up a `CascadingPersonAttributeDao` in Spring to would look like the following:

```xml
<bean id="mergingPersonAttributeDao" class="org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl">
    <property name="personAttributeDaos">
        <list>
            <ref bean="jdbcPersonAttributeDao" />
            <ref bean="studentLdapPersonAttributeDao" />
            <ref bean="facStaffLdapPersonAttributeDao" />
        </list>
    </property>
</bean>
```

This configuration will query three IPersonAttributeDaos in order and merge their results using the default `IAttributeMerger` which is the `ReplacingAttributeAdder`.

#### Configuration
`MergingPersonAttributeDaoImpl` has an anonymous constructor.

| Property | Type | Default Value | Description |
| ---------|-------|--------------|-------------- |
| defaultAttribute | String | username | The attribute name to use for calls to `IPersonAttributes getPerson(String)`. A query Map is generated for these calls using the defaultAttribute and the value passed in.
| personAttributeDaos | List<IPersonAttributesAttributeDao> | null | A List of `IPersonAttributeDaos` to be queried and have their results merged.
| attrMerger  | IAttributeMerger  | new ReplacingAttributeAdder() | The result set merging strategy to be used. See the Merging 
Strategies section for more information on available options.
| recoverExceptions  | boolean | true | If an exception thrown by a child IPersonAttributesAttributeDao


### Merging Strategies
Both merging daos use the IAttributeMerger to actually put the multiple results together. Person Directory ships with three implementations of this interface.

#### MultivaluedAttributeMerger
Merging of the Sets of IPersonAttributess is additive. For IPersonAttributess with the same name the person's attributes are merged into multi-valued lists.
As an example of this for two IPersonAttributess with the same name where:

- IPersonAttributes A has attributes {email=eric.dalquist@example.com, phone=123-456-7890}
- IPersonAttributes B has attributes {phone=[111-222-3333, 000-999-8888], office=3233}
- The resulting merged IPersonAttributes would have attributes: {email=eric.dalquist@example.com, phone=[123-456-7890, 111-222-3333, 000-999-8888], office=3233}

#### NoncollidingAttributeAdder
Merging of the Sets of IPersonAttributess is additive. For IPersonAttributess with the same name the person's 
attributes are merged such that only attributes on the second IPersonAttributes that don't already exist on the first IPersonAttributes are merged in.

As an example of this for two IPersonAttributess with the same name where:

- IPersonAttributes A has attributes {email=eric.dalquist@example.com, phone=123-456-7890}
- IPersonAttributes B has attributes {phone=[111-222-3333, 000-999-8888], office=3233}
- The resulting merged IPersonAttributes would have attributes: {email=eric.dalquist@example.com, phone=123-456-7890, office=3233}

#### ReplacingAttributeAdder
Merging of the Sets of IPersonAttributess is additive. For IPersonAttributess with the same name the person's 
attributes are merged such that attributes on the second IPersonAttributes replace any attributes with the same name on the first IPersonAttributes.
As an example of this for two IPersonAttributess with the same name where:

- IPersonAttributes A has attributes {email=eric.dalquist@example.com, phone=123-456-7890}
- IPersonAttributes B has attributes {phone=[111-222-3333, 000-999-8888], office=3233}
- The resulting merged IPersonAttributes would have attributes: {email=eric.dalquist@example.com, phone=[111-222-3333, 000-999-8888], office=3233}

### Request Header Attribute Source

The `RequestAttributeSourceFilter` provides the ability to use values from HttpServletRequest methods and headers as user attributes. 
The examples below store the attributes in the user's session. 

**Note**: The `AdditionalDescriptorsPersonAttributeDao` that actually provides the user attributes from the session should 
not be wrapped in a CachingPersonAttributeDaoImpl or any other cache, the attributes can change from request to request and the cache will hide this changes.


#### Examples

There are two ways to use the request attribute source. One requires the username be available as part of the request, 
the other requires that the application can provide the current username when running an attribute query.

**web.xml**: 

```xml
<!--
 | Use Spring's delegating proxy, uses the spring managed bean with the id that matches the filter-name
 +-->
<filter>
    <filter-name>requestAttributeSourceFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
 
<!--
 | Apply the filter to the URL that will receive the user attribute headers, likely /Login
 +-->
<filter-mapping>
    <filter-name>requestAttributeSourceFilter</filter-name>
    <url-pattern>/Login</url-pattern>
</filter-mapping>
```

**Example using username from Request**

The following example assumes the username is provided by `HttpServletRequest.getRemoteUser()`. Spring Configuration - Username from Request follows.

```xml
<!--
 | This is the actual servlet filter implementation. The delegating fitler proxy configured in web.xml will delegate
 | to this bean.
 |
 | In this example:
 |   - The getRemoteUser value is mapped to the 'username' attribute.
 |   - The getServerName value is mapped to the 'serverName' attribute.
 |   - The headerAttributeMapping declares two headers to turn into attributes (headerAttr1 and headerAttr2).
 |     headerAttr1 will appear as portalAttrName1 in the user's attributes.
 |     headerAttr2 will appear as portalAttr2Varient1 and portalAttr2Varient2 in the user's attributes.
 |   - processingPosition Tells the filter to store the user attributes in the session both before and after
 |     doFilter is called. This is useful when filtering around things like the uPortal login servlet which
 |     invalidates and re-creates the session during execution.
 +-->
<bean id="requestAttributeSourceFilter" class="org.apereo.services.persondir.support.web.RequestAttributeSourceFilter">
    <property name="additionalDescriptors" ref="requestAdditionalDescriptors" />
    <property name="remoteUserAttribute" value="username" />
    <property name="serverNameAttribute" value="serverName" />
    <property name="processingPosition" value="BOTH" />
    <property name="headerAttributeMapping">
        <map>
            <entry key="headerAttr1" value="portalAttrName1" />
            <entry key="headerAttr2">
                <set>
                    <value>portalAttr2Varient1</value>
                    <value>portalAttr2Varient2</value>
                </set>
            </entry>
        </map>
    </property>
    <property name="processingPosition" value="BOTH" />
</bean>
 
<!--
 | This object holds the user attributes set by the RequestAttributeSourceFilter for later retrieval. Since
 | these attributes are tied to the user's session the bean is declared in the globalSession scope and tagged
 | as an aop:scoped-proxy. The result of this is each user will get their own copy of this bean and the proxy
 | that classes referencing this bean will use will automatically find the correct instance from the current
 | user's session.
 +-->
<bean id="requestAttributeDescriptors" class="org.apereo.services.persondir.support.AdditionalDescriptors" scope="globalSession">
    <!-- Required so Spring injects an AOP proxy instead of the actual bean instance -->
    <aop:scoped-proxy/>
</bean>
 
<!--
 | The AdditionalDescriptorsPersonAttributeDao is what you would configure in the tree of IPersonAttributeDaos
 | used to get user attributes. It can be treated just like a JDBC or LDAP dao.
 +-->
<bean id="requestAttributesDao" class="org.apereo.services.persondir.support.AdditionalDescriptorsPersonAttributeDao">
    <property name="descriptors" ref="requestAdditionalDescriptors" />
    <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
</bean>
```

**Example using ICurrentUserProvider**

The following example assumes the username is not available from the request. In this case the application must 
implement the `ICurrentUserProvider` and inject it into the AdditionalDescriptorsPersonAttributeDao. This is required so the 
DAO knows for which queries to return the attributes from the request.

```xml
<!--
 | This is the actual servlet filter implementation. The delegating fitler proxy configured in web.xml will delegate
 | to this bean.
 |
 | In this example:
 |   - The getServerName value is mapped to the 'serverName' attribute.
 |   - The headerAttributeMapping declares two headers to turn into attributes (headerAttr1 and headerAttr2).
 |     headerAttr1 will appear as portalAttrName1 in the user's attributes.
 |     headerAttr2 will appear as portalAttr2Varient1 and portalAttr2Varient2 in the user's attributes.
 |   - processingPosition Tells the filter to store the user attributes in the session both before and after
 |     doFilter is called. This is useful when filtering around things like the uPortal login servlet which
 |     invalidates and re-creates the session during execution.
 +-->
<bean id="requestAttributeSourceFilter" class="org.apereo.services.persondir.support.web.RequestAttributeSourceFilter">
    <property name="additionalDescriptors" ref="requestAdditionalDescriptors" />
    <property name="serverNameAttribute" value="serverName" />
    <property name="processingPosition" value="BOTH" />
    <property name="headerAttributeMapping">
        <map>
            <entry key="headerAttr1" value="portalAttrName1" />
            <entry key="headerAttr2">
                <set>
                    <value>portalAttr2Varient1</value>
                    <value>portalAttr2Varient2</value>
                </set>
            </entry>
        </map>
    </property>
    <property name="processingPosition" value="BOTH" />
</bean>
 
<!--
 | This object holds the user attributes set by the RequestAttributeSourceFilter for later retrieval. Since
 | these attributes are tied to the user's session the bean is declared in the globalSession scope and tagged
 | as an aop:scoped-proxy. The result of this is each user will get their own copy of this bean and the proxy
 | that classes referencing this bean will use will automatically find the correct instance from the current
 | user's session.
 +-->
<bean id="requestAttributeDescriptors" class="org.apereo.services.persondir.support.AdditionalDescriptors" scope="globalSession">
    <!-- Required so Spring injects an AOP proxy instead of the actual bean instance -->
    <aop:scoped-proxy/>
</bean>
 
<!--
 | The AdditionalDescriptorsPersonAttributeDao is what you would configure in the tree of IPersonAttributeDaos
 | used to get user attributes. It can be treated just like a JDBC or LDAP dao.
 +-->
<bean id="requestAttributesDao" class="org.apereo.services.persondir.support.AdditionalDescriptorsPersonAttributeDao">
    <property name="descriptors" ref="requestAdditionalDescriptors" />
    <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
    <property name="currentUserProvider" ref="currentUserProvider" />
</bean>
```

** Example using username from Request and handling session invalidation**

The following example assumes the username is provided by `HttpServletRequest.getRemoteUser()`. This example also handles 
session invalidation which happens during the login request on some applications, including uPortal. The additional attributes 
are stored the current request as well as the session ensuring that they are available
at every point during the request.

```xml
<!--
 | This is the actual servlet filter implementation. The delegating fitler proxy configured in web.xml will delegate
 | to this bean.
 |
 | In this example:
 |   - The getRemoteUser value is mapped to the 'username' attribute.
 |   - The getServerName value is mapped to the 'serverName' attribute.
 |   - The headerAttributeMapping declares two headers to turn into attributes (headerAttr1 and headerAttr2).
 |     headerAttr1 will appear as portalAttrName1 in the user's attributes.
 |     headerAttr2 will appear as portalAttr2Varient1 and portalAttr2Varient2 in the user's attributes.
 |   - processingPosition Tells the filter to store the user attributes in the session both before and after
 |     doFilter is called. This is useful when filtering around things like the uPortal login servlet which
 |     invalidates and re-creates the session during execution.
 +-->
<bean id="requestAttributeSourceFilter" class="org.apereo.services.persondir.support.web.RequestAttributeSourceFilter">
    <property name="additionalDescriptors" ref="requestAdditionalDescriptors" />
    <property name="remoteUserAttribute" value="username" />
    <property name="serverNameAttribute" value="serverName" />
    <property name="processingPosition" value="BOTH" />
    <property name="headerAttributeMapping">
        <map>
            <entry key="headerAttr1" value="portalAttrName1" />
            <entry key="headerAttr2">
                <set>
                    <value>portalAttr2Varient1</value>
                    <value>portalAttr2Varient2</value>
                </set>
            </entry>
        </map>
    </property>
    <property name="processingPosition" value="BOTH" />
</bean>
 
<!--
 | Delegates to two data holding AdditionalDescriptors objects. The first is session scoped, used to store
 | the attributes for the duration of the user's session. The second is request scoped, ensuring that on the
 | request that the attributes were provided if the session is invalidated the attributes will still be
 | available.
 +-->
<bean id="requestAdditionalDescriptors" class="org.apereo.services.persondir.support.MediatingAdditionalDescriptors">
    <property name="delegateDescriptors">
        <list>
            <bean class="org.apereo.services.persondir.support.AdditionalDescriptors" scope="globalSession">
                <aop:scoped-proxy />
            </bean>
            <bean class="org.apereo.services.persondir.support.AdditionalDescriptors" scope="request">
                <aop:scoped-proxy />
            </bean>
        </list>
    </property>
</bean>
 
<!--
 | The AdditionalDescriptorsPersonAttributeDao is what you would configure in the tree of IPersonAttributeDaos
 | used to get user attributes. It can be treated just like a JDBC or LDAP dao.
 +-->
<bean id="requestAttributesDao" class="org.apereo.services.persondir.support.AdditionalDescriptorsPersonAttributeDao">
    <property name="descriptors" ref="requestAdditionalDescriptors" />
    <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
</bean>
```
