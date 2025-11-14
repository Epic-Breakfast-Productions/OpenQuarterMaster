package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation;

import org.bson.types.ObjectId;

public interface ToGenerate {
	
	ObjectId getGenerateFrom();
	
	Generates generates();
}
