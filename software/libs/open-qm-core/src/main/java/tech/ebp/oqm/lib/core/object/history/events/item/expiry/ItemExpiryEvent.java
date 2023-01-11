package tech.ebp.oqm.lib.core.object.history.events.item.expiry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemStorageBlockEvent;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public abstract class ItemExpiryEvent extends ItemStorageBlockEvent {

}
