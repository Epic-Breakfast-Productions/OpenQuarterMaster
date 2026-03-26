package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString(callSuper = true)
@Getter
@SuperBuilder(toBuilder = true)
public class FileAttachmentSearch extends FileSearchObject {
	//TODO:: add to bson filter list
}
