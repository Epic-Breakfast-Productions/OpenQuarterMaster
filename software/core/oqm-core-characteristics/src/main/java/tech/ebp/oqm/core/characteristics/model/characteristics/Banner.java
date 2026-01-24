package tech.ebp.oqm.core.characteristics.model.characteristics;

import io.smallrye.common.constraint.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.awt.*;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Banner {
	@NonNull
	private String text;
	@NotNull
	private Color textColor;
	@NotNull
	private Color backgroundColor;
}
