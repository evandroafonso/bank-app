package com.assignment.bank.user.repositories;

import com.assignment.bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPersonalId(String personalId);

    Optional<User> findByEmail(String email);
}
