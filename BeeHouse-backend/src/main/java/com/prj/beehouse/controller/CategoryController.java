package com.prj.beehouse.controller;

import com.prj.beehouse.payload.ResponseApi;
import com.prj.beehouse.payload.response.CategoryResponse;
import com.prj.beehouse.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "Category", description = "Category management")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create a new category",
            description = "Creates a new category. The category name must be unique.",
            parameters = @Parameter(
                    name = "categoryName",
                    description = "Name of the category to create",
                    required = true,
                    example = "Groceries"
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category created successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid categoryName parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "409", description = "Category already exists", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PostMapping("administrator/categories")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<CategoryResponse>> createCategory(
            @Size(min = 1, max = 150) @NotBlank @RequestParam String categoryName
    ) {
        return ResponseApi.buildResponse(HttpStatus.CREATED, categoryService.createCategory(categoryName));
    }

    @Operation(
            summary = "Category list",
            description = "Returns the list of all categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category list retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("shared/categories")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<Set<CategoryResponse>>> getAllCategories() {
        return ResponseApi.buildResponse(HttpStatus.OK, categoryService.getAllCategories());
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes an existing category. Before deleting the category, all related transactions are detached.",
            parameters = @Parameter(
                    name = "id",
                    description = "ID of the category to delete",
                    required = true,
                    example = "1"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category deleted successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid id parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @DeleteMapping("administrator/categories/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<Set<CategoryResponse>>> deleteCategory(
            @PathVariable @Positive int id
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, categoryService.deleteCategory(id));
    }

    @Operation(
            summary = "Update a category name",
            description = "Updates the name of an existing category. The new name must not already exist.",
            parameters = {
                    @Parameter(
                            name = "oldCategoryName",
                            description = "Current category name",
                            required = true,
                            example = "Old expenses"
                    ),
                    @Parameter(
                            name = "newCategoryName",
                            description = "New category name",
                            required = true,
                            example = "New expenses"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "409", description = "Category already exists", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PatchMapping("administrator/categories")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<Set<CategoryResponse>>> updateCategory(
            @Size(min = 1, max = 150) @NotBlank @RequestParam String oldCategoryName,
            @Size(min = 1, max = 150) @NotBlank @RequestParam String newCategoryName
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, categoryService.updateCategory(oldCategoryName, newCategoryName));
    }
}
