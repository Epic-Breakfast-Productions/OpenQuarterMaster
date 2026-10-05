package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.StoredNotificationStatus;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredState;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.validation.annotations.UniqueLabeledCollection;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidStoredLabelFormat;

import javax.measure.Quantity;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Describes an item stored in the system.
 * <p>
 * A {@code Stored} object represents a specific physical instance or quantity of an
 * {@link InventoryItem} that is held by the system, tracking details such as its
 * current {@link StoredState}, {@link Identifier}s, pricing, expiration, and
 * condition. It is the concrete, per-instance counterpart to the generic
 * {@link InventoryItem} definition.
 * <p>
 * This is an abstract class; use one of its subtypes via the builder:
 * <ul>
 *     <li>{@link AmountStored} - a quantity of an item (e.g., 5 meters of pipe)</li>
 *     <li>{@link UniqueStored} - a single unique item (e.g., one serial-numbered unit)</li>
 * </ul>
 * <p>
 * Example - creating a stored object and applying item defaults:
 * <pre>{@code
 * Stored stored = AmountStored.builder()
 *     .item(itemId)
 *     .amount(UnitUtils.Quantities.ONE)
 *     .build();
 * stored.applyDefaultsFromItem(inventoryItem); // computes prices and label
 * }</pre>
 * <p>
 * Serialization: subclasses are discriminated by the {@code type} property
 * ({@code AMOUNT}/{@code UNIQUE}) and extend {@link ImagedMainObject}, giving
 * them an ID, history, images, and the ability to hold file attachments.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = AmountStored.class, name = "AMOUNT"),
	@JsonSubTypes.Type(value = UniqueStored.class, name = "UNIQUE")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
@Schema(oneOf = {AmountStored.class, UniqueStored.class})
public abstract class Stored extends ImagedMainObject implements FileAttachmentContaining {

	/**
	 * The current schema version of this object, used for data migration.
	 */
	public static final int CUR_SCHEMA_VERSION = 5;

	/** Matches {@code {placeholder}} tokens within a label format string. */
	private static final Pattern LABEL_PARTS_PATTERN = Pattern.compile("\\{[^}]*}");

	/** Delimiter separating the placeholder name from its arguments. */
	private static final String LABEL_PLACEHOLDER_PART_DELIM = ";";

	/** Delimiter separating multiple arguments within a placeholder. */
	private static final String LABEL_PLACEHOLDER_ARG_DELIM = LABEL_PLACEHOLDER_PART_DELIM;

	/** Marker emitted in place of an unresolvable placeholder value. */
	private static final String LABEL_ERROR = "#E#";

	/** Format used for {@code exp} placeholders when no explicit format is given. */
	private static final DateTimeFormatter LABEL_DT_DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");


