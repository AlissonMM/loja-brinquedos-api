package edu.meialua.morkstore.adapters.in.repositories;

import edu.meialua.morkstore.adapters.in.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);
}
