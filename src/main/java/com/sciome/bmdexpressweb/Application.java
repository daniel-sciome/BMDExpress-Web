package com.sciome.bmdexpressweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BMDExpress Web Application
 *
 * Spring Boot application providing web-based access to BMDExpress
 * dose-response analysis functionality.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Initialize BMDExpress properties for console/server mode
        // This disables GUI components and sets the application to headless mode
        com.sciome.bmdexpress2.shared.BMDExpressProperties.getInstance().setIsConsole(true);

        SpringApplication.run(Application.class, args);
    }
}
