package com.pantheon.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:app.cfg")
public class PantheonBackendConfig {

    @Value("${app.sse.batch.size:100}")
    private int sseBatchSize;

    @Value("${app.sse.timeout:0}")
    private Long sseTimeout;

    @Value("${app.sse.tries.max:3}")
    private int maxTries;

    @Bean(name = "sseBatchSize")
    public int getSseBatchSize() {
        return sseBatchSize;
    }


    @Bean(name = "maxSize")
    public int getMaxTries() {
        return maxTries;
    }

    @Bean(name = "sseTimeout")
    public Long getSseTimeout() {
        return sseTimeout;
    }

}
