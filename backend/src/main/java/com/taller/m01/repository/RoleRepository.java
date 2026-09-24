package com.taller.m01.repository;

import com.taller.m01.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    List<Role> findAllByOrderByNameAsc();
}
