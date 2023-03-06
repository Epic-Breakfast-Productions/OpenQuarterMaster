package tech.ebp.oqm.lib.core.object.itemList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class ItemList extends AttKeywordMainObject {
	
	@NonNull
	@NotNull
	@NotBlank
	private String name;
	
	@NonNull
	@NotNull
	private String description = "";
	
	@NonNull
	@NotNull
	private List<@NonNull ItemListItem> items = new ArrayList<>();
	
	private boolean applied = false;
}
