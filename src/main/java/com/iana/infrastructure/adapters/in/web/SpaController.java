package com.iana.infrastructure.adapters.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/login", "/books", "/categories", "/users"})
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
