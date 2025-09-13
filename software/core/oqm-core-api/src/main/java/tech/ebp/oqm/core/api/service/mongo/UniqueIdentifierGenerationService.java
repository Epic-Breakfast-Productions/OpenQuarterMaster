package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.UniqueIdentifierGenerator;
import tech.ebp.oqm.core.api.model.rest.search.UniqueIdGeneratorSearch;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

/**
 * Service to handle applying transactions to items stored, and keeping track of what transactions have been applied.
 */
@Slf4j
@Named("UniqueIdentifierGenerationService")
@ApplicationScoped
public class UniqueIdentifierGenerationService extends MongoObjectService<UniqueIdentifierGenerator, UniqueIdGeneratorSearch, CollectionStats> {
	
	
	private static final Pattern PARTS_PATTERN = Pattern.compile("\\{[^}]*}");
	private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy-HH:mm:ss");
	private static final String PLACEHOLDER_PART_DELIM = ";";
	private static final String PLACEHOLDER_ARG_DELIM = PLACEHOLDER_PART_DELIM;
	
	/**
	 * Actually generates the next id in the sequence.
	 * <p>
	 * Supported format variables:
	 * <ul>
	 *     <li>
	 *         Datetime: {@code {dt;format}}- Insert a formatted local datetime. Format should match standard Java datetime syntax. Default format is {@code MM/dd/yyyy-HH:mm:ss}
	 *     </li>
	 *     <li>
	 *         Uuid: {@code {uuid}}- a standard java-generated uuid
	 *     </li>
	 *     <li>
	 *         Random: {@code {rand;length}}- a random series of digits and letters. Min length is 1, max length is 50. Length defaults to {@code 3}.
	 *     </li>
	 *     <li>
	 *         Increment: {@code {inc;padding;base}}- an auto-incrementing number. Options:
	 *         <ol>
	 *             <li>
	 *                 Padding: How many digits to pad out to. For example, a value of {@code 5} produces {@code 00001}. Defaults to {@code 5}
	 *             </li>
	 *             <li>
	 *                 Base: what base of number to use to increment; {@code 10} for standard digits, {@code 16} for base 16, etc. Defaults to {@code 16}
	 *             </li>
	 *         </ol>
	 *     </li>
	 * </ul>
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>
	 *         {@code {dt}-{inc}} -> {@code 01/30/2020-24:30:30-00001}
	 *     </li>
	 * </ul>
	 *
	 * @param generator the generator object to base off of.
	 *
	 * @return The next id in the sequence
	 */
	public static String getNextUniqueId(UniqueIdentifierGenerator generator) {
		String format = generator.getIdFormat();
		
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
		AtomicBoolean hadInc = new AtomicBoolean(false);
		
		PARTS_PATTERN.matcher(format).results()
			.forEach((MatchResult result)->{
				numPlaceholders.getAndIncrement();
				sb.append(format, curStart.get(), result.start());
				curStart.set(result.end());
				lastEnd.set(result.end());
				
				String placeholder = result.group();
				log.debug("placeholder: {}", placeholder);
				
				String[] parts = placeholder.replace("{", "").replace("}", "").split(PLACEHOLDER_PART_DELIM, 2);
				String placeholderType = parts[0].toLowerCase();
				String[] args = parts.length > 1 ? parts[1].split(PLACEHOLDER_ARG_DELIM) : new String[0];
				
				log.debug("placeholderType: {}, args: {}", placeholderType, args);
				
				switch (placeholderType) {
					case "dt": {
						DateTimeFormatter formatter = DT_FORMATTER;
						
						if (args.length > 0) {
							formatter = DateTimeFormatter.ofPattern(args[0]);
						}
						
						sb.append(LocalDateTime.now().format(formatter));
					}
					break;
					case "uuid":
						sb.append(UUID.randomUUID());
						break;
					case "rand": {
						int length = 3;
						
						if (args.length > 0) {
							length = Integer.parseInt(args[0]);
						}
						if (length < 1 || length > 50) {
							throw new IllegalArgumentException("Length of random characters must be between 1 and 50 (inclusive).");
						}
						
						sb.append(RandomStringUtils.secureStrong().nextAlphanumeric(length));
					}
					break;
					case "inc": {
						if (hadInc.get()) {
							throw new IllegalArgumentException("Cannot have more than one increment placeholder.");
						}
						hadInc.set(true);
						
						int padding = 5;
						int base = 10;
						
						if (args.length > 0) {
							padding = Integer.parseInt(args[0]);
						}
						if (args.length > 1) {
							base = Integer.parseInt(args[1]);
						}
						
						if (padding < 1 || padding > 50) {
							throw new IllegalArgumentException("Length of padding characters must be between 1 and 50 (inclusive).");
						}
						if (base < 2 || base > 36) {
							throw new IllegalArgumentException("Base must be between 2 and 36 (inclusive).");
						}
						
						BigInteger lastNum = new BigInteger("0", base);
						if (generator.getLastIncremented() != null) {
							lastNum = generator.getLastIncremented();
						}
						BigInteger newNum = lastNum.add(BigInteger.ONE);
						sb.append(
							String.format(
									"%" + padding + "s",
									newNum.toString(base)
								).replace(' ', '0')
								.toUpperCase()
						);
						generator.setLastIncremented(newNum);
					}
					break;
					default:
						throw new IllegalArgumentException("Unknown placeholder type: " + placeholderType);
				}
			});
		
		if (numPlaceholders.intValue() == 0) {
			throw new IllegalArgumentException("No placeholders found in format.");
		}
		
		sb.append(format, lastEnd.get(), format.length());
		
		String newIdentifier = sb.toString();
		
		if(generator.isEncoded()){
			newIdentifier = Base64.getEncoder().withoutPadding().encodeToString(newIdentifier.getBytes());
		}
		
		return newIdentifier;
	}
	
	
	public UniqueIdentifierGenerationService() {
		super(UniqueIdentifierGenerator.class);
	}
	
	@Override
	public Set<String> getDisallowedUpdateFields() {
		Set<String> output = new HashSet<>(super.getDisallowedUpdateFields());
		output.add("idFormat");
		output.add("lastIncremented");
		output.add("encoded");
		return output;
	}
	
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return null;//TODO
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return UniqueIdentifierGenerator.CUR_SCHEMA_VERSION;
	}
	
	public String getNextUniqueId(String oqmDbNameOrId, ObjectId generatorId) {
		UniqueIdentifierGenerator gen = this.get(oqmDbNameOrId, generatorId);
		String output = null;
		
		log.debug("Getting next id from generator: {}", gen.getId());
		
		do {
			BigInteger origLastIncremented = gen.getLastIncremented();
			output = getNextUniqueId(gen);
			
			//no need to save if no increment
			if(!gen.hasIncrement()){
				continue;
			}
			
			UniqueIdentifierGenerator oldGen = this.getTypedCollection(oqmDbNameOrId)
				.findOneAndReplace(
					Filters.and(
						eq("_id", gen.getId()),
						eq("lastIncremented", origLastIncremented)
					),
					gen
				);
			
			if(oldGen == null){
				log.debug("Got back a likely duplicate id. Retrying.");
				gen = this.get(oqmDbNameOrId, generatorId);
				output = null;
			}
		} while (output == null);
		
		log.info("Generated new id from generator ({}): {}", gen.getId(), output);
		
		return output;
	}
}
