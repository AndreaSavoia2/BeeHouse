package com.prj.beehouse.controller;

import com.prj.beehouse.payload.ResponseApi;
import com.prj.beehouse.service.InstalmentTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "InstalmentTemplate", description = "Instalment template management")
public class InstalmentTemplateController {

    private final InstalmentTemplateService instalmentTemplateService;

    @Operation(
            summary = "Update the instalment template cost",
            description = "Updates the standard instalment cost in the template.",
            parameters = @Parameter(
                    name = "newCost",
                    description = "New cost to set",
                    required = true,
                    example = "150.50"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cost updated successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid newCost parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PatchMapping("administrator/instalment-template/cost")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<String>> updateCost(
            @NotNull
            @DecimalMin(value = "0.0", inclusive = false)
            @Digits(integer = 10, fraction = 2)
            @RequestParam BigDecimal newCost
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, instalmentTemplateService.updateCost(newCost));
    }
}
