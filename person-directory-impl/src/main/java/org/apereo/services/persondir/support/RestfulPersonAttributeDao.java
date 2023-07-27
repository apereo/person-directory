package org.apereo.services.persondir.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Provides the ability to contact a URL resource to ask for attributes.
 * Support GET/POST endpoints, and provides the username in form of a parameter.
 * The response is expected to be a JSON map.
 *
 * @author Misagh Moayyed
 */
public class RestfulPersonAttributeDao extends BasePersonAttributeDao {
    private final ObjectMapper jacksonObjectMapper = new ObjectMapper().findAndRegisterModules();
    private IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();
   
    private boolean caseInsensitiveUsername = false;
    private String url;
    private String basicAuthUsername;
    private String basicAuthPassword;
    private String method;
    private String principalId = "username";
    private Map<String, String> parameters = new LinkedHashMap<>();
    private Map<String, String> headers = new LinkedHashMap<>();

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public IUsernameAttributeProvider getUsernameAttributeProvider() {
        return usernameAttributeProvider;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(final Map<String, String> headers) {
        this.headers = headers;
    }

    public void setUsernameAttributeProvider(final IUsernameAttributeProvider usernameAttributeProvider) {
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    public boolean isCaseInsensitiveUsername() {
        return caseInsensitiveUsername;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(final String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(final String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final Set<IPersonAttributes> resultPeople, final IPersonAttributeDaoFilter filter) {
        try {
            if (!this.isEnabled()) {
                return null;
            }
            var client = new OkHttpClient.Builder().build();

            var uriBuilder = HttpUrl.parse(this.url).newBuilder();
            uriBuilder.addQueryParameter(principalId, Objects.requireNonNull(uid, principalId + " cannot be null"));
            this.parameters.forEach(uriBuilder::addQueryParameter);

            var uri = uriBuilder.build();
            var requestBuilder = new Request.Builder().url(uri);
            requestBuilder = method.equalsIgnoreCase(HttpMethod.GET.name()) ? requestBuilder.get() : requestBuilder.post(RequestBody.create(new byte[0]));
            this.headers.forEach(requestBuilder::addHeader);

            if (StringUtils.isNotBlank(this.basicAuthUsername) && StringUtils.isNotBlank(this.basicAuthPassword)) {
                var credentials = Credentials.basic(this.basicAuthUsername, this.basicAuthPassword);
                requestBuilder.addHeader("Authorization", credentials);
            }

            var request = requestBuilder.build();

            try (var response = client.newCall(request).execute()) {
                var attributes = jacksonObjectMapper.readValue(response.body().byteStream(), Map.class);

                if (this.caseInsensitiveUsername) {
                    return new CaseInsensitiveNamedPersonImpl(uid, MultivaluedPersonAttributeUtils.stuffAttributesIntoListValues(attributes, filter));
                }
                return new NamedPersonImpl(uid, MultivaluedPersonAttributeUtils.stuffAttributesIntoListValues(attributes, filter));
            }

        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query,
                                            final IPersonAttributeDaoFilter filter,
                                            final Set<IPersonAttributes> resultPeople) {
        return getPeopleWithMultivaluedAttributes(MultivaluedPersonAttributeUtils.stuffAttributesIntoListValues(query, filter), filter, resultPeople);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter,
                                                                     final Set<IPersonAttributes> resultPeople) {
        var people = new LinkedHashSet<IPersonAttributes>();
        var username = usernameAttributeProvider.getUsernameFromQuery(query);
        var person = getPerson(username, resultPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return Collections.EMPTY_SET;
    }

}
