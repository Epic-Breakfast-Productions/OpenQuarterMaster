package com.ebp.openQuarterMaster.lib.core.media;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test image facts:
 * <p>
 * - center color: FF0000
 * - TL quad:      00FF00
 * - TR quad:      0000FF
 * - BL quad:      000000
 * - TR quad:      FFFF00
 */
@Slf4j
class ImageTest {
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
        assertEquals(colorOrig, colorOut);
    }

    @Test
    public void resizeImageTest() throws IOException {
        BufferedImage imageIn = ImageIO.read(ImageTest.class.getResourceAsStream("/test_image.png"));

        StopWatch sw = StopWatch.createStarted();
        BufferedImage imageOut = Image.resize(imageIn);
        sw.stop();
        log.info("Took {} to resize image.", sw);

        for (AtVal cur : AtVal.values()) {
            assertImageSameAt(imageIn, imageOut, cur);
        }
    }

    @Test
    public void constructorTest() throws IOException {
        BufferedImage imageIn = ImageIO.read(ImageTest.class.getResourceAsStream("/test_image.png"));

        StopWatch sw = StopWatch.createStarted();
        Image image = new Image("Test image", imageIn);
        sw.stop();
        log.info("Took {} to construct Image. Resulting size: {}", sw, image.getDataLength());

        //TODO:: determine that data is correctly base64 encoded
    }

}