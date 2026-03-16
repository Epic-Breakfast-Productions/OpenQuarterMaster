package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.money.Monetary;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StoredTest extends BasicTest {

	public static Stream<Arguments> getParseLabelTests(){
		Identifier gid = GenericIdentifier.builder()
							.label(FAKER.name().name())
							.value(FAKER.idNumber().valid())
							.build();
		LinkedHashSet<Identifier> identifiers = new LinkedHashSet<>(){{
			add(gid);
		}};
		CalculatedPricing pricing = CalculatedPricing.builder()
										.label(FAKER.name().name())
										.flatPrice(Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create())
										.build();
		LinkedHashSet<CalculatedPricing> pricingSet = new LinkedHashSet<>(){{
			add(pricing);
		}};
		String att = FAKER.name().name();
		Map<String, String> atts = Map.of(att, FAKER.idNumber().valid());
		
		Integer condition = 20;
		
		ZonedDateTime expires = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");
		
		
		
		AmountStored fullAmountStored = AmountStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.storageBlock(ObjectId.get())
											.amount(UnitUtils.Quantities.UNIT_ONE)
											.identifiers(identifiers)
											.calculatedPrices(pricingSet)
											.attributes(atts)
											.condition(condition)
											.expires(expires)
											.build();
		UniqueStored fullUniqueStored = UniqueStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.storageBlock(ObjectId.get())
											.identifiers(identifiers)
											.build();
		
		return Stream.of(
			//id
			Arguments.of(fullAmountStored, "{id}", fullAmountStored.getId().toHexString()),
			//amounts
			Arguments.of(fullAmountStored, "{amt}", "1 units"),
			Arguments.of(fullUniqueStored, "{amt}", "1 units"),
			//condition
			Arguments.of(fullAmountStored, "{cnd}", "20%"),
			Arguments.of(fullUniqueStored, "{cnd}", "-%"),
			//expiration
			Arguments.of(fullAmountStored, "{exp}", "12/03/2007"),
			Arguments.of(fullAmountStored, "{exp;LLL/yyyy}", "Dec/2007"),
			Arguments.of(fullUniqueStored, "{exp}", "-"),
			//general Ids
			Arguments.of(fullAmountStored, "{ident;"+gid.getLabel()+"}", gid.getValue()),
			Arguments.of(fullAmountStored, "{ident;foo}", "#E#"),
			//Pricing
			Arguments.of(fullAmountStored, "{price;"+pricing.getLabel()+"}", "$1.00"),
			Arguments.of(fullAmountStored, "{price;foo}", "#E#"),
			//Atts
			Arguments.of(fullAmountStored, "{att;"+att+"}", atts.get(att)),
			Arguments.of(fullAmountStored, "{att;foo}", "#E#"),
			//combined
			Arguments.of(
				fullAmountStored,
				"-{id} - {amt} - {cnd} - {ident;"+gid.getLabel()+"} - {price;"+pricing.getLabel()+"} - {att;"+att+"}",
				"-" + fullAmountStored.getId().toHexString() +
				" - 1 units" +
				" - 20%" +
				" - " + gid.getValue() +
				" - $1.00" +
				" - " + atts.get(att)
			)
		);
	}
	

	@ParameterizedTest
	@MethodSource("getParseLabelTests")
	public void testParseLabel(Stored stored, String format, String expected){
		log.info("Testing formatting label '{}' for: {}, format, stored", format, stored);
		
		String result = Stored.parseLabel(stored, format);
		
		assertEquals(expected, result);
	}
	
}
