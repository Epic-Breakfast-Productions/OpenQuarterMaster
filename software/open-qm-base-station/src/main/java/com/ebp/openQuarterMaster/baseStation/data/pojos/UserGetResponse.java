package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:: move to lib
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class UserGetResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String title;
    private Map<String, String> externIds = new HashMap<>();
    private List<String> roles = new ArrayList<>();

    public static Builder builder(User user) {
        return new Builder().firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .title(user.getTitle())
                .externIds(user.getExternIds())
                .roles(new ArrayList<>(user.getRoles()));
    }

}
