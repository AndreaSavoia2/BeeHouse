package com.prj.beehouse.repository;

import com.prj.beehouse.entity.Instalment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InstalmentRepository extends JpaRepository<Instalment, Integer> {

    boolean existsByYearAndMonth(int year, int month);

    Optional<Instalment> findByYearAndMonth(int year, int month);

    @Query("SELECT i FROM Instalment i WHERE i.month = :month AND i.year = :year")
    Instalment getCurrentAmount(Integer month, Integer year);

    List<Instalment> findAllByOrderByYearDescMonthDesc();
}
