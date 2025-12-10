package com.example.bankcards.repository;


import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Role.*;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(RoleName name);


    List<Role> findAllByNameIn(Iterable<RoleName> names);
}
