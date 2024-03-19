package tech.ebp.oqm.baseStation.model.object.media.file;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.object.media.FileHashes;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FileHashesTest extends BasicTest {
	
	private static final String TEST_FILE = "/test_image_big.png";
	private static final String EXPECTED_MD5 = "22668f94a2075f367b9bbf0f3113dab3";
	private static final String EXPECTED_SHA1 = "581c4a28da688ca2c36eab49064a2892060893c7";
	private static final String EXPECTED_SHA256 = "7c09c10e5424bdf87cf224cc3f2c7d1cbb79b0601137e958fffe80bce257c039";
	
	@Test
	public void testFromFile() {
		FileHashes hashes;
		
		File testFile = new File(FileHashesTest.class.getResource(TEST_FILE).getFile());
		
		hashes = FileHashes.fromFile(testFile);
		
		assertEquals(EXPECTED_MD5, hashes.getMd5(), "MD5 wrong");
		assertEquals(EXPECTED_SHA1, hashes.getSha1(), "SHA1 wrong");
		assertEquals(EXPECTED_SHA256, hashes.getSha256(), "SHA256 wrong");
	}
}