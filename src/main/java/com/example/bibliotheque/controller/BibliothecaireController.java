package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BibliothecaireController {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @GetMapping("/bibliothecaire/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("livres", livreRepository.findAll());
        model.addAttribute("adherents", adherentRepository.findAll());
        return "bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/ajouter-livre")
    public String ajouterLivreForm(Model model) {
        model.addAttribute("livre", new Livre());
        return "bibliothecaire/ajouter-livre";
    }

    @PostMapping("/bibliothecaire/ajouter-livre")
    public String ajouterLivre(@ModelAttribute Livre livre) {
        livreRepository.save(livre);
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/ajouter-adherent")
    public String ajouterAdherentForm(Model model) {
        model.addAttribute("adherent", new Adherent());
        return "bibliothecaire/ajouter-adherent";
    }

    @PostMapping("/bibliothecaire/ajouter-adherent")
    public String ajouterAdherent(@ModelAttribute Adherent adherent) {
        adherentRepository.save(adherent);
        return "redirect:/bibliothecaire/dashboard";
    }
}