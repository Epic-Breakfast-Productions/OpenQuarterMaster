package tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
public abstract class ReturnableLocale extends InTransitLocale {

}
