package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ImageCrud.class)
class ImageCrudTest extends RunningServerTest {


	//TODO:: add image

	//TODO:: search, empty

	//TODO::  search, results

	//TODO:: update object

	//TODO:: update image file

	//TODO:: get revision object

	//TODO:: get revision data

	//TODO:: history
}
