package bul.nik.ldtesttask.user.repository;

import bul.nik.ldtesttask.user.model.ERole;
import bul.nik.ldtesttask.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByName(ERole name);
}
