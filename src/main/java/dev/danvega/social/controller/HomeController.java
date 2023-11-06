package dev.danvega.social.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String page() {
        return "page";
    }
    @RequestMapping("/login")
    public String customLogin() {
        return "login"; // Name of the Thymeleaf template for the login page
    }
}
