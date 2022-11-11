package tech.ebp.oqm.lib.core.object.externalService.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.lib.core.object.externalService.plugin.components.nav.NavItem;
import tech.ebp.oqm.lib.core.object.externalService.plugin.components.nav.NavSubMenu;

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
	
	@lombok.Builder.Default
	public boolean enabled = true;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract PluginType getPluginType();
}
