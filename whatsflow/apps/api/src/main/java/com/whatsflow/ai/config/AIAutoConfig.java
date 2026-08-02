package com.whatsflow.ai.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AIProperties.class)
public class AIAutoConfig {}
