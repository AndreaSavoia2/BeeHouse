package com.prj.beehouse.entity;

import com.prj.beehouse.entity.common.CreationUpdate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@Table(name = "instalment_template")
public class InstalmentTemplate extends CreationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    public InstalmentTemplate(BigDecimal cost) {
        this.cost = cost;
    }
}
