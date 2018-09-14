package org.apereo.services.persondir.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();
   
    private boolean caseInsensitiveUsername = false;
    private String url;
    private String basicAuthUsername;
    private String basicAuthPassword;
    private String method;

    public boolean isCaseInsensitiveUsername() {
        return caseInsensitiveUsername;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
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
    public IPersonAttributes getPerson(final String uid) {
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();

            if (StringUtils.isNotBlank(this.basicAuthUsername) && StringUtils.isNotBlank(this.basicAuthPassword)) {
                final CredentialsProvider provider = new BasicCredentialsProvider();
                final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(this.basicAuthUsername, this.basicAuthPassword);
                provider.setCredentials(AuthScope.ANY, credentials);
                builder.setDefaultCredentialsProvider(provider);
            }

            final HttpClient client = builder.build();

            final URIBuilder uriBuilder = new URIBuilder(this.url);
            uriBuilder.addParameter("username", uid);
            final URI uri = uriBuilder.build();
            final HttpUriRequest request = method.equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            final HttpResponse response = client.execute(request);
            final Map attributes = jacksonObjectMapper.readValue(response.getEntity().getContent(), Map.class);

            if (this.caseInsensitiveUsername) {
                return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoListValues(attributes));
            }
            return new NamedPersonImpl(uid, stuffAttributesIntoListValues(attributes));

        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoListValues(query));
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        final Set<IPersonAttributes> people = new LinkedHashSet<>();
        final String username = usernameAttributeProvider.getUsernameFromQuery(query);
        final IPersonAttributes person = getPerson(username);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames() {
        return Collections.EMPTY_SET;
    }

    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes() {
        return Collections.EMPTY_SET;
    }

    private static Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, ?> personAttributesMap) {
        final Map<String, List<Object>> personAttributes = new HashMap<>();

        for (final Map.Entry<String, ?> stringObjectEntry : personAttributesMap.entrySet()) {
            final Object value = stringObjectEntry.getValue();
            if (value instanceof List) {
                personAttributes.put(stringObjectEntry.getKey(), (List) value);
            } else {
                personAttributes.put(stringObjectEntry.getKey(), new ArrayList<>(Arrays.asList(value)));
            }
        }
        return personAttributes;
    }
}
