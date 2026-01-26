package tech.ebp.oqm.core.characteristics.utils;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ColorUtilsTest {
	
	@Inject
	ColorUtils colorUtils;
	
	public static Stream<Arguments> colorTests() {
		return Stream.of(
			Arguments.of("red", Color.RED),
			Arguments.of("Red", Color.RED),
			Arguments.of("#FF0000", Color.RED)
		);
	}
	
	@ParameterizedTest
	@MethodSource("colorTests")
	public void testColorUtils(String inputStr, Color expectedColor) {
		assertEquals(expectedColor, this.colorUtils.getColor(inputStr));
	}
}