package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.service.mongo.image.ImageService;

import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class ImageServiceTest extends RunningServerTest { //extends MongoHistoriedFileServiceTest<Image, ImageService> {
	private static final Color COLOR_CENTER = new Color(255, 0, 0);
	private static final Color COLOR_TOP_LEFT = new Color(0, 255, 0);
	private static final Color COLOR_TOP_RIGHT = new Color(0, 0, 255);
	private static final Color COLOR_BOT_LEFT = new Color(255, 255, 255);
	private static final Color COLOR_BOT_RIGHT = new Color(255, 255, 0);
	private static final Color COLOR_CROSS = new Color(0, 0, 0);

	private enum AtVal {
		CENTER,
		TOP_LEFT,
		TOP_RIGHT,
		BOT_LEFT,
		BOT_RIGHT,
		CENTER_LEFT,
		CENTER_RIGHT,
		CENTER_UP,
		CENTER_DOWN
	}

	private static Color getColorAt(BufferedImage image, int x, int y) {
		int clr = image.getRGB(x, y);
		return new Color(
			(clr & 0x00ff0000) >> 16,
			(clr & 0x0000ff00) >> 8,
			clr & 0x000000ff
		);
	}

	private static void assertImageSameAt(BufferedImage imageOrig, BufferedImage imageOut, AtVal where) {
		final int origHeight = imageOrig.getHeight() - 1,
			origWidth = imageOrig.getWidth() - 1,
			origXmid = imageOrig.getWidth() / 2,
			origYmid = imageOrig.getHeight() / 2,

			outHeight = imageOut.getHeight() - 1,
			outWidth = imageOut.getWidth() - 1,
			outXmid = imageOut.getWidth() / 2,
			outYmid = imageOut.getHeight() / 2;


		int origX = 0, origY = 0, outX = 0, outY = 0;
		switch (where) {
			case CENTER:
				origX = origXmid;
				origY = origYmid;
				outX = outXmid;
				outY = outYmid;
				break;
			case TOP_LEFT:
				//                origX = 0;
				//                origY = 0;
				//                outX = 0;
				//                outY = 0;
				break;
			case TOP_RIGHT:
				origX = origWidth;
				//                origY = 0;
				outX = outWidth;
				//                outY = 0;
				break;
			case BOT_LEFT:
				//                origX = 0;
				origY = origHeight;
				//                outX = 0;
				outY = outHeight;
				break;
			case BOT_RIGHT:
				origX = origWidth;
				origY = origHeight;
				outX = outWidth;
				outY = outHeight;
				break;
			case CENTER_LEFT:
				//                origX = 0;
				origY = origYmid;
				//                outX = 0;
				outY = outYmid;
				break;
			case CENTER_RIGHT:
				origX = origWidth;
				origY = origYmid;
				outX = outWidth;
				outY = outYmid;
				break;
			case CENTER_UP:
				origX = origXmid;
				//                origY = 0;
				outX = outXmid;
				//                outY = 0;
				break;
			case CENTER_DOWN:
				origX = origXmid;
				origY = origHeight;
				outX = outXmid;
				outY = outHeight;
				break;
		}

		Color colorOrig = getColorAt(imageOrig, origX, origY);
		Color colorOut = getColorAt(imageOut, outX, outY);
		log.debug("Colors at {}({}:{}/{}:{}): {}/{}", where, origX, origY, outX, outY, colorOrig, colorOut);

		//gifs seem to result in some color shifting, hence allowable delta
		assertEquals(
			colorOrig.getRed(),
			colorOut.getRed(),
			10
		);
		assertEquals(
			colorOrig.getGreen(),
			colorOut.getGreen(),
			10
		);
		assertEquals(
			colorOrig.getBlue(),
			colorOut.getBlue(),
			10
		);
	}

	public static Stream<Arguments> getTestImages() {
		return Stream.of(
			Arguments.of("/testFiles/test_image.png"),
			Arguments.of("/testFiles/test_image.jpeg"),
			Arguments.of("/testFiles/test_image.bmp"),
			Arguments.of("/testFiles/test_image.gif")
//						Arguments.of("/testFiles/test_image_big.png"),
//			Arguments.of("/testFiles/test_image_big_tall.png")
		);
	}

	@Inject
	ImageService imageService;

	@BeforeEach
	@AfterEach
	public void triggerGC() {
		log.info("Triggering Garbage Collector.");
		System.gc();
	}

	@ParameterizedTest
	@MethodSource("getTestImages")
	public void resizeImageTest(String imageFile) throws IOException {
		BufferedImage imageIn = ImageIO.read(ImageServiceTest.class.getResourceAsStream(imageFile));

		StopWatch sw = StopWatch.createStarted();
		BufferedImage imageOut = this.imageService.resizeImage(imageIn);
		sw.stop();
		log.info("Took {} to resize image.", sw);

		for (AtVal cur : AtVal.values()) {
			assertImageSameAt(imageIn, imageOut, cur);
		}
	}

	@ParameterizedTest
	@MethodSource("getTestImages")
	public void addImageTest(String imageFile) throws IOException {
		User user = this.getTestUserService().getTestUser();
		File file = new File(ImageServiceTest.class.getResource(imageFile).getFile());
		this.imageService.add(
			DEFAULT_TEST_DB_NAME,
			tech.ebp.oqm.core.api.model.object.media.Image.builder()
				.fileName(file.getName())
				.build(),
			file,
			user
		);

	}


	//TODO:: test real CRUD


}