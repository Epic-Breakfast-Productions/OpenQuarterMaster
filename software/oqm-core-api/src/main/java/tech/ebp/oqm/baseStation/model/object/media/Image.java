package tech.ebp.oqm.baseStation.model.object.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

/**
 * TODO:: refactor?
 */
@Data
//@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(builderClassName = "Builder")
public class Image extends FileMainObject {


}
