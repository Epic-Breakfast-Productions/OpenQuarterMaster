package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.components.nav.NavItem;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.components.nav.NavSubMenu;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "pluginType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NavItem.class, name = "NAV_ITEM"),
	@JsonSubTypes.Type(value = NavSubMenu.class, name = "NAV_SUB_MENU"),
})
@BsonDiscriminator()
@SuperBuilder
public abstract class Plugin {
	
	//	@lombok.Builder.Default
	//	public boolean enabled = true;
	
	@NonNull
	@NotNull
	@NotBlank
	public String name;
	
	@NonNull
	@NotNull
	@Builder.Default
	public String description = "";
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract PluginType getPluginType();
}
