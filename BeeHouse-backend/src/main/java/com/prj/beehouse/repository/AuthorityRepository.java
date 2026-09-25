package com.prj.beehouse.repository;

import com.prj.beehouse.entity.Authority;
import com.prj.beehouse.entity.enumerated.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Authority findByAuthorityDefaultTrue();

    Set<Authority> findByAuthorityName(AuthorityName authorityName);
}
