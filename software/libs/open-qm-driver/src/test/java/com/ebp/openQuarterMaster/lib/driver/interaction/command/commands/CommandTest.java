package com.ebp.openQuarterMaster.lib.driver.interaction.command.commands;

import com.ebp.openQuarterMaster.lib.driver.ModuleInfo;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.ErrorCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetInfoReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.GetStateReturnCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.MessageCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetInfoCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.GetStateCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.OkCommand;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.simple.PingCommand;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
	
	public static Stream<Arguments> getCommandsWithString() {
		return Stream.of(
			Arguments.of(GetInfoCommand.getInstance(), "$I"),
			Arguments.of(GetStateCommand.getInstance(), "$S"),
			Arguments.of(OkCommand.getInstance(), "$O"),
			Arguments.of(PingCommand.getInstance(), "$P"),
			Arguments.of(new ErrorCommand(), "$E"),
			Arguments.of(new ErrorCommand().setErrInfo("error happened"), "$E|error happened"),
			Arguments.of(new MessageCommand().setMessage("message"), "$M|message"),
			Arguments.of(new GetInfoReturnCommand(
				ModuleInfo.builder()
						  .serialNo("serial")
						  .manufactureDate(LocalDate.of(2022, 2, 22))
						  .commSpecVersion("1.0.0")
						  .numBlocks(50)
						  .build()
			), "$I|serial|2022-02-22|1.0.0|50"),
			Arguments.of(new GetStateReturnCommand(
				ModuleState.builder()
						   //TODO
						   .build()
			), "$S|")
		);
	}
	
	@ParameterizedTest
	@MethodSource("getCommandsWithString")
	public void testCommandString(Command command, String expectedString) {
		assertEquals(expectedString, command.serialLine());
	}
	
}