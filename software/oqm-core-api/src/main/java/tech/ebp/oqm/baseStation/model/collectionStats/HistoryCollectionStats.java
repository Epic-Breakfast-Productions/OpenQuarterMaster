package tech.ebp.oqm.baseStation.model.collectionStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

/**
 * TODO:: contemplate what stats we actually want
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class HistoryCollectionStats extends CollectionStats {
	private ZonedDateTime firstAdditionDt;
	private ZonedDateTime latestAdditionDt;
}
