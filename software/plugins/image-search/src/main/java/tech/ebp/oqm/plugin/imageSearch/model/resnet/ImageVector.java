package tech.ebp.oqm.plugin.imageSearch.model.resnet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageVector {
	
	private ObjectId id;
}
