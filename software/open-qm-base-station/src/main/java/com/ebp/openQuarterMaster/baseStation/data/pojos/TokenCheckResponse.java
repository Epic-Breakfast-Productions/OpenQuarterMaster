package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * TODO:: move to lib
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenCheckResponse {
    private boolean hadToken = false;
    private boolean tokenSecure = false;
    private boolean expired = true;
    private Date expirationDate = null;
}
