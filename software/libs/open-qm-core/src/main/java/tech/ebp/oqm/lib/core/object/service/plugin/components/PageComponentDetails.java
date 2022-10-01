package tech.ebp.oqm.lib.core.object.service.plugin.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "componentType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NavItemDetails.class, name = "NAV_ITEM"),
	@JsonSubTypes.Type(value = NavSubMenuDetails.class, name = "NAV_SUB_MENU"),
})
@BsonDiscriminator(key = "componentType_mongo")
@SuperBuilder
public abstract class PageComponentDetails {
	
	public abstract PageComponentType getComponentType();
}
