package com.taller.m01.repository;
import com.taller.m01.entity.RevokedSession;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RevokedSessionRepository extends JpaRepository<RevokedSession, Long> { boolean existsByJti(String jti); }
