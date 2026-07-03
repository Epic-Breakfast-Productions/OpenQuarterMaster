package tech.ebp.oqm.plugin.mssController.service.media;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockWeightReport;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightPowerState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightSetting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
//@QuarkusTest
class ModuleStateImageServiceTest {
	private static final String IMAGE_OUTPUT_DIR = "build/test-results/test/moduleStateImageService";

	protected static ModuleInfo getModuleInfo(Capabilities capabilities) {
		return new ModuleInfo(
			"1.0.0",
			"1.2.3",
			UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
			LocalDate.of(2026, 1, 1),
			64,
			capabilities
		);
	}

	protected static ModuleInfo getModuleInfo() {
		return getModuleInfo(Capabilities.builder()
								 .blockLights(true)
								 .blockLightColor(true)
								 .blockLightBrightness(true)
								 .blockWeightReporting(true)
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
											.weightReport(
												BlockWeightReport.builder()
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

		Path output = Path.of(IMAGE_OUTPUT_DIR, "test.svg");
		Files.createDirectories(output.getParent());

		try(
			OutputStream os = Files.newOutputStream(output);
			Writer writer = new OutputStreamWriter(os);
		){
			writer.write(data);
		}
	}


}
