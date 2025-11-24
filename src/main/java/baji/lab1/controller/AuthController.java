package baji.lab1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";  // покажет login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // покажет register.html
    }
}