package com.bfsi.risk;

import com.bfsi.risk.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class RiskAnalysisApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskAnalysisApplication.class, args);
    }
}
