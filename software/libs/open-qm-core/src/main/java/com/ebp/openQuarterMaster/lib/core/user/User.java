package com.ebp.openQuarterMaster.lib.core.user;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserCreateRequest;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
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
    private String username;
    @NonNull
    @NotNull
    @Email
    private String email;
    private String title;

    /*
     * Not used if service.authMode set to EXTERNAL
     */
    private String pwHash = null;

    private Set<String> roles = new HashSet<>();

    /*
     * Only used if service.authMode set to EXTERNAL
     */
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
        return new Builder()
                .firstName(userCreateRequest.getFirstName())
                .lastName(userCreateRequest.getLastName())
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .title(userCreateRequest.getTitle());
    }
    public static Builder builder() {
        return new Builder();
    }



}
