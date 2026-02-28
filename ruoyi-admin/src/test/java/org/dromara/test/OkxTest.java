package org.dromara.test;

import com.bitstrat.client.OkxRestClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/11 17:14
 * @Content
 */

@Slf4j
public class OkxTest {

    @Test
    public void test() {
        OkxRestClient okxRestClient = new OkxRestClient();
        ResponseEntity<String> noAuth = okxRestClient.getNoAuth("/api/v5/public/instruments", Map.of("instType", "SWAP"));
        log.info(noAuth.getBody());
    }
}
