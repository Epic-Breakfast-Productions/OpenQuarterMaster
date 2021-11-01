package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO:: move to lib
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    private Map<@NotBlank String, String> externIds = new HashMap<>();
}
