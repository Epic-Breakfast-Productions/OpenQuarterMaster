package com.ebp.openQuarterMaster.plugin.moduleInteraction.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "command"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GetModInfoCommand.class, name = "GET_MODULE_INFO"),
})
public abstract class MssCommand {
	
	public abstract CommandType getCommand();
}
