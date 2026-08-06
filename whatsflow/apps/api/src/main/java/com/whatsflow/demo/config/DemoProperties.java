package com.whatsflow.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "whatsflow.demo")
public class DemoProperties {
    private boolean enabled = false;
    private String email = "demo@whatsflow.ai";
    private String password = "Demo@123";
    private String fullName = "Dr Rajesh Sharma";
    private String companyName = "ABC Hospital";
    private int customers = 500;
    private int campaigns = 20;
    private int conversations = 150;
    private int forms = 10;
}
