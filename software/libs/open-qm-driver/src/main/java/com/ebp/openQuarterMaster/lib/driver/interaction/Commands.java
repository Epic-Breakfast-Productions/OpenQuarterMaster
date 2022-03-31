package com.ebp.openQuarterMaster.lib.driver.interaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commands {
	
	public static class Parts {
		
		public static final char COMMAND_START_CHAR = '$';
		public static final char RETURN_START_CHAR = '^';
		public static final char SEPARATOR_CHAR = '|';
	}
	
	public static class CommandChars {
		
		public static final char INFO = 'I';
		public static final char STATE = 'S';
	}
	
	public static byte[] getSimpleCommand(char commandChar) {
		return ("" + Parts.COMMAND_START_CHAR + commandChar)
			.getBytes(StandardCharsets.UTF_8);
	}
	
	public static final byte[] GET_INFO_COMMAND = getSimpleCommand(CommandChars.INFO);
	public static final byte[] GET_STATE_COMMAND = getSimpleCommand(CommandChars.STATE);
	
	
	public static boolean isReturn(String line) {
		return !line.isBlank() && line.charAt(0) == Parts.RETURN_START_CHAR;
	}
	
	public static boolean isLog(String line) {
		return !isReturn(line);
	}
}
