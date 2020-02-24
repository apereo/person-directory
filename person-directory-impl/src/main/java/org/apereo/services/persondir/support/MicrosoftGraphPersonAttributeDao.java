package org.apereo.services.persondir.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.moshi.Json;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.springframework.util.ReflectionUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the ability to fetch attributes from azure active directory
 * using the graph api.
 *
 * @author Misagh Moayyed
 */
public class MicrosoftGraphPersonAttributeDao extends BasePersonAttributeDao {
    private final IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private boolean caseInsensitiveUsername;

    private String tenant;

    private String resource = "https://graph.microsoft.com/";

    private String scope;

    private String grantType = "client_credentials";

    private String clientId;

    private String clientSecret;

    private String properties;

    private String apiBaseUrl = "https://graph.microsoft.com/v1.0/";

    private String loginBaseUrl = "https://login.microsoftonline.com/%s/";

    private String domain;

    /**
     * NONE,BASIC,HEADERS or BODY.
     */
    private String loggingLevel = "BASIC";

    private static Map<String, List<Object>> stuffAttributesIntoListValues(final Map<String, ?> personAttributesMap,
                                                                           final IPersonAttributeDaoFilter filter) {
        final Map<String, List<Object>> personAttributes = new HashMap<>();

        for (final Map.Entry<String, ?> stringObjectEntry : personAttributesMap.entrySet()) {
            final Object value = stringObjectEntry.getValue();
            if (value instanceof List) {
                personAttributes.put(stringObjectEntry.getKey(), (List<Object>) value);
            } else {
                personAttributes.put(stringObjectEntry.getKey(), new ArrayList<>(Arrays.asList(value)));
            }
        }
        return personAttributes;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public String getLoginBaseUrl() {
        return loginBaseUrl;
    }

    public void setLoginBaseUrl(final String loginBaseUrl) {
        this.loginBaseUrl = loginBaseUrl;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(final String loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(final String tenant) {
        this.tenant = tenant;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(final String grantType) {
        this.grantType = grantType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public IUsernameAttributeProvider getUsernameAttributeProvider() {
        return usernameAttributeProvider;
    }

    public boolean isCaseInsensitiveUsername() {
        return caseInsensitiveUsername;
    }

    public void setCaseInsensitiveUsername(final boolean caseInsensitiveUsername) {
        this.caseInsensitiveUsername = caseInsensitiveUsername;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(final String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        try {
            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(this.loggingLevel.toUpperCase()));

            final String token = getToken();
            final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    final Request request = chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .build();
            final Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(this.apiBaseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build();

            final GraphApiService service = retrofit.create(GraphApiService.class);
            final String user = this.domain == null ? uid : uid + "@" + this.domain;
            final Call<User> call = service.getUserByUserPrincipalName(user,
                StringUtils.defaultIfBlank(this.properties,
                    User.getDefaultFieldQuery().stream().collect(Collectors.joining(","))));

            final Response<User> r = call.execute();
            if (r.isSuccessful()) {
                final User response = r.body();
                final Map<String, Object> attributes = response.buildAttributes();
                if (this.caseInsensitiveUsername) {
                    return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoListValues(attributes, filter));
                }
                return new NamedPersonImpl(uid, stuffAttributesIntoListValues(attributes, filter));
            }
            throw new RuntimeException("error requesting token (" + r.code() + "): " + r.errorBody());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query,
                                            final IPersonAttributeDaoFilter filter) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoListValues(query, filter), filter);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final IPersonAttributeDaoFilter filter) {
        final Set<IPersonAttributes> people = new LinkedHashSet<>();
        final String username = usernameAttributeProvider.getUsernameFromQuery(query);
        final IPersonAttributes person = getPerson(username, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    @Override
    @JsonIgnore
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return Collections.emptySet();
    }

    @Override
    @JsonIgnore
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return Collections.emptySet();
    }

    private String getToken() throws Exception {
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(this.loggingLevel.toUpperCase()));

        final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

        final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format(this.loginBaseUrl, this.tenant))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build();
        final GraphAuthApiService service = retrofit.create(GraphAuthApiService.class);
        final Response<OAuthTokenInfo> response = service.getOauth2Token(
            this.grantType,
            this.clientId,
            this.clientSecret,
            this.scope,
            this.resource)
            .execute();
        if (response.isSuccessful()) {
            final OAuthTokenInfo info = response.body();
            return info.accessToken;
        }
        final ResponseBody errorBody = response.errorBody();
        throw new RuntimeException("error requesting token (" + response.code() + "): " + errorBody);
    }

    private interface GraphApiService {
        @GET("users/{upn}")
        Call<User> getUserByUserPrincipalName(@Path("upn") String upn, @Query(value = "$select", encoded = true) String selectQuery);
    }

    private interface GraphAuthApiService {
        @FormUrlEncoded
        @POST("oauth2/token")
        Call<OAuthTokenInfo> getOauth2Token(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("scope") String scope,
            @Field("resource") String resource
        );
    }

    public static class User implements Serializable {
        private static final long serialVersionUID = 8497244140827305607L;

        public String userPrincipalName;

        public String id;

        public boolean accountEnabled;

        public String displayName;

        public String mail;

        public String jobTitle;

        public String officeLocation;

        public String preferredLanguage;

        public String mobilePhone;

        public String surname;

        public String givenName;

        public String passwordPolicies;

        public String preferredName;

        public List<String> businessPhones = new ArrayList<>(0);

        public List<String> schools = new ArrayList<>(0);

        public List<String> skills = new ArrayList<>(0);

        private String postalCode;

        private String consentProvidedForMinor;

        private String aboutMe;

        private String streetAddress;

        private String userType;

        private String usageLocation;

        private String state;

        private String ageGroup;

        private String otherMails;

        private String city;

        private String country;

        private String countryName;

        private String department;

        private String employeeId;

        private String faxNumber;

        private String mailNickname;

        @JsonIgnore
        static String getFieldQuery() {
            final List<String> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(User.class, field -> {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    fields.add(field.getName());
                }
            });
            return fields.stream().collect(Collectors.joining(","));
        }

        static List<String> getDefaultFieldQuery() {
            return Arrays.asList("businessPhones,displayName,givenName,id,"
                + "jobTitle,mail,givenName,employeeId,"
                + "mobilePhone,officeLocation,accountEnabled"
                + "preferredLanguage,surname,userPrincipalName");
        }

        @JsonIgnore
        private Map<String, Object> buildAttributes() {
            final Map<String, Object> fields = new HashMap<>();
            ReflectionUtils.doWithFields(getClass(), field -> {
                field.setAccessible(true);
                fields.put(field.getName(), field.get(User.this));
            });
            return fields;
        }
    }

    private static class OAuthTokenInfo implements Serializable {
        private static final long serialVersionUID = -8586825191767772463L;

        @Json(name = "token_type")
        public String tokenType;

        @Json(name = "scope")
        public String scope;

        @Json(name = "expires_in")
        public int expiresIn;

        @Json(name = "expires_on")
        public int expiresOn;

        @Json(name = "not_before")
        public int notBefore;

        @Json(name = "resource")
        public String resource;

        @Json(name = "access_token")
        public String accessToken;
    }
}
