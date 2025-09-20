package com.soundtracker.backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * Ensures clean URLs for embedded SPAs.
 * Spring Boot only auto-serves index.html as a welcome page at the root context.
 * Subdirectories (e.g. /app/angular/) need an explicit forward so users can click
 * a folder-style link and still receive the SPA's index.html.
 *
 * We forward only the root of each SPA here. Deep-link / route fallback can be
 * added later if the Angular/React apps enable HTML5 pushState routing that
 * requires mapping arbitrary subpaths back to index.html.
 */
@Controller
public class FrontendForwardController {

    @GetMapping({"/app/angular", "/app/angular/"})
    public String angularRoot() {
        return "forward:/app/angular/index.html";
    }

    @GetMapping({"/app/react", "/app/react/"})
    public String reactRoot() {
        // If React hasn't been built yet (no index.html), forwarding will still
        // result in a 404 – which is acceptable and surfaces the missing build.
        return "forward:/app/react/index.html";
    }

    /** Optional lightweight check endpoints (not strictly required) */
    @GetMapping("/internal/spa-status/angular")
    @ResponseBody
    public String angularStatus() throws IOException {
        return new ClassPathResource("static/app/angular/index.html").exists() ? "present" : "missing";
    }

    @GetMapping("/internal/spa-status/react")
    @ResponseBody
    public String reactStatus() throws IOException {
        return new ClassPathResource("static/app/react/index.html").exists() ? "present" : "missing";
    }
}
