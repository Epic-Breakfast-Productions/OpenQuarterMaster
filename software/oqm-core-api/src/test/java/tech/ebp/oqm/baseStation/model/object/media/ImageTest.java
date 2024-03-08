package tech.ebp.oqm.baseStation.model.object.media;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.model.rest.media.ImageCreateRequest;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test image facts:
 * <p>
 * - center color: FF0000 - TL quad:      00FF00 - TR quad:      0000FF - BL quad:      000000 - TR quad:      FFFF00
 */
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class ImageTest extends BasicTest {

}