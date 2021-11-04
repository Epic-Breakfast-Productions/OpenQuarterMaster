package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO:: move to lib
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {
    @NonNull
    @NotNull
    @NotBlank
    private String usernameEmail;
    @NonNull
    @NotNull
    @NotBlank
    private String password;
}
