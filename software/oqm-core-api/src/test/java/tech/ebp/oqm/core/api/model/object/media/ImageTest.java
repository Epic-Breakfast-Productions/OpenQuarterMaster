package tech.ebp.oqm.core.api.model.object.media;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

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