package com.ebp.openQuarterMaster.lib.core.rest.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * The response object from a user login request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
	
	private String token;
	private Instant expires;
}
