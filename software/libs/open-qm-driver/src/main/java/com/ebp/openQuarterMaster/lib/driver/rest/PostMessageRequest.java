package com.ebp.openQuarterMaster.lib.driver.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostMessageRequest {
	@Max(30)
	private String message;
}
