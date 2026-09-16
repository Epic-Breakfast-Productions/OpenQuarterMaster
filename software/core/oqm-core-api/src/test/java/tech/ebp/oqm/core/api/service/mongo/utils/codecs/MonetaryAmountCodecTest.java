package tech.ebp.oqm.core.api.service.mongo.utils.codecs;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class MonetaryAmountCodecTest extends RunningServerTest {

	@Inject
	MonetaryAmountCodec codec;


	public static Stream<Arguments> amountArgs() {
		return Stream.of(
			Arguments.of(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create()),
			Arguments.of(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1000).create())
		);
	}

	@ParameterizedTest
	@MethodSource("amountArgs")
	public void testPricingFormat(MonetaryAmount pricing) {
		log.info("Pricing initially: {}", pricing);
		BsonDocument doc = new BsonDocument();
		try(
			BsonDocumentWriter writer = new BsonDocumentWriter(doc)
		) {
			this.codec.encode(writer, pricing, null);

			writer.flush();
		}

		log.info("Pricing Doc: {}", doc);

		MonetaryAmount afterPricing;
		try(
			BsonDocumentReader reader = new BsonDocumentReader(doc)
			) {
			afterPricing = this.codec.decode(reader, null);
		}

		log.info("Pricing after: {}", afterPricing);
		assertEquals(pricing, afterPricing);
	}
}
