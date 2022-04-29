package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import com.ebp.openQuarterMaster.lib.driver.BlockLightPowerState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.exceptions.CommandParseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CommandParsingUtilsTest {
	
	@Test
	public void testSplitLine() {
		String testString = " hello" + Commands.Parts.SEPARATOR_CHAR + "world ";
		
		String[] result = CommandParsingUtils.splitLine(testString);
		
		assertEquals(2, result.length);
		
		assertEquals("hello", result[0]);
		assertEquals("world", result[1]);
	}
	
	
	@Test
	public void testGetCommandPart() {
		String result = CommandParsingUtils.getCommandPart(new String[]{"hello", "world"});
		
		assertEquals("hello", result);
	}
	
	@Test
	public void testGetNonCommandParts() {
		String[] result = CommandParsingUtils.getNonCommandParts(
			new String[]{"hello", "world", "there", "again"}
		);
		
		assertArrayEquals(new String[]{"world", "there", "again"}, result);
	}
	
	@Test
	public void testGetNonCommandPartsOnePart() {
		String[] result = CommandParsingUtils.getNonCommandParts(
			new String[]{"hello"}
		);
		
		assertArrayEquals(new String[0], result);
	}
	
	@Test
	public void testGetNonCommandPartsNoParts() {
		assertThrows(
			CommandParseException.class,
			()->{
				CommandParsingUtils.getNonCommandParts(
					new String[0]
				);
			},
			"Command parts empty."
		);
	}
	
	@Test
	public void testGetPartsAndAssertCommandParts() {
		String[] returned = CommandParsingUtils.getPartsAndAssertCommand(CommandType.MESSAGE, "$M|hello world");
		
		assertArrayEquals(new String[]{"hello world"}, returned);
	}
	
	public static Stream<Arguments> getErringCommandParts() {
		return Stream.of(
			Arguments.of(CommandType.PING, "$I")
		);
	}
	
	@ParameterizedTest
	@MethodSource("getErringCommandParts")
	public void testGetPartsAndAssertCommandPartsErr(CommandType type, String line) {
		assertThrows(
			CommandParseException.class,
			()->{
				CommandParsingUtils.getNonCommandParts(
					new String[0]
				);
			},
			"Line given not the correct command type."
		);
	}
	
	
	public static Stream<Arguments> getValidLightSettings() {
		return Stream.of(
			Arguments.of("ON", BlockLightPowerState.ON),
			Arguments.of("OFF", BlockLightPowerState.OFF),
			Arguments.of("FLASHING", BlockLightPowerState.FLASHING),
			Arguments.of("0", 0),
			Arguments.of("127", 127),
			Arguments.of("255", 255),
			Arguments.of("FFFFFF", Color.WHITE)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValidLightSettings")
	public void testValidGetLightSettingAsObj(String setting, Object expected) {
		assertEquals(expected, CommandParsingUtils.getLightSettingAsObj(setting));
	}
	
	public static Stream<Arguments> getInvalidLightSettings() {
		return Stream.of(
			Arguments.of("BAD"),
			Arguments.of("256"),
			Arguments.of("-1"),
			Arguments.of("ASDFGH")
		);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidLightSettings")
	public void testInvalidGetLightSettingAsObj(String setting) {
		assertThrows(
			CommandParseException.class,
			()->log.error("Got value when we shouldn't have: {}", CommandParsingUtils.getLightSettingAsObj(setting)),
			"Could not determine type of light setting:"
		);
	}
	
}