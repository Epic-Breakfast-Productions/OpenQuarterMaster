package tech.ebp.oqm.lib.core.test;

import tech.ebp.oqm.lib.core.object.MainObject;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = TestOne.class, name = "ONE"),
	@JsonSubTypes.Type(value = TestTwo.class, name = "TWO")
})
@BsonDiscriminator
@Deprecated
public class TestSuper<T> extends MainObject {
	
	private final ClassType type;
	
	private T value;
	
	//for Lombok, never to be used
	private TestSuper() {
		this.type = null;
	}
}
