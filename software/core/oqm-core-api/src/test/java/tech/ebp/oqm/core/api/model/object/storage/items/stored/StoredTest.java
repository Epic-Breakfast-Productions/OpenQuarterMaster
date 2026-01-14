package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.Generic;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.ProvidedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StoredTest extends BasicTest {

	public static Stream<Arguments> getParseLabelTests(){
		GeneralId gid = Generic.builder()
							.label(FAKER.name().name())
							.value(FAKER.idNumber().valid())
							.build();
		LinkedHashSet<GeneralId> generalIds = new LinkedHashSet<>(){{
			add(gid);
		}};
		UniqueId uid = ProvidedUniqueId.builder()
							.label(FAKER.name().name())
							.value(FAKER.idNumber().valid())
							.build();
		LinkedHashSet<UniqueId> uniqueIds = new LinkedHashSet<>(){{
			add(uid);
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
		
		
		AmountStored fullAmountStored = AmountStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.storageBlock(ObjectId.get())
											.amount(UnitUtils.Quantities.UNIT_ONE)
											.generalIds(generalIds)
											.uniqueIds(uniqueIds)
											.calculatedPrices(pricingSet)
											.attributes(atts)
											.build();
		UniqueStored fullUniqueStored = UniqueStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.storageBlock(ObjectId.get())
											.generalIds(generalIds)
											.build();
		
		return Stream.of(
			//id
			Arguments.of(fullAmountStored, "{id}", fullAmountStored.getId().toHexString()),
			//amounts
			Arguments.of(fullAmountStored, "{amt}", "1 units"),
			Arguments.of(fullUniqueStored, "{amt}", "1 units"),
			//general Ids
			Arguments.of(fullAmountStored, "{gid;"+gid.getLabel()+"}", gid.getValue()),
			Arguments.of(fullAmountStored, "{gid;foo}", "#E#"),
			//unique Ids
			Arguments.of(fullAmountStored, "{uid;"+uid.getLabel()+"}", uid.getValue()),
			Arguments.of(fullAmountStored, "{uid;foo}", "#E#"),
			//Pricing
			Arguments.of(fullAmountStored, "{price;"+pricing.getLabel()+"}", "$1.00"),
			Arguments.of(fullAmountStored, "{price;foo}", "#E#"),
			//Atts
			Arguments.of(fullAmountStored, "{att;"+att+"}", atts.get(att)),
			Arguments.of(fullAmountStored, "{att;foo}", "#E#"),
			//combined
			Arguments.of(
				fullAmountStored,
				"-{id} - {amt} - {gid;"+gid.getLabel()+"} - {uid;"+uid.getLabel()+"} - {price;"+pricing.getLabel()+"} - {att;"+att+"}",
				"-" + fullAmountStored.getId().toHexString() +
				" - 1 units" +
				" - " + gid.getValue() +
				" - " + uid.getValue() +
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
