package baji.lab1.controller;

import baji.lab1.entity.User;
import baji.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String adminDashboard() {
        return "admin/management_main";
    }

//    //управление пользователями
//    @GetMapping("/users")
//    public String manageUsers() {
//        return "admin/users";
//    }

//    // повысить до админа
//    @PostMapping("/users/promote/{userId}")
//    public String promoteToAdmin(@PathVariable Long userId) {
//        User user = userRepository.findById(userId).orElseThrow();
//        user.setRole(baji.lab1.entity.Role.ROLE_ADMIN);
//        userRepository.save(user);
//        return "redirect:/admin/users";
//    }
}