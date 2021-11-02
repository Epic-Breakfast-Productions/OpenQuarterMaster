package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:: move to lib
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class User extends MainObject {
    @NonNull
    @NotNull
    @NotBlank
    private String firstName;
    @NonNull
    @NotNull
    @NotBlank
    private String lastName;
    @NonNull
    @NotNull
    @Email
    private String email;
    @NonNull
    @NotNull
    private String title = "";
    private String pwHash = null;
    private List<String> roles = new ArrayList<>();

    private Map<@NotBlank String, String> externIds = new HashMap<>();

    /**
     * Still responsible for setting:
     * <ul>
     *     <li>password hash</li>
     *     <li>Roles</li>
     *     <li>attributes</li>
     * </ul>
     *
     * @param userCreateRequest
     * @return
     */
    public static Builder builder(UserCreateRequest userCreateRequest) {
        return new Builder().firstName(userCreateRequest.getFirstName())
                .lastName(userCreateRequest.getLastName())
                .email(userCreateRequest.getEmail())
                .title(userCreateRequest.getTitle());
    }
}
