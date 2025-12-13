package com.pantheon.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PantheonBackendConfig {

    @Value("${app.sse.batch.size:100}")
    private int sseBatchSize;

    @Value("${app.sse.timeout:0}")
    private Long sseTimeout;

    @Bean(name = "sseBatchSize")
    public int getSseBatchSize() {
        return sseBatchSize;
    }

    @Bean(name = "sseTimeout")
    public Long getSseTimeout() {
        return sseTimeout;
    }

}
