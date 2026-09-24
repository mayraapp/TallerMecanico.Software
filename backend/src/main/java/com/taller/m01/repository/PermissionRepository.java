package com.taller.m01.repository;

import com.taller.m01.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PermissionRepository extends JpaRepository<Permission, Long> { List<Permission> findAllByOrderByNameAsc(); }
