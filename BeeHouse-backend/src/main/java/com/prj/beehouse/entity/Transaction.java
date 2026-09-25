package com.prj.beehouse.entity;

import com.prj.beehouse.entity.common.CreationUpdate;
import com.prj.beehouse.entity.enumerated.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@Table(name = "transaction")
public class Transaction extends CreationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 65535)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @JoinColumn(name = "category_id", nullable = true)
    @ManyToOne
    private Category category;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    public Transaction(String title, String description, BigDecimal amount, LocalDate date, TransactionType transactionType, Category category) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.transactionType = transactionType;
        this.category = category;
    }
}
