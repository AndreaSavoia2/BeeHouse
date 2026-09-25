package com.prj.beehouse.payload.response;

import com.prj.beehouse.entity.Instalment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstalmentResponse {

    private int id;
    private Integer year;
    private Integer month;
    private BigDecimal initialAmount;
    private BigDecimal currentBalance;

    public static InstalmentResponse toResponse(Instalment instalment) {
        return InstalmentResponse.builder()
                .id(instalment.getId())
                .year(instalment.getYear())
                .month(instalment.getMonth())
                .initialAmount(instalment.getInitialAmount())
                .currentBalance(instalment.getCurrentBalance())
                .build();
    }
}
