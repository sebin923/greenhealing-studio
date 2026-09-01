package com.greenhealing.studio.controller;

import com.greenhealing.studio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // templates/signup.html
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) String phone,
                          Model model) {
        try {
            userService.signup(name, email, password, phone);
            return "redirect:/login?signup=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
