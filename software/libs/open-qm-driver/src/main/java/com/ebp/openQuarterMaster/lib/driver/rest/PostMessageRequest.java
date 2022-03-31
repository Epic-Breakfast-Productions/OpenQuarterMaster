package com.ebp.openQuarterMaster.lib.driver.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostMessageRequest {
	
	private String message;
}
