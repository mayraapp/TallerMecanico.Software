package com.taller.m01.repository;

import com.taller.m01.entity.PasswordRecoveryCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RecoveryCodeRepository extends JpaRepository<PasswordRecoveryCode, Long> {
    @Query("select c from PasswordRecoveryCode c where c.user.email = :email and c.usedAt is null order by c.id desc")
    List<PasswordRecoveryCode> findUsableByEmail(@Param("email") String email);
}
