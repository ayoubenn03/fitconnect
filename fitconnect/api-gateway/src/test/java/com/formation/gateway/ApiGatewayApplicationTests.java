package com.formation.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifie que le contexte Spring de la gateway demarre correctement,
 * sans dependre d'un config-server ou d'un eureka-server reellement lances.
 */
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
