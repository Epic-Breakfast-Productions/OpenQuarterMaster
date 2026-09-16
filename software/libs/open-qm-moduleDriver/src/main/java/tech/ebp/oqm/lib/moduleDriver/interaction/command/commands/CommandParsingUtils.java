package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands;

import tech.ebp.oqm.lib.moduleDriver.BlockLightPowerState;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.exceptions.CommandParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandParsingUtils {
	
	public static String[] splitLine(String line) {
		return line.strip().split("\\" + Commands.Parts.SEPARATOR_CHAR);
	}
	
	public static String getCommandPart(String[] commandParts) {
		return commandParts[0];
	}
	
	public static String[] getNonCommandParts(String[] commandParts) {
		if (commandParts.length < 1) {
			throw new CommandParseException("Command parts empty.");
		}
		if (commandParts.length < 2) {
			return new String[0];
		}
		return Arrays.copyOfRange(commandParts, 1, commandParts.length);
	}
	
	public static String[] getPartsAndAssertCommand(CommandType type, String line) {
		String[] commandParts = splitLine(line);
		if (!type.isType(CommandParsingUtils.getCommandPart(commandParts))) {
			throw new CommandParseException("Line given not the correct command type.");
		}
		
		return getNonCommandParts(commandParts);
	}
	
	public static Object getLightSettingAsObj(String setting) {
		setting = setting.strip().toUpperCase();
		
		try {
			return BlockLightPowerState.valueOf(setting.toUpperCase());
		} catch(IllegalArgumentException e) {
		}
		
		try {
			Integer num = Integer.parseUnsignedInt(setting, 10);
			if (num >= 0 && num < 256) {
				return num;
			}
		} catch(NumberFormatException e) {
		}
		
		try {
			if (setting.length() == 6) {
				Color color = Color.decode("#" + setting);
				if (color != null) {
					return color;
				}
			}
		} catch(IllegalArgumentException e) {
		
		}
		
		throw new CommandParseException("Could not determine type of light setting: \"" + setting + "\"");
	}
}
