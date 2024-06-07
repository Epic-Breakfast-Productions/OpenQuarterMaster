package tech.ebp.oqm.core.api.service.importExport.importing.csv;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StorageType;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class InvItemCsvConverter {
	public static final String[] CSV_HEADERS = {"name", "description", "storageType", "unit", "valuePerUnit", "barcode", "trackedItemId",  "storageBlock"};
	public static final Character COMMENT_CHAR = '#';
	
	public List<InventoryItem<?, ?, ?>> csvIsToItems(InputStream is) throws IOException {
		List<InventoryItem<?, ?, ?>> output = new ArrayList<>();
		Reader in = new InputStreamReader(is);
		
		Iterable<CSVRecord> records = CSVFormat.Builder
										  .create(CSVFormat.EXCEL)
										  .setCommentMarker(COMMENT_CHAR)
										  .setHeader(CSV_HEADERS)
										  .setSkipHeaderRecord(true)
										  .setTrim(true)
										  .build().parse(in);
		
		for (CSVRecord record : records) {
			StorageType storageType = StorageType.valueOf(record.get("storageType"));
			InventoryItem<?, ?, ?> item = switch (storageType) {
				case AMOUNT_SIMPLE -> new SimpleAmountItem();
				case AMOUNT_LIST -> new ListAmountItem();
				case TRACKED -> new TrackedItem();
				default -> throw new IllegalArgumentException("Invalid Storage Type Given: " + record.get("storageType"));
			};
			
			item.setName(record.get("name"));
			item.setDescription(record.get("description"));
			
			if (!record.get("barcode").isBlank()) {
				item.setBarcode(record.get("barcode"));
			}
			if (!record.get("storageBlock").isBlank()) {
				item.getStoredWrapperForStorage(new ObjectId(record.get("storageBlock")), true);
			}
			
			String unitVal = record.get("storageBlock");
			switch (storageType) {
				case AMOUNT_SIMPLE:
					SimpleAmountItem sai = (SimpleAmountItem) item;
					if (!unitVal.isBlank()) {
						sai.setUnit(UnitUtils.unitFromString(unitVal));
					}
					if (!record.get("valuePerUnit").isBlank()) {
						sai.setValuePerUnit(new BigDecimal(record.get("valuePerUnit")));
					}
					break;
				case AMOUNT_LIST:
					ListAmountItem lai = ((ListAmountItem) item);
					if (!unitVal.isBlank()) {
						lai.setUnit(UnitUtils.unitFromString(unitVal));
					}
					if (!record.get("valuePerUnit").isBlank()) {
						lai.setValuePerUnit(new BigDecimal(record.get("valuePerUnit")));
					}
					break;
				case TRACKED:
					TrackedItem tri = ((TrackedItem) item);
					
					tri.setTrackedItemIdentifierName(record.get("trackedItemId"));
					
					if (!record.get("valuePerUnit").isBlank()) {
						tri.setDefaultValue(new BigDecimal(record.get("valuePerUnit")));
					}
					break;
			}
			output.add(item);
		}
		return output;
	}
}
