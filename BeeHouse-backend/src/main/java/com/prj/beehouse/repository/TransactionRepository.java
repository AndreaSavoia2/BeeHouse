package com.prj.beehouse.repository;

import com.prj.beehouse.entity.Transaction;
import com.prj.beehouse.payload.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Modifying
    @Query("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId")
    void clearCategory(Integer categoryId);

    @Query("SELECT t " +
            "FROM Transaction t " +
            "LEFT JOIN t.category c " +
            "WHERE FUNCTION('YEAR', t.date) = :year AND FUNCTION('MONTH', t.date) = :month")
    Page<Transaction> getTransactionsByMonthAndYear(int year, int month, Pageable pageable);

    @Query("SELECT t " +
            "FROM Transaction t " +
            "LEFT JOIN t.category c " +
            "WHERE t.user.id = :userId AND FUNCTION('YEAR', t.createdAt) = :year AND FUNCTION('MONTH', t.createdAt) = :month")
    Page<Transaction> getTransactionsByUserAndMonth(int userId, int year, int month, Pageable pageable);
}
