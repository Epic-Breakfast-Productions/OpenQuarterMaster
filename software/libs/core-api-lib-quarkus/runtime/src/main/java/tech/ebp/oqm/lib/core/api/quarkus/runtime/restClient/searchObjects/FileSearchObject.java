package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString(callSuper = true)
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class FileSearchObject extends SearchObject {

}
