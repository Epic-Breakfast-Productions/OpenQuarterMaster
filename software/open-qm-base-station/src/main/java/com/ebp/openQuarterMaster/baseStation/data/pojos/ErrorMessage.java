package com.ebp.openQuarterMaster.baseStation.data.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO:: move to lib
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
    private String error;
    private String detail = "";

    public ErrorMessage(String error) {
        this.error = error;
    }
}
