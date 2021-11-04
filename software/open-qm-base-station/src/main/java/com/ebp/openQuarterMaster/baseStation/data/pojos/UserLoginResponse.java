package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * TODO:: move to lib
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    private String token;
    private Instant expires;
}
