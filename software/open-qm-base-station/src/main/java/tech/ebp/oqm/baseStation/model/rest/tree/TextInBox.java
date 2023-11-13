package tech.ebp.oqm.baseStation.model.rest.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TextInBox {
	
	public final String text;
	public final int height;
	public final int width;
}
