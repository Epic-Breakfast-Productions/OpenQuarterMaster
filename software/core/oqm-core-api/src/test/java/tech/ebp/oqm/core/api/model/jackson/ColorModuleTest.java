package tech.ebp.oqm.core.api.model.jackson;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

import java.awt.*;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class ColorModuleTest extends BasicTest {
	
	private static Stream<Arguments> hexColorArgs() {
		return Stream.of(
			Arguments.of("#000000", Color.BLACK),
			Arguments.of("#FFFFFF", Color.WHITE)
//			Arguments.of("#FFF", Color.WHITE)//no-go
		);
	}
	
	private final ColorModule module = new ColorModule();
	
	@ParameterizedTest
	@MethodSource("hexColorArgs")
	public void toColorStaticUtilTest(String hex, Color expectedColor) {
		Color gotten = ColorModule.toColor(hex);
		assertEquals(expectedColor, gotten);
	}
	
	@ParameterizedTest
	@MethodSource("hexColorArgs")
	public void toHexStringStaticUtilTest(String expectedHex, Color color) {
		String gotten = ColorModule.toHexString(color);
		assertEquals(expectedHex, gotten);
	}
	
	//TODO:: test parse from/to json
	
}