package edu.meialua.kidsgrace.service;

import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.adapters.in.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolve o usuário autenticado a partir do SecurityContextHolder.
 * O projeto não usa @AuthenticationPrincipal em nenhum lugar hoje — o
 * subject do JWT (username) é o único dado disponível na Authentication,
 * então buscamos o User completo a partir dele.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário autenticado não encontrado no banco de dados."
                ));
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