	/**
	 *
	 * Supported format variables:
	 * <ul>
	 *     <li>
	 *         id: {@code {id;<number of digits, startting from the end (optional)>}}- The ID of the stored object. Parameter to specify the number of characters to actually
	 *         display. Defaults to full Id.
	 *     </li>
	 *     <li>
	 *         amt: {@code {amt}}- Use the stored amount.
	 *     </li>
	 *     <li>
	 *         cnd: {@code {cnd}}- Use the condition as a percentage.
	 *     </li>
	 *     <li>
	 *         exp: {@code {exp;<datetime format>}}- Writes out the expiration for this stored. Format should match standard Java datetime syntax. Default format is {@code MM/dd/yyyy}
	 *     </li>
	 *     <li>
	 *         ident: {@code {ident;<name of identifier>}}- Use an identifier value.
	 *     </li>
	 *     <li>
	 *         price: {@code {price;<name of calculated price>}}- Use a price value. Does not include price label.
	 *     </li>
	 *     <li>
	 *         att: {@code {att;<key for attribute>}}-  Use an attribute value.
	 *     </li>
	 * </ul>
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>
	 *         {@code {id}} -> {@code 01/30/2020-24:30:30-00001}
	 *     </li>
	 * </ul>
	 *
	 * @param stored the stored object to draw values from.
	 * @param format the label format string containing at least one {@code {placeholder}}.
	 *              Must not be null, blank, or contain leading/trailing whitespace.
	 * @return the rendered label text with all placeholders substituted.
	 * @throws IllegalArgumentException if the format is invalid, contains an unknown
	 *                                  placeholder, or if no placeholders are present.
	 */
	public static String parseLabel(Stored stored, String format) {
		if (format == null || format.isBlank()) {
			throw new IllegalArgumentException("Format cannot be null, blank, or empty.");
		}

		if (!format.equals(format.trim())) {
			throw new IllegalArgumentException("Format cannot contain leading or trailing whitespace.");
		}

		StringBuilder sb = new StringBuilder();
		AtomicInteger numPlaceholders = new AtomicInteger();
		AtomicInteger curStart = new AtomicInteger();
		AtomicInteger lastEnd = new AtomicInteger();

		LABEL_PARTS_PATTERN.matcher(format).results()
			.forEach((MatchResult result)->{
				numPlaceholders.getAndIncrement();
				sb.append(format, curStart.get(), result.start());
				curStart.set(result.end());
				lastEnd.set(result.end());

				String placeholder = result.group();
				//				log.debug("placeholder: {}", placeholder);

				String[] parts = placeholder.replace("{", "").replace("}", "").split(LABEL_PLACEHOLDER_PART_DELIM, 2);
				String placeholderType = parts[0].toLowerCase();
				String[] args = parts.length > 1 ? parts[1].split(LABEL_PLACEHOLDER_ARG_DELIM) : new String[0];

				//				log.debug("placeholderType: {}, args: {}", placeholderType, args);

				switch (placeholderType) {
					case "id":
						sb.append(stored.getId());
						break;
					case "amt":
						Quantity<?> amount;
						if (stored instanceof AmountStored) {
							amount = ((AmountStored) stored).getAmount();
						} else {
							amount = UnitUtils.Quantities.UNIT_ONE;
						}

						sb.append(amount.toString());
						break;
					case "cnd":
						Integer condition = stored.getCondition();

						sb.append(
							condition == null ?
								"-" :
								condition.toString()
						);
						sb.append('%');
						break;
					case "exp":
						DateTimeFormatter formatter = LABEL_DT_DEFAULT_FORMATTER;

						if (args.length > 0) {
							formatter = DateTimeFormatter.ofPattern(args[0]);
						}

						sb.append(
							stored.getExpires() == null ?
								'-' :
								stored.getExpires().format(formatter)
						);
						break;
					case "ident":
					case "price":
						if (args.length != 1) {
							throw new IllegalArgumentException("Must specify exactly one argument for 'ident', and 'price'.");
						}

						String label = args[0];
						Optional<Labeled> foundLabel = Labeled.findLabeledInSet(
							label,
							(Collection<Labeled>) switch (placeholderType) {
								case "ident" -> stored.getIdentifiers();
								case "price" -> stored.getCalculatedPrices();
								default -> new ArrayList<Identifier>(0);
							}
						);

						if (foundLabel.isPresent()) {
							Labeled cur = foundLabel.get();

							if (cur instanceof Identifier) {
								sb.append(((Identifier) cur).getValue());
							} else if (cur instanceof CalculatedPricing) {
								sb.append(((CalculatedPricing) cur).getTotalPriceString());
							}
						} else {
							sb.append(LABEL_ERROR);
						}
						break;
					case "att":
						if (args.length != 1) {
							throw new IllegalArgumentException("Must specify exactly one argument for 'att'.");
						}

						sb.append(stored.getAttributes().getOrDefault(args[0], LABEL_ERROR));

						break;
					default:
						throw new IllegalArgumentException("Unknown placeholder type: '" + placeholderType + "'");
				}
			});

		if (numPlaceholders.intValue() == 0) {
			throw new IllegalArgumentException("No placeholders found in format.");
		}

		sb.append(format, lastEnd.get(), format.length());

		String newIdentifier = sb.toString();

		return newIdentifier;
	}

	/**
	 * The subtype discriminator for this stored object ({@code AMOUNT} or {@code UNIQUE}).
	 */
	@Schema(required = true, description = "The type of stored object.")
	public abstract StoredType getType();

	/**
	 * The {@link InventoryItem} this stored is associated with.
	 */
	@NonNull
	@NotNull
	@Schema(description = "The item that this stored is associated with.")
	private ObjectId item;

	/**
	 * The state describing how this item is stored. Example, stored in a storage block, or installed in another item.
	 */
	@Schema(description = "The state describing how this item is stored. Example, stored in a storage block, or installed in another item.")
	private StoredState state;

	/**
	 * Checks whether this stored object is currently in the given {@link StoredStateType}.
	 *
	 * @param type the state type to check against (e.g., {@code STORED}, {@code IN_TRANSIT}).
	 * @return {@code true} if the current state matches, or if no state is set.
	 */
	public boolean isState(StoredStateType type) {
		return this.getState() != null && this.getState().getType().equals(type);
	}

	/**
	 * Identifiers that apply to this specific stored instance, but not to all
	 * stored of the associated item (e.g., a serial number on one of many).
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@UniqueLabeledCollection
	private LinkedHashSet<@NotNull Identifier> identifiers = new LinkedHashSet<>();

	/**
	 * When the item(s) held expire. Null if it does not expire.
	 */
	@lombok.Builder.Default
	@Schema(required = false, description = "When the item(s) held expire. Null if it does not expire.", examples = {"null", "2022-03-10T12:15:50"})
	private ZonedDateTime expires = null;

