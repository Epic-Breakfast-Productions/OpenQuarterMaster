package tech.ebp.oqm.core.api.model.object.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.Versionable;

public abstract class ObjectVersionBumper<T extends Versionable> implements Comparable<ObjectVersionBumper<T>> {
	
	protected ObjectVersionBumper(int bumperTo) {
		this.bumperTo = bumperTo;
	}
	
	@Getter
	public final int bumperTo;
	
	public abstract JsonNode bumpObject(JsonNode node);
	
	@Override
	public int compareTo(ObjectVersionBumper<T> tObjectVersionBumper) {
		return Integer.compare(
			this.getBumperTo(),
			tObjectVersionBumper.getBumperTo()
		);
	}
}
