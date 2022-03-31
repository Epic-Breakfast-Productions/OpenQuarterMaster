package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class State {
	
	private String serialNo;
	private boolean online;
	
	//for demo
	private String currentMessage;
	private int encoderVal;
	private boolean encoderPressed;
	private List<String> pixelColors;
}
