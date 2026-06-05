package baji.lab1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlockedController {

    @GetMapping("/blocked")
    public String blocked() {
        return "blocked";
    }
}