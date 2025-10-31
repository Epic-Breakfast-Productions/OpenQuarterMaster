package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc.UPC_E;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = EAN_8.class, name = "EAN_8"),
	@JsonSubTypes.Type(value = EAN_13.class, name = "EAN_13"),
	@JsonSubTypes.Type(value = GTIN_14.class, name = "GTIN_14"),
	@JsonSubTypes.Type(value = ISBN_10.class, name = "ISBN_10"),
	@JsonSubTypes.Type(value = ISBN_13.class, name = "ISBN_13"),
	@JsonSubTypes.Type(value = UPC_A.class, name = "UPC_A"),
	@JsonSubTypes.Type(value = UPC_E.class, name = "UPC_E"),
	@JsonSubTypes.Type(value = Generic.class, name = "GENERIC"),
	@JsonSubTypes.Type(value = GeneralGeneratedId.class, name = "GENERATED"),
	@JsonSubTypes.Type(value = ToGenerateGeneralId.class, name = "TO_GENERATE"),
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
@AllArgsConstructor
@NoArgsConstructor
public abstract class GeneralId extends Identifier {
	
	/**
	 * TODO:: maybe not have this here?
	 */
	@lombok.Builder.Default
	private boolean useInLabel = false;
	
	/**
	 * Abstract to allow attachment of appropriate validation annotation
	 * TODO:: move to this class level annotation, switch case based on type?
	 * @return
	 */
	public abstract GeneralIdType getType();
	
	public abstract String getValue();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isBarcode() {
		return this.getType().isBarcode;
	}
}
