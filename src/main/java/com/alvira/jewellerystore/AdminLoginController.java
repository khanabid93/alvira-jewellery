package com.alvira.jewellerystore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/admin-login")
    public String showAdminLogin() {
        return "admin-login";
    }
}