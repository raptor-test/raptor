package com.raptor.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@EnableAutoConfiguration
@ComponentScan(basePackages = "com.raptor.*")
@PropertySource(value = "classpath:application.properties",ignoreResourceNotFound = true)
@Configuration
public class Utililty implements EnvironmentAware {
    @Autowired
    private static Environment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public String getEnvironmentProperties(String property) {
        if (environment != null && property != null && environment.getProperty(property) != null) {
            return environment.getProperty(property);
        }
        return property;
    }

    public String getDecodedValue (String input) {
        byte[] decodedstring = Base64.decodeBase64(input);
        return  new String(decodedstring );
    }
}
