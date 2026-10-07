package tech.ebp.oqm.plugin.mssController.testResources.testClasses;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModule;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public abstract class RunningServerTest {

	private static final Path IMAGE_OUTPUT_DIR = Path.of("build/test-results/test/outputFiles/");

	@Getter
	protected TestInfo testInfo;
	@Getter
	protected Path imageOutputDir;

	@BeforeEach
	public void setup(TestInfo testInfo) throws IOException {
		this.testInfo = testInfo;

		this.imageOutputDir = IMAGE_OUTPUT_DIR.resolve(
			testInfo.getTestClass().get().getSimpleName() + "/" +
			testInfo.getDisplayName()
				.replaceAll("\\(", "")
				.replaceAll("\\)", "")
		);
		Files.createDirectories(this.imageOutputDir);

		try (Stream<Path> walk = Files.walk(this.imageOutputDir)) {
			walk.sorted(Comparator.reverseOrder())
				.filter(path->!path.equals(this.imageOutputDir)) // Skip the root directory itself
				.forEach(path->{
					try {
						Files.delete(path);
					} catch(IOException e) {
						log.error("Failed to delete {}: {}", path, e.getMessage());
					}
				});
		}

		log.info("results dir: {}", this.imageOutputDir);

	}


	protected void saveModuleInfo(
		TestModule module,
		String subDir,
		String name
	) throws IOException {
		Path outputPath = this.imageOutputDir;
		if(subDir != null && !subDir.isBlank()) {
			outputPath = outputPath.resolve(subDir);
		}

		Files.createDirectories(outputPath);

		outputPath = outputPath.resolve(
			(name == null ? "" : name) + module.getModuleInfo().getSerialId() + ".svg"
		);

		try (OutputStream os = Files.newOutputStream(outputPath)) {
			os.write(module.generateStateImage().getBytes());
		}
	}


	@AfterEach
	public void postTest() throws IOException {

		//record image of each module
		for (TestModule module : TestModuleResource.getTestModules()) {

			this.saveModuleInfo(
				module,
				"finalState",
				null
			);
		}

		//reset module
		TestModuleResource.resetModuleState();
	}
}
