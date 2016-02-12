package org.zalando.planb.provider;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringApplicationConfiguration(classes = {Main.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("it")
public class OIDCDiscoveryIT extends AbstractSpringTest {
    @Value("${local.server.port}")
    private int port;

    RestTemplate rest = new RestTemplate();

    @Test
    public void discoveryAvailability() {
        ResponseEntity<OIDCDiscoveryInformationResponse> response = rest
                .getForEntity(
                        URI.create("http://localhost:" + port + "/.well-known/openid-configuration"),
                        OIDCDiscoveryInformationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void jwksUrl() {
        ResponseEntity<OIDCDiscoveryInformationResponse> response = rest
                .getForEntity(
                        URI.create("http://localhost:" + port + "/.well-known/openid-configuration"),
                        OIDCDiscoveryInformationResponse.class);

        assertThat(response.getBody().getJwksUri()).isEqualTo("http://localhost:" + port + "/oauth2/v3/certs");
    }

    @Test
    public void jwksUrlProto() {
        RequestEntity request = RequestEntity.get(URI.create("http://localhost:" + port + "/.well-known/openid-configuration"))
                .header("X-Forwarded-Proto", "https")
                .build();

        ResponseEntity<OIDCDiscoveryInformationResponse> response = rest
                .exchange(request, OIDCDiscoveryInformationResponse.class);

        assertThat(response.getBody().getJwksUri()).isEqualTo("https://localhost:" + port + "/oauth2/v3/certs");
    }

}