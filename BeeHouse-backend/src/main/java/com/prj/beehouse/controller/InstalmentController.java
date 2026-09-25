package com.prj.beehouse.controller;

import com.prj.beehouse.payload.ResponseApi;
import com.prj.beehouse.payload.response.InstalmentPeriodResponse;
import com.prj.beehouse.payload.response.InstalmentResponse;
import com.prj.beehouse.service.InstalmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "Instalment", description = "Instalment management")
public class InstalmentController {

    private final InstalmentService instalmentService;

    @Operation(
            summary = "Get the instalment amount for a specific month",
            description = "Returns the instalment amount for a given month and year.",
            parameters = {
                    @Parameter(name = "year", description = "Year of the instalment to retrieve", required = true, example = "2025"),
                    @Parameter(name = "month", description = "Month of the instalment to retrieve", required = true, example = "8")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instalment retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Instalment not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("shared/instalments/{year}/{month}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<InstalmentResponse>> getCurrentAmount(
            @PathVariable @NotNull @Min(2000) Integer year,
            @PathVariable @NotNull @Min(1) @Max(12) Integer month
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, instalmentService.getCurrentAmount(month, year));
    }

    @Operation(
            summary = "Get the instalment amount for the current month",
            description = "Returns the instalment amount for the current month and year.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instalment retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Instalment not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("shared/instalments/current")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<InstalmentResponse>> getCurrentAmountForCurrentMonth() {
        return ResponseApi.buildResponse(HttpStatus.OK, instalmentService.getCurrentAmountForCurrentMonth());
    }

    @Operation(
            summary = "Get instalment periods",
            description = "Returns year and month for all instalments.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Instalment periods retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("shared/instalments/periods")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<List<InstalmentPeriodResponse>>> getInstalmentPeriods() {
        return ResponseApi.buildResponse(HttpStatus.OK, instalmentService.getInstalmentPeriods());
    }
}
