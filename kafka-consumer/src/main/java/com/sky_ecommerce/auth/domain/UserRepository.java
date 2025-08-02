package com.sky_ecommerce.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // Derived query kept if the property 'email' exists on User
    Optional<User> findByEmail(String email);

    // Fallback JPQL to avoid PartTree parsing failures if the property name differs or Lombok accessors differ
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailExplicit(@Param("email") String email);
}
