package com.whatsflow.config;

import com.whatsflow.demo.config.DemoProperties;
import com.whatsflow.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        WhatsFlowProperties.class,
        WhatsAppProperties.class,
        JwtProperties.class,
        MinioProperties.class,
        DemoProperties.class
})
public class AppPropertiesConfig {
}
