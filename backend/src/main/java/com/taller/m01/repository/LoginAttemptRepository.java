package com.taller.m01.repository;
import com.taller.m01.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> { }
