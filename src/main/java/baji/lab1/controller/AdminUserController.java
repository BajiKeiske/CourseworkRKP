package baji.lab1.controller;

import baji.lab1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Показать всех пользователей (с фильтрацией, если есть параметры)
    @GetMapping
    public String users(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean blocked,
            Model model) {
        model.addAttribute("users", userService.searchUsers(username, blocked));
        return "admin/users";
    }

    // Заблокировать пользователя
    @PostMapping("/block/{id}")
    public String block(@PathVariable Long id) {
        userService.blockUser(id);
        return "redirect:/admin/users";
    }

    // Разблокировать пользователя
    @PostMapping("/unblock/{id}")
    public String unblock(@PathVariable Long id) {
        userService.unblockUser(id);
        return "redirect:/admin/users";
    }
}