package com.ebp.openQuarterMaster.lib.driver.interaction.command;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commands {
	
	public static class Parts {
		
		public static final char COMMAND_START_CHAR = '$';
		public static final char RETURN_START_CHAR = COMMAND_START_CHAR;
		public static final char SEPARATOR_CHAR = '|';
		public static final char COMMAND_SEPARATOR_CHAR = '\n';
	}
	
	public static String getSimpleCommandString(CommandType type) {
		return ("" + Parts.COMMAND_START_CHAR + type.commandChar);
	}
	
	public static String getComplexCommandString(CommandType type, String... parts) {
		return getSimpleCommandString(type) + Parts.SEPARATOR_CHAR + String.join("" + Parts.SEPARATOR_CHAR, parts);
	}
	
	
	public static boolean isCommand(String line) {
		return line != null && !line.isBlank() && line.charAt(0) == Parts.COMMAND_START_CHAR;
	}
	
	public static boolean isLog(String line) {
		return line != null && !isCommand(line);
	}
	
	
}
