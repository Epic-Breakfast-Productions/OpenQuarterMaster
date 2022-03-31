package com.ebp.openQuarterMaster.lib.driver;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class State {
	
	@NotNull
	private String serialNo;
	private boolean online;
	
	//for demo
	private String currentMessage;
	private int encoderVal;
	private boolean encoderPressed;
	private List<String> pixelColors;
}
