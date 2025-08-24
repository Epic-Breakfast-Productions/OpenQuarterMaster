package tech.ebp.oqm.core.baseStation.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;

@Slf4j
@Named("ItemStoredResultsQuteFunctions")
@ApplicationScoped
public class ItemStoredResultsQuteFunctions {

	public boolean showAmount(
		ObjectNode inventoryItem
	){
		log.debug("showAmount: {}", inventoryItem);
		
		return inventoryItem == null ||
			   inventoryItem.get("storageType").asText().equals("BULK") ||
			   inventoryItem.get("storageType").asText().equals("AMOUNT_LIST");
		
		//inventoryItem ? (inventoryItem.get("storageType").asText().equals("BULK") || inventoryItem.get("storageType").asText().equals("AMOUNT_LIST")) : true
//		return false;
	}
	
	public boolean storedIsAmount(
		ObjectNode storedItem
	){
		return storedItem.get("type").asText().equals("AMOUNT");
	}
}
