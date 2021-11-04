package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.baseStation.data.validation.annotations.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO:: move to lib
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
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
    @NotBlank
    private String username;
    @NonNull
    @NotNull
    @Email
    private String email;
    @NonNull
    @NotNull
    private String title = "";
    @NonNull
    @NotNull
    @ValidPassword
    private String password;

    private Map<@NotBlank String, String> attributes = new HashMap<>();
}
