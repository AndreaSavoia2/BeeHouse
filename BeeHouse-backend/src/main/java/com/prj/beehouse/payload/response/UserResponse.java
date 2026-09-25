package com.prj.beehouse.payload.response;

import com.prj.beehouse.entity.User;
import com.prj.beehouse.util.StringUtility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private int id;
    private String username;
    private String name;
    private String lastname;
    private String email;

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(StringUtility.capitalizeFirstLetter(user.getUsername()))
                .name(StringUtility.capitalizeFirstLetter(user.getName()))
                .lastname(StringUtility.capitalizeFirstLetter(user.getLastname()))
                .email(StringUtility.cleanString(user.getEmail()))
                .build();
    }
}
