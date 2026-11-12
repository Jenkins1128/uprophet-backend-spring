package com.uprophet.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findByUserName(String userName);
}
