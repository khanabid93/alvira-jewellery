package com.alvira.jewellerystore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/returns")
    public String returns() {
        return "returns";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }
}