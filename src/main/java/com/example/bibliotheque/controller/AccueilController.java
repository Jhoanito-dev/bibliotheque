package com.example.bibliotheque.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AccueilController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/accueil")
    public String accueil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            if (auth.getAuthorities().toString().contains("ADMIN")) {
                return "bibliothecaire/dashboard"; // Retourne directement la vue
            } else {
                return "adherent/dashboard"; // Retourne directement la vue
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}