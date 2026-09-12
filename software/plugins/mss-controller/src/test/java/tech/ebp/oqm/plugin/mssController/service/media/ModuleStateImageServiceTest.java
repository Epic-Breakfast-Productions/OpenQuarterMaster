package tech.ebp.oqm.plugin.mssController.service.media;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockWeightState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightPowerState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightSetting;
import tech.ebp.oqm.plugin.mssController.testResources.testClasses.RunningServerTest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@QuarkusTest
class ModuleStateImageServiceTest extends RunningServerTest {
	private static final String IMAGE_OUTPUT_DIR = "build/test-results/test/moduleStateImageService";

	protected static ModuleInfo getModuleInfo(Capabilities capabilities) {
		return new ModuleInfo(
			"1.0.0",
			"1.2.3",
			UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
			"2026-07-13",
			64,
			capabilities
		);
	}

	protected static ModuleInfo getModuleInfo() {
		return getModuleInfo(Capabilities.builder()
								 .blockLights(true)
								 .blockLightColor(true)
								 .blockLightBrightness(true)
								 .itemEventReporting(true)
								 .build());
	}

	protected static ModuleState getModuleState(ModuleInfo info) {
		return ModuleState.builder()
				   .storageBlocks(
					   IntStream.range(1, info.getNumBlocks() + 1)
						   .mapToObj(i->BlockState.builder()
											.blockNum(i)
											.lightSettings(
												BlockLightSetting.builder()
													.powerState(BlockLightPowerState.OFF)
													.color("#FFFFFF").build()
											)
											.weightState(
												BlockWeightState.builder()
													.weightUnit("lbs")
													.weightValue(5.0)
													.weightStr("5lbs")
													.build()
											)
											.build())
						   .toList()
				   )
				   .build();
	}

//	@Inject
//	ModuleStateImageService moduleStateImageService;

	@Test
	public void testGetModuleStateImage() throws IOException {
		ModuleInfo info = getModuleInfo();
		ModuleState state = getModuleState(info);

		String data = ModuleStateImageService.generateStateImage(
			getModuleInfo(),
			state
		);

		Path output = this.getImageOutputDir().resolve( "test.svg");
		Files.createDirectories(output.getParent());

		try(
			OutputStream os = Files.newOutputStream(output);
			Writer writer = new OutputStreamWriter(os);
		){
			writer.write(data);
		}
	}


}
