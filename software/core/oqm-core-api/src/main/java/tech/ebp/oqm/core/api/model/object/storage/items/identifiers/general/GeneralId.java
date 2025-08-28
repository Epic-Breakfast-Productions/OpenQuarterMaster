package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
public abstract class GeneralId extends Identifier {
	
	public abstract GeneralIdType getType();
	
	public abstract String getValue();
	
	public boolean isBarcode() {
		return this.getType().isBarcode;
	}
}
