package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredInBlock;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.money.Monetary;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

		String keyword = FAKER.name().name();
		List<String> keywords = List.of(keyword);

		Integer condition = 20;

		ZonedDateTime expires = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");

		AmountStored fullAmountStored = AmountStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
											.amount(UnitUtils.Quantities.UNIT_ONE)
											.identifiers(identifiers)
											.calculatedPrices(pricingSet)
											.attributes(atts)
											.keywords(keywords)
											.condition(condition)
											.expires(expires)
											.build();
		UniqueStored fullUniqueStored = UniqueStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
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

			//if-a (just has att)
			Arguments.of(fullAmountStored, "{id}{if;a;"+att+"}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString() + atts.get(att)),
			Arguments.of(fullAmountStored, "{id}{if;a;foo}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString()),
			//if-a (with att + value)
			Arguments.of(fullAmountStored, "{id}{if;a;"+att+";"+atts.get(att)+"}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString() + atts.get(att)),
			Arguments.of(fullAmountStored, "{id}{if;a;"+att+";foo}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString()),

			//if-k
			Arguments.of(fullAmountStored, "{id}{if;k;"+keyword+"}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString() + atts.get(att)),
			Arguments.of(fullAmountStored, "{id}{if;k;foo}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString()),

			//additional if tests
			Arguments.of(fullAmountStored, "{id}{if;k;"+keyword+"}{att;"+att+"}{/if}\\", fullAmountStored.getId().toHexString()+atts.get(att) + "\\"),
			Arguments.of(fullAmountStored, "{id}{if;k;"+keyword+"}-{att;"+att+"}-{/if}\\", fullAmountStored.getId().toHexString()+"-"+atts.get(att) + "-\\"),
			Arguments.of(fullAmountStored, "{if;k;foo}{att;"+att+"}{/if}{id}", fullAmountStored.getId().toHexString()),
			Arguments.of(fullAmountStored, "{id}{if;k;foo}-{att;"+att+"}-{/if}", fullAmountStored.getId().toHexString()),
			Arguments.of(fullAmountStored, "{id}{if;k;"+keyword+"}{att;"+att+"}{/if}{if;k;"+keyword+"}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString() + atts.get(att)+atts.get(att)),
			Arguments.of(fullAmountStored, "{id}{if;k;"+keyword+"}{att;"+att+"}{/if}-{if;k;"+keyword+"}{att;"+att+"}{/if}", fullAmountStored.getId().toHexString() + atts.get(att)+"-"+atts.get(att)),

			//misc
			Arguments.of(fullAmountStored, "{id}\uD83D\uDE80", fullAmountStored.getId().toHexString()+"\uD83D\uDE80"),

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
		log.info("Testing formatting label '{}' for: {}", format, stored);

		String result = Stored.parseLabel(stored, format);

		log.info("Got result: {}", result);
		assertEquals(expected, result);
	}

	public static Stream<Arguments> getParseLabelFailTests(){
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

		String keyword = FAKER.name().name();
		List<String> keywords = List.of(keyword);

		Integer condition = 20;

		ZonedDateTime expires = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");



		AmountStored fullAmountStored = AmountStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
											.amount(UnitUtils.Quantities.UNIT_ONE)
											.identifiers(identifiers)
											.calculatedPrices(pricingSet)
											.attributes(atts)
											.keywords(keywords)
											.condition(condition)
											.expires(expires)
											.build();
		UniqueStored fullUniqueStored = UniqueStored.builder()
											.id(ObjectId.get())
											.item(ObjectId.get())
											.state(StoredInBlock.builder().storageBlock(ObjectId.get()).build())
											.identifiers(identifiers)
											.build();

		return Stream.of(
			Arguments.of(null, "{id}", new NullPointerException("stored is marked non-null but is null")),
			Arguments.of(fullAmountStored, null, new NullPointerException("format is marked non-null but is null")),

			//empty, blank
			Arguments.of(fullAmountStored, "", new IllegalArgumentException("Format cannot be null, blank, or empty.")),
			Arguments.of(fullAmountStored, " \t\n", new IllegalArgumentException("Format cannot be null, blank, or empty.")),

			//trailing,leading whitespace
			Arguments.of(fullAmountStored, " {id} ", new IllegalArgumentException("Format cannot contain leading or trailing whitespace.")),

			//just a value
			Arguments.of(fullAmountStored, "foo", new IllegalArgumentException("No placeholders found in format.")),

			//just a value
			Arguments.of(fullAmountStored, "{foo}", new IllegalArgumentException("Unknown placeholder type: 'foo'")),

			//closing if
			Arguments.of(fullAmountStored, "{/if}", new IllegalArgumentException("Got to if closing statement without being in if statement.")),

			//expires
			Arguments.of(fullAmountStored, "{exp;foo}", new IllegalArgumentException("Bad datetime format specified for expiry date: Unknown pattern letter: f")),

			//general Ids
			Arguments.of(fullAmountStored, "{ident;"+gid.getLabel()+";foo}", new IllegalArgumentException("Must specify exactly one argument for 'ident', and 'price'.")),
			//Pricing
			Arguments.of(fullAmountStored, "{price;"+pricing.getLabel()+";foo}", new IllegalArgumentException("Must specify exactly one argument for 'ident', and 'price'.")),

			//Atts
			Arguments.of(fullAmountStored, "{att;"+att+";foo}", new IllegalArgumentException("Must specify exactly one argument for 'att'.")),

			//ifs
			Arguments.of(fullAmountStored, "{id}{if;a;foo}{foo}{/if}", new IllegalArgumentException("Unknown placeholder type: 'foo'")),
			Arguments.of(fullAmountStored, "{if;a;foo}{if;a;foo}{att;"+att+"}{/if}{/if}", new IllegalArgumentException("We do not currently support nested if's.")),
			Arguments.of(fullAmountStored, "{if;a;"+att+"}{if;a;"+att+"}{att;"+att+"}{/if}{/if}", new IllegalArgumentException("We do not currently support nested if's.")),
			Arguments.of(fullAmountStored, "{if;z;foo}-{/if}", new IllegalArgumentException("Unrecognized if comparison type: z")),
			Arguments.of(fullAmountStored, "{if;a;"+att+"}{att;"+att+"}{/if}", new IllegalArgumentException("Must have placeholders outside 'if' statements.")),
			Arguments.of(fullAmountStored, "{id}{if}{att;"+att+"}{/if}\\", new IllegalArgumentException("Must specify a type for 'if' statements.")),
			Arguments.of(fullAmountStored, "{id}{if;k}{att;"+att+"}{/if}\\", new IllegalArgumentException("Must specify exactly one keyword for if of type keyword.")),
			Arguments.of(fullAmountStored, "{id}{if;k;foo;bar}{att;"+att+"}{/if}\\", new IllegalArgumentException("Must specify exactly one keyword for if of type keyword.")),
			Arguments.of(fullAmountStored, "{id}{if;a}{att;"+att+"}{/if}\\", new IllegalArgumentException("Must specify an attribute or attribute and value for if of type attribute.")),
			Arguments.of(fullAmountStored, "{id}{if;a;foo;bar;baz}{att;"+att+"}{/if}\\", new IllegalArgumentException("Must specify an attribute or attribute and value for if of type attribute.")),
			Arguments.of(fullAmountStored, "{id}{if;a;"+att+"}{att;"+att+"}", new IllegalArgumentException("Must close if statement.")),
			Arguments.of(fullAmountStored, "{id}{if;a;foo}{att;"+att+"}", new IllegalArgumentException("Must close if statement."))
		);
	}

	@ParameterizedTest
	@MethodSource("getParseLabelFailTests")
	public void testParseLabelFail(Stored stored, String format, Exception expected){
		log.info("Testing bad formatting label '{}' for: {}", format, stored);

		Exception result = assertThrows(expected.getClass(), ()->Stored.parseLabel(stored, format));

		log.info("Got result error message: {}", result.getMessage());
		assertEquals(expected.getMessage(), result.getMessage());
	}
}
