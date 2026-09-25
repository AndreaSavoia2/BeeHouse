package com.prj.beehouse.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ListCategoryResponse {

    @Schema(description = "Category ID", example = "1")
    private Integer categoryId;

    @Schema(description = "Category name", example = "Groceries")
    private String categoryName;

    @Schema(description = "ID of the user that owns the category", example = "10")
    private Integer userId;

    public ListCategoryResponse(Integer categoryId, String categoryName, Integer userId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
    }
}
