package tech.ebp.oqm.lib.moduleDriver.interaction.command.commands;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.ErrorCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.MessageCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.GetInfoCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.GetStateCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.OkCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.simple.PingCommand;
import tech.ebp.oqm.lib.moduleDriver.interaction.exceptions.CommandParseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
	
	public static Stream<Arguments> getValidCommandLines() {
		return Stream.of(
			Arguments.of("$O", OkCommand.getInstance()),
			Arguments.of("$P", PingCommand.getInstance()),
			Arguments.of("$I", GetInfoCommand.getInstance()),
			Arguments.of("$S", GetStateCommand.getInstance()),
			Arguments.of("$E", new ErrorCommand()),
			Arguments.of("$E|hello world", new ErrorCommand("hello world")),
			Arguments.of("$M|hello world", new MessageCommand("hello world"))
			//			Arguments.of("$O", OkCommand.getInstance())
			//TODO:: more
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValidCommandLines")
	public void testParse(String line, Command expectedCommand) {
		assertEquals(expectedCommand, CommandParser.parse(line));
	}
	
	public static Stream<Arguments> getInvalidCommandLines() {
		return Stream.of(
			Arguments.of(null, "Null line."),
			Arguments.of("", "Line is empty."),
			Arguments.of("     ", "Line is empty."),
			Arguments.of("$", "Line too short"),
			Arguments.of("#O", "Line is not a command."),
			Arguments.of("$@", "Could not determine command type."),
			Arguments.of("$M", "Simple command not recognized."),
			Arguments.of("$O|hello world", "Complex command not recognized.")
		);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidCommandLines")
	public void testParseFail(String line, String errMessage) {
		assertThrows(
			CommandParseException.class,
			()->{
				CommandParser.parse(line);
			},
			errMessage
		);
	}
	
	
}