package edu.meialua.kidsgrace.config;

import edu.meialua.kidsgrace.adapters.in.Role;
import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.adapters.in.repositories.RoleRepository;
import edu.meialua.kidsgrace.adapters.in.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Garante que sempre exista um usuário administrador ao subir a API.
 * Roda uma vez a cada startup; se o usuário configurado já existir, não faz nada.
 * <p>
 * Credenciais padrão (sobrescrevíveis via application.properties / variáveis de
 * ambiente — ver app.seed.admin.*): username "admin", senha "conecta@2024".
 * Troque esses valores antes de subir em produção.
 */
@Component
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.password:conecta@2024}")
    private String adminPassword;

    @Value("${app.seed.admin.email:admin@admin.com}")
    private String adminEmail;

    @Value("${app.seed.admin.name:Administrador}")
    private String adminName;

    public AdminUserSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUserName(adminUsername)) {
            log.info("Seeder: usuário admin '{}' já existe, nada a fazer.", adminUsername);
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(buildRole("ADMIN")));

        User admin = new User();
        admin.setUserName(adminUsername);
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setTelephone("00000000000");
        admin.setAddress("N/A");
        admin.setImageProfile(1);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(Collections.singletonList(adminRole));

        userRepository.save(admin);

        log.info("Seeder: usuário admin '{}' criado com sucesso.", adminUsername);
    }

    private Role buildRole(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
