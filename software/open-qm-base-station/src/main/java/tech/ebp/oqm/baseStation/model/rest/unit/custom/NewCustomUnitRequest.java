package tech.ebp.oqm.baseStation.model.rest.unit.custom;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewBaseCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewDerivedCustomUnitRequest;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitCategory;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "requestType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NewBaseCustomUnitRequest.class, name = "BASE"),
	@JsonSubTypes.Type(value = NewDerivedCustomUnitRequest.class, name = "DERIVED")
})
@BsonDiscriminator
public abstract class NewCustomUnitRequest {
	
	@NonNull
	@NotNull
	private UnitCategory unitCategory;
	
	@NonNull
	@NotNull
	private String name;
	
	@NonNull
	@NotNull
	private String symbol;
	
	public abstract RequestType getRequestType();
	
	public abstract Unit<?> toUnit();
	
	public CustomUnitEntry toCustomUnitEntry(long orderVal) {
		return new CustomUnitEntry(
			this.getUnitCategory(),
			orderVal,
			this
		);
	}
	
	public enum RequestType {
		BASE, DERIVED
	}
}
