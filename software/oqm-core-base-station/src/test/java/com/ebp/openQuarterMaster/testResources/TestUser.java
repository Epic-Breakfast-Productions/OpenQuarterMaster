package com.ebp.openQuarterMaster.testResources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestUser {

	public String firstname;
	public String lastname;
	public String username;
	public String email;
	public String password;
}
