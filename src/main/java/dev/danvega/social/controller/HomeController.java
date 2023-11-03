package dev.danvega.social.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String page() {
        return "page";
    }

    @GetMapping("/login")
    public String secured() {
        return "Hello, Secured!";
    }

}
