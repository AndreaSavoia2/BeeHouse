package com.prj.beehouse.repository;

import com.prj.beehouse.entity.InstalmentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstalmentTemplateRepository extends JpaRepository<InstalmentTemplate, Integer> {

    Optional<InstalmentTemplate> findFirstByOrderByIdAsc();
}
