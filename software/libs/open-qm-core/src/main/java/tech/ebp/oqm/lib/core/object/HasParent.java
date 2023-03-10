package tech.ebp.oqm.lib.core.object;

import org.bson.types.ObjectId;

public interface HasParent {
	ObjectId getParent();
}
