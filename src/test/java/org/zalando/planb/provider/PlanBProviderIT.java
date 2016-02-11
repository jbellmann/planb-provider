package org.zalando.planb.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringApplicationConfiguration(classes = { Main.class })
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("it")
public class PlanBProviderIT extends AbstractSpringTest {

    private static final Logger log = LoggerFactory.getLogger(PlanBProviderIT.class);

    @Value("${local.server.port}")
    private int port;

    RestTemplate rest = new RestTemplate();

    @Test
    public void run() {
        ResponseEntity<String> response = rest.getForEntity(
                URI.create("http://localhost:" + port + "/.well-known/openid-configuration"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info(response.getBody());
    }

    @Test
    public void createToken() {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.GET,
                URI.create("http://localhost:" + port + "/oauth2/access_token"));

        ResponseEntity<OIDCCreateTokenResponse> response = rest.exchange(request, OIDCCreateTokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        log.info(response.getBody().toString());
    }
}
