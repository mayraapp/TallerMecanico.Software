package com.taller.m01.repository;

import com.taller.m01.entity.UserAccount;
import com.taller.m01.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select distinct u from UserAccount u left join u.roles r where (:query = '' or lower(u.fullName) like lower(concat('%', :query, '%')) or lower(u.email) like lower(concat('%', :query, '%'))) and (:role = '' or r.code = :role) and (:status is null or u.status = :status)")
    Page<UserAccount> search(@Param("query") String query, @Param("role") String role, @Param("status") UserStatus status, Pageable pageable);
    @Query("select count(u) from UserAccount u join u.roles r where r.code = 'SUPERADMIN' and u.status = 'ACTIVE'")
    long countActiveSuperAdmins();
}
