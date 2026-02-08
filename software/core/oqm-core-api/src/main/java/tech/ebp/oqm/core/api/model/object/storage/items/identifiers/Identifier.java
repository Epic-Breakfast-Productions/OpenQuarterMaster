package tech.ebp.oqm.core.api.model.object.storage.items.identifiers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.jetbrains.annotations.NotNull;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean.EAN_8;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated.GeneratedIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated.ToGenerateIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.gtin.GTIN_14;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_10;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn.ISBN_13;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_A;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc.UPC_E;

@Data
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
	@JsonSubTypes.Type(value = GenericIdentifier.class, name = "GENERIC"),
	@JsonSubTypes.Type(value = GeneratedIdentifier.class, name = "GENERATED"),
	@JsonSubTypes.Type(value = ToGenerateIdentifier.class, name = "TO_GENERATE"),
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
@AllArgsConstructor
@NoArgsConstructor
public abstract class Identifier implements Labeled, Comparable<Identifier> {
	
	@lombok.Builder.Default
	private String label = null;
	
	//TODO:: contemplate
	//	@lombok.Builder.Default
	//	private boolean unique = true;
	
	public abstract IdentifierType getType();
	
	public abstract String getValue();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isBarcode() {
		return this.getType().isBarcode;
	}
	
	public String getLabel() {
		if (this.label == null) {
			return this.getType().prettyName();
		}
		return this.label;
	}
	
	@Override
	public int compareTo(@NotNull Identifier identifier) {
		{
			int typeComp = this.getType().compareTo(identifier.getType());
			if (typeComp != 0) {
				return typeComp;
			}
		}
		{
			int labelComp = this.getLabel().compareTo(identifier.getLabel());
			if (labelComp != 0) {
				return labelComp;
			}
		}
		{
			int valueComp = this.getValue().compareTo(identifier.getValue());
			if (valueComp != 0) {
				return valueComp;
			}
		}
		
		return 0;
	}
}
