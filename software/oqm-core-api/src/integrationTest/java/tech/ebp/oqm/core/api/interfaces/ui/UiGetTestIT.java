package tech.ebp.oqm.core.api.interfaces.ui;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusIntegrationTest
public class UiGetTestIT extends UiGetTest {

}
