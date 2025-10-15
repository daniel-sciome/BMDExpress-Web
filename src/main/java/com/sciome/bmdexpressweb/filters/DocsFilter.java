package com.sciome.bmdexpressweb.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Run before all other filters including Vaadin
public class DocsFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(DocsFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        log.debug("DocsFilter processing: {}", path);

        // Intercept all /docs/** requests before Vaadin sees them
        if (path.startsWith("/docs/")) {
            log.info("Serving documentation file: {}", path);
            serveStaticFile(path, httpResponse);
            return; // Don't continue the chain - we handled it
        }

        // For other requests, continue normally
        chain.doFilter(request, response);
    }

    private void serveStaticFile(String path, HttpServletResponse response) throws IOException {
        // If path ends with / or is just /docs, serve index.html
        String filePath = path;
        if (path.endsWith("/") || path.equals("/docs")) {
            filePath = path + (path.endsWith("/") ? "" : "/") + "index.html";
        }

        // Remove leading slash and load from classpath
        ClassPathResource resource = new ClassPathResource("static" + filePath);

        if (resource.exists()) {
            // Set appropriate content type
            String contentType = determineContentType(filePath);
            response.setContentType(contentType);

            // Prevent browser caching during development
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // Copy resource to response
            try (InputStream is = resource.getInputStream()) {
                is.transferTo(response.getOutputStream());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String determineContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }
}