	/**
	 * Prices for this stored item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@UniqueLabeledCollection
	private LinkedHashSet<@NotNull StoredPricing> prices = new LinkedHashSet<>();

	/**
	 * Prices for this stored item, derived from the stored's own prices plus
	 * defaults not already present. Read-only; recomputed via {@link #calculatePrices(InventoryItem)}.
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PRIVATE)
	@lombok.Builder.Default
	private LinkedHashSet<@NotNull CalculatedPricing> calculatedPrices = null;

	/**
	 * Recalculates this stored's {@link CalculatedPricing} set from its own prices
	 * merged with the item's default prices.
	 *
	 * @param item the associated item, whose default prices may be carried over.
	 * @return {@code true} if the calculated prices changed, {@code false} otherwise.
	 */
	protected boolean calculatePrices(InventoryItem item) {
		LinkedHashSet<CalculatedPricing> storedPrices = this.getPrices().stream()
															.map((p)->p.calculatePrice(this)).collect(Collectors.toCollection(LinkedHashSet::new));
		//add prices not in stored's from item
		for (StoredPricing itemPrice : item.getDefaultPrices()) {
			if (
				storedPrices.stream()
					.noneMatch((price)->{
						return price.getLabel().equals(itemPrice.getLabel());
					})
			) {
				storedPrices.add(itemPrice.calculatePrice(this).setFromDefault(true));
			}
		}

		boolean output = this.getCalculatedPrices() == null || !this.getCalculatedPrices().equals(storedPrices);
		this.setCalculatedPrices(storedPrices);

		return output;
	}

	/**
	 * Statuses about this stored object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "State of the notifications sent about this item stored.")
	private StoredNotificationStatus notificationStatus = new StoredNotificationStatus();

	/**
	 * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
	 */
	@Max(100)
	@Min(0)
	@lombok.Builder.Default
	@Schema(required = false, description = "The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.", examples = {"null", "100"})
	private Integer condition = null;

	/**
	 * Notes on the condition on the thing(s) stored.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Notes on the condition on the thing(s) stored.", examples = {""})
	private String conditionNotes = "";

	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<@NotNull ObjectId> imageIds = new ArrayList<>();

	/**
	 * IDs of files attached to this stored object. Populated by {@link #applyDefaultsFromItem(InventoryItem)}.
	 */
	@lombok.Builder.Default
	private Set<@NotNull ObjectId> attachedFiles = new HashSet<>();

	/**
	 * The format to use for the label.
	 * <p>
	 * To use/ set to default specified by the Item, update using `null` or blank value.
	 * <p>
	 * Format spec described by {@link #parseLabel(Stored, String)}
	 */
	@lombok.Builder.Default
	@ValidStoredLabelFormat
	private String labelFormat = null;

	/**
	 * The fallback label format used when neither this stored nor the associated
	 * item specifies one.
	 *
	 * @return the default label format string.
	 */
	@JsonIgnore
	protected abstract String getDefaultLabelFormat();

	/**
	 * The rendered label text, generated by {@link #processLabel(InventoryItem)}
	 * from the resolved label format. Read-only.
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PRIVATE)
	@lombok.Builder.Default
	@Schema(required = false, description = "A generated label text.")
	private String labelText = null;

	/**
	 * Resolves the label format (stored, then item, then default) and renders it
	 * into {@link #labelText}.
	 *
	 * @param item the associated item, consulted for its default label format.
	 */
	private void processLabel(InventoryItem item) {
		String labelFormat = this.getLabelFormat();
		if (labelFormat == null) {
			labelFormat = item.getDefaultLabelFormat();
		}
		if (labelFormat == null) {
			labelFormat = this.getDefaultLabelFormat();
		}

		this.labelText = parseLabel(this, labelFormat);
	}

	/**
	 * Applies item-derived defaults to this stored: recalculates prices from the
	 * item's defaults and renders the label text.
	 * <p>
	 * Example:
	 * <pre>{@code
	 * stored.applyDefaultsFromItem(inventoryItem);
	 * System.out.println(stored.getLabelText());
	 * }</pre>
	 *
	 * @param item the associated item; its ID must match this stored's item.
	 * @throws IllegalArgumentException if the item IDs do not match.
	 */
	public void applyDefaultsFromItem(InventoryItem item) {
		if (!this.getItem().equals(item.getId())) {
			throw new IllegalArgumentException("Item ID's do not match");
		}
		this.calculatePrices(item);
		this.processLabel(item);
	}

	/**
	 * @return {@value #CUR_SCHEMA_VERSION}
	 */
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
