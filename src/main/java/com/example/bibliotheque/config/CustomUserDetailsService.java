package com.example.bibliotheque.config;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.repository.AdherentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdherentRepository adherentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Adherent> adherentOptional = adherentRepository.findByEmail(email);
        if (!adherentOptional.isPresent()) {
            throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
        }
        Adherent adherent = adherentOptional.get();
        System.out.println("Utilisateur trouvé : " + adherent.getEmail() + ", rôle brut : " + adherent.getRole());

        // Retirer le préfixe "ROLE_" si présent
        String role = adherent.getRole();
        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5); // Supprime "ROLE_" (5 caractères)
        }

        return User.builder()
                .username(adherent.getEmail())
                .password(adherent.getMotDePasse())
                .roles(role) // Passer le rôle sans préfixe
                .build();
    }
}