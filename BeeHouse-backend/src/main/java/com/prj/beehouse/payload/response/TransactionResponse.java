package com.prj.beehouse.payload.response;

import com.prj.beehouse.entity.Transaction;
import com.prj.beehouse.entity.enumerated.TransactionType;
import com.prj.beehouse.util.StringUtility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Represents a user's transaction")
public class TransactionResponse {

    private Integer id;

    @Schema(description = "Transaction title", example = "Grocery shopping")
    private String title;

    @Schema(description = "Transaction description", example = "Weekly grocery shopping")
    private String description;

    @Schema(description = "Transaction amount", example = "45.50")
    private BigDecimal amount;

    @Schema(description = "Transaction date", example = "2025-08-28")
    private LocalDate date;

    @Schema(description = "Transaction type", example = "EXPENSE")
    private TransactionType transactionType;

    @Schema(description = "Category linked to the transaction", example = "Groceries")
    private String category;

    @Schema(description = "User that owns the transaction", example = "10")
    private UserResponse user;

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .title(StringUtility.capitalizeFirstLetter(transaction.getTitle()))
                .description(StringUtility.capitalizeFirstLetter(transaction.getDescription()))
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .transactionType(transaction.getTransactionType())
                .category(transaction.getCategory() == null
                        ? null
                        : StringUtility.capitalizeFirstLetter(transaction.getCategory().getCategoryName()))
                .user(UserResponse.toResponse(transaction.getUser()))
                .build();
    }
}
