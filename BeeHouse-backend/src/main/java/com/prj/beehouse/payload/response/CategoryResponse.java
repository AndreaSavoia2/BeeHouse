package com.prj.beehouse.payload.response;

import com.prj.beehouse.entity.Category;
import com.prj.beehouse.util.StringUtility;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {

    private int id;
    private String categoryName;

    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(StringUtility.capitalizeFirstLetter(category.getCategoryName()))
                .build();
    }
}
