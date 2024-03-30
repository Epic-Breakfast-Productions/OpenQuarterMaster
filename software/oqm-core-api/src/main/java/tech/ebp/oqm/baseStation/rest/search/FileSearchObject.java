package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

@ToString(callSuper = true)
@Getter
public class FileSearchObject<F extends FileMainObject> extends SearchObject<F> {
	//TODO:: add to bson filter list
}
