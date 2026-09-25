package com.prj.beehouse.payload.response;

import com.prj.beehouse.entity.Instalment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstalmentPeriodResponse {

    private Integer year;
    private Integer month;

    public static InstalmentPeriodResponse toResponse(Instalment instalment) {
        return InstalmentPeriodResponse.builder()
                .year(instalment.getYear())
                .month(instalment.getMonth())
                .build();
    }
}
