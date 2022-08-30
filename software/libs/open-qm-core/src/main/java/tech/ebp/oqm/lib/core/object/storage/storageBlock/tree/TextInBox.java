package tech.ebp.oqm.lib.core.object.storage.storageBlock.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TextInBox {
	
	public final String text;
	public final int height;
	public final int width;
}
