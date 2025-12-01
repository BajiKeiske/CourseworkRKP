package baji.lab1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin_dashboard"; // страница для админа
    }

    @GetMapping("/user/dashboard")
    public String userDashboard() {
        return "user_dashboard"; // страница для юзера
    }
}