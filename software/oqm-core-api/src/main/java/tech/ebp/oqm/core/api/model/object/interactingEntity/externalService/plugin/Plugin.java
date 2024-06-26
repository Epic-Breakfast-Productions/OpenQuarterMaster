package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.components.nav.NavItem;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.components.nav.NavSubMenu;

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
	@lombok.Builder.Default
	public String description = "";
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract PluginType getPluginType();
}
