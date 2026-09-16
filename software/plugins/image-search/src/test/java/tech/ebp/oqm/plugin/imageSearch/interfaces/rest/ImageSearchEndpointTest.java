package tech.ebp.oqm.plugin.imageSearch.interfaces.rest;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ImageSearchEndpoint.class)
class ImageSearchEndpointTest extends RunningServerTest {


	@BeforeAll
	public static void setUp(){
//		this.setupOqmDb(TEST_DB);
	}


	@Test
	public void testSearchEndpointBasic(){

		//TODO:: basic search


	}

	@Test
	public void testSearchEndpointMulti(){
		this.setupOqmDb(TEST_DB);
		//TODO:: multiple searches at once
	}




}
