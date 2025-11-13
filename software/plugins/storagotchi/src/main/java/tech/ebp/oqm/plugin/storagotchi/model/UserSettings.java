package tech.ebp.oqm.plugin.storagotchi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;


/**
 * Example JPA entity defined as a Panache Entity. An ID field of Long type is provided, if you want to define your own ID field extends <code>PanacheEntityBase</code> instead.
 * <p>
 * This uses the active record pattern, you can also use the repository pattern instead: .
 * <p>
 * Usage (more example on the documentation)
 * <p>
 * {@code public void doSomething() { MyEntity entity1 = new MyEntity(); entity1.field = "field-1"; entity1.persist();
 * <p>
 * List<MyEntity> entities = MyEntity.listAll(); } }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class UserSettings {
	
	@Id
	private String id;
	
	@NotNull
	@NonNull
	@NotBlank
	@Builder.Default
	private String storagotchiName = "Storagotchi";
	
	@Builder.Default
	private boolean music = true;
	
	@Builder.Default
	private boolean soundfx = true;
	
	@Min(0)
	@Max(1)
	@Builder.Default
	private double volume = 0.75;
	
}
