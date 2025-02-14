package tech.ebp.oqm.core.baseStation.testResources.ui.utilities;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

@Slf4j
public class ItemsUiUtils {

	public static Stream<Arguments> itemTypeArgs(){
		return Stream.of(
			Arguments.of("BULK"),
			Arguments.of("AMOUNT_LIST"),
			Arguments.of("UNIQUE_MULTI"),
			Arguments.of("UNIQUE_SINGLE")
		);
	}
}
