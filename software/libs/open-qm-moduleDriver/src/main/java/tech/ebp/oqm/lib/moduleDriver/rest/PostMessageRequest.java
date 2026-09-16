package tech.ebp.oqm.lib.moduleDriver.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostMessageRequest {
	
	@Size(max = 30)
	private String message;
}
