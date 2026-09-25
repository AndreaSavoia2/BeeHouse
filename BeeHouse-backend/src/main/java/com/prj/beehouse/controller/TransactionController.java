package com.prj.beehouse.controller;

import com.prj.beehouse.payload.ResponseApi;
import com.prj.beehouse.payload.request.AddTransactionRequest;
import com.prj.beehouse.security.CustomUserDetails;
import com.prj.beehouse.service.TransactionService;
import com.prj.beehouse.util.BuildPageableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Transaction", description = "Transaction management")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Add a new transaction",
            description = "Adds a new transaction for the authenticated user. If a category is provided, the transaction is linked to it.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transaction added successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request fields", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Category or instalment not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PostMapping("user/transactions")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseApi<String>> addTransaction(
            @Valid @RequestBody AddTransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseApi.buildResponse(HttpStatus.CREATED, transactionService.addTransaction(request, userDetails));
    }

    @Operation(
            summary = "Remove a transaction",
            description = "Deletes a transaction owned by the authenticated user and updates the linked instalment balance.",
            parameters = @Parameter(
                    name = "transactionId",
                    description = "ID of the transaction to delete",
                    required = true,
                    example = "1"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction deleted successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid transactionId parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Transaction does not belong to the user or operation is unauthorized", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @DeleteMapping("user/transactions/{transactionId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseApi<String>> removeTransaction(
            @PathVariable @NotNull @Positive Integer transactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, transactionService.removeTransaction(transactionId, userDetails));
    }

    @Operation(
            summary = "Get transactions by month and year",
            description = "Returns transactions filtered by year and month, with pagination and sorting.",
            parameters = {
                    @Parameter(name = "year", description = "Transaction year", required = true, example = "2025"),
                    @Parameter(name = "month", description = "Transaction month", required = true, example = "8"),
                    @Parameter(name = "pageNumber", description = "Page number", example = "0"),
                    @Parameter(name = "pageSize", description = "Number of items per page", example = "10"),
                    @Parameter(name = "name", description = "Field used to sort the results", example = "date"),
                    @Parameter(name = "direction", description = "Sort direction: ASC or DESC", example = "DESC")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("user/transactions/{year}/{month}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getTransactionsByMonthAndYear(
            @PathVariable @NotNull @Min(2000) Integer year,
            @PathVariable @NotNull @Min(1) @Max(12) Integer month,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "date") @Size(min = 1, max = 50) String name,
            @RequestParam(defaultValue = "DESC") @Size(min = 3, max = 4) String direction
    ) {
        Map<String, Object> page = BuildPageableResponse.buildPage(
                transactionService.getTransactionsByMonthAndYear(year, month, pageNumber, pageSize, name, direction)
        );
        return ResponseApi.buildResponse(HttpStatus.OK, page);
    }

    @Operation(
            summary = "Get a user's transactions by month and year",
            description = "Returns a user's transactions filtered by username, year and month, with pagination and sorting.",
            parameters = {
                    @Parameter(name = "year", description = "Transaction year", required = true, example = "2025"),
                    @Parameter(name = "month", description = "Transaction month", required = true, example = "8"),
                    @Parameter(name = "username", description = "User username", required = true, example = "john.doe"),
                    @Parameter(name = "pageNumber", description = "Page number", example = "0"),
                    @Parameter(name = "pageSize", description = "Number of items per page", example = "10"),
                    @Parameter(name = "name", description = "Field used to sort the results", example = "date"),
                    @Parameter(name = "direction", description = "Sort direction: ASC or DESC", example = "DESC")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("user/transactions/{year}/{month}/{username}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getTransactionsByUserAndMonth(
            @PathVariable @NotNull @Min(2000) Integer year,
            @PathVariable @NotNull @Min(1) @Max(12) Integer month,
            @PathVariable @Size(max = 101, min = 1) String username,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "date") @Size(min = 1, max = 50) String name,
            @RequestParam(defaultValue = "DESC") @Size(min = 3, max = 4) String direction
    ) {
        Map<String, Object> page = BuildPageableResponse.buildPage(
                transactionService.getTransactionsByUserAndMonth(username, year, month, pageNumber, pageSize, name, direction)
        );
        return ResponseApi.buildResponse(HttpStatus.OK, page);
    }

    @Operation(
            summary = "Get transactions for the current month",
            description = "Returns transactions for the current month, with pagination and sorting.",
            parameters = {
                    @Parameter(name = "pageNumber", description = "Page number", example = "0"),
                    @Parameter(name = "pageSize", description = "Number of items per page", example = "10"),
                    @Parameter(name = "name", description = "Field used to sort the results", example = "date"),
                    @Parameter(name = "direction", description = "Sort direction: ASC or DESC", example = "DESC")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("user/transactions/current")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getTransactionsForCurrentMonth(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "date") @Size(min = 1, max = 50) String name,
            @RequestParam(defaultValue = "DESC") @Size(min = 3, max = 4) String direction
    ) {
        Map<String, Object> page = BuildPageableResponse.buildPage(
                transactionService.getTransactionsForCurrentMonth(pageNumber, pageSize, name, direction)
        );
        return ResponseApi.buildResponse(HttpStatus.OK, page);
    }
}
