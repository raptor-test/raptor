package com.raptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

@SpringBootApplication()
@PropertySource(ResourceUtils.CLASSPATH_URL_PREFIX + "application.properties")
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class,args);

	}



}
