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
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.StoredNotificationStatus;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.CalculatedPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
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
public abstract class Stored extends ImagedMainObject implements FileAttachmentContaining {
	
	public static final int CUR_SCHEMA_VERSION = 4;
	
	private static final Pattern LABEL_PARTS_PATTERN = Pattern.compile("\\{[^}]*}");
	private static final String LABEL_PLACEHOLDER_PART_DELIM = ";";
	private static final String LABEL_PLACEHOLDER_ARG_DELIM = LABEL_PLACEHOLDER_PART_DELIM;
	private static final String LABEL_ERROR = "#E#";
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
	 * @param stored
	 * @param format
	 *
	 * @return
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
							stored.getExpires() == null?
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
	
	public abstract StoredType getType();
	
	/**
	 * The {@link InventoryItem} this stored is associated with.
	 */
	@NonNull
	@NotNull
	private ObjectId item;
	
	/**
	 * The {@link StorageBlock} this stored is stored in.
	 */
	//TODO:: determine if we can use null for 'no block in particular'
	private ObjectId storageBlock;
	
	/**
	 * The general ids that apply to this stored, but not to all stored (as specified in the associated item)
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
	private ZonedDateTime expires = null;
	
	/**
	 * Prices for this stored item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@UniqueLabeledCollection
	private LinkedHashSet<@NotNull StoredPricing> prices = new LinkedHashSet<>();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PRIVATE)
	@lombok.Builder.Default
	private LinkedHashSet<@NotNull CalculatedPricing> calculatedPrices = null;
	
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
	private StoredNotificationStatus notificationStatus = new StoredNotificationStatus();
	
	/**
	 * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
	 */
	@Max(100)
	@Min(0)
	@lombok.Builder.Default
	private Integer condition = null;
	
	/**
	 * Notes on the condition on the thing(s) stored.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String conditionNotes = "";
	
	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<@NotNull ObjectId> imageIds = new ArrayList<>();
	
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
	 * Label format to use if there is not one specified in this stored, or one in the item.
	 *
	 * @return
	 */
	@JsonIgnore
	protected abstract String getDefaultLabelFormat();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PRIVATE)
	@lombok.Builder.Default
	private String labelText = null;
	
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
	
	public void applyDefaultsFromItem(InventoryItem item) {
		if (!this.getItem().equals(item.getId())) {
			throw new IllegalArgumentException("Item ID's do not match");
		}
		this.calculatePrices(item);
		this.processLabel(item);
	}
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
