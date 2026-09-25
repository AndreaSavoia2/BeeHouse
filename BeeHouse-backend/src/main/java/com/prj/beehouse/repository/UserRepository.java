package com.prj.beehouse.repository;

import com.prj.beehouse.entity.User;
import com.prj.beehouse.entity.enumerated.AuthorityName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsUserByUsernameAndIdNot(String username, int id);


    double countUserByAuthorityAuthorityName(AuthorityName authorityName);

    boolean existsByEmail(String email);

    Page<User> findAllByAuthorityAuthorityNameAndEnableTrue(AuthorityName authorityName, Pageable pageable);
}
