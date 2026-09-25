package com.prj.beehouse.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class SearchInstalmentCostRequest {

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be greater than or equal to 2000")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    public SearchInstalmentCostRequest(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }
}
