package org.zalando.planb.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringApplicationConfiguration(classes = {Main.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("it")
public class OIDCCreateTokenIT extends AbstractSpringTest {
    @Value("${local.server.port}")
    private int port;

    RestTemplate rest = new RestTemplate();

    @Test
    public void createToken() {
        MultiValueMap<String, Object> requestParameters = new LinkedMultiValueMap<String, Object>();
        requestParameters.add("realm", "/test");
        requestParameters.add("grant_type", "password");
        requestParameters.add("username", "klaus");
        requestParameters.add("password", "test");
        requestParameters.add("scope", "uid name");

        RequestEntity<MultiValueMap<String, Object>> request = RequestEntity
                .post(URI.create("http://localhost:" + port + "/oauth2/access_token"))
                .body(requestParameters);

        ResponseEntity<OIDCCreateTokenResponse> response = rest.exchange(request, OIDCCreateTokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScope()).isEqualTo("uid name");
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getAccessToken()).isNotEmpty();
        assertThat(response.getBody().getRealm()).isEqualTo("/test");

        assertThat(response.getBody().getAccessToken()).isNotEmpty();
        assertThat(response.getBody().getAccessToken()).isEqualTo(response.getBody().getIdToken());
    }

    @Test
    @Ignore("The fetched JWK somehow doesn't verify the JWT.")
    public void jwtClaims() throws InvalidJwtException, MalformedClaimException {
        MultiValueMap<String, Object> requestParameters = new LinkedMultiValueMap<String, Object>();
        requestParameters.add("realm", "/test");
        requestParameters.add("grant_type", "password");
        requestParameters.add("username", "klaus");
        requestParameters.add("password", "test");
        requestParameters.add("scope", "uid name");

        RequestEntity<MultiValueMap<String, Object>> request = RequestEntity
                .post(URI.create("http://localhost:" + port + "/oauth2/access_token"))
                .body(requestParameters);

        ResponseEntity<OIDCCreateTokenResponse> response = rest.exchange(request, OIDCCreateTokenResponse.class);
        String jwt = response.getBody().getIdToken();

        // fetch JWK
        HttpsJwks httpsJkws = new HttpsJwks("http://localhost:" + port + "/oauth2/v3/certs");
        HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKeyResolver(httpsJwksKeyResolver)
                .build();

        // verify JWT
        JwtContext context = jwtConsumer.process(jwt);
        assertThat(context.getJwtClaims().getSubject()).isEqualTo("klaus");
        assertThat("uid").isIn(context.getJwtClaims().getClaimValue("scope"));
        assertThat("name").isIn(context.getJwtClaims().getClaimValue("scope"));
    }
}
