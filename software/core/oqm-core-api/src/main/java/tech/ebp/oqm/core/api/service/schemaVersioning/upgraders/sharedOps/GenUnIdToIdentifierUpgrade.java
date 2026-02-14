package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.sharedOps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

public class GenUnIdToIdentifierUpgrade {
	
	private static boolean hasIdWithLabel(ArrayNode identifiers, String label){
		return identifiers.valueStream().anyMatch(id -> id.get("label").asText().equals(label));
	}
	
	private static void normalizeId(ObjectNode identifier){
		identifier.remove("_t");
		identifier.remove("useInLabel");
		UpgradingUtils.normalizeObjectId(identifier, "generatedFrom");
		
	}
	
	public static void upgradeIds(ObjectNode oldObj){
		ArrayNode generalIds = (ArrayNode) oldObj.remove("generalIds");
		ArrayNode unIds = (ArrayNode) oldObj.remove("uniqueIds");
		
		ArrayNode identifiers = oldObj.putArray("identifiers");
		
		//add general Ids
		for(JsonNode genId : generalIds){
			normalizeId((ObjectNode) genId);
			identifiers.add(genId);
		}
		
		//add unique Ids
		for(JsonNode unIdNode : unIds){
			ObjectNode unId = (ObjectNode) unIdNode;
			normalizeId(unId);
			
			//check for label collision
			TextNode label = (TextNode) unId.get("label");
			
			if(hasIdWithLabel(identifiers, label.asText())){
				//append num to label to make unique
				String origLabel = unId.get("label").asText();
				int num = 0;
				
				do{
					label = new TextNode(origLabel + '-' + ++num);
				}while(hasIdWithLabel(identifiers, label.asText()));
				
				unId.set("label", label);
			}
			
			//tweak type
			switch(unId.get("type").asText()){
				case "PROVIDED":
					unId.put("type", "GENERIC");
					break;
			}
			
			identifiers.add(unId);
		}
	}
	public static void upgradeStoredLabelFormat(ObjectNode oldObj, String field){
		if(oldObj.get(field) == null || !oldObj.get(field).isTextual()){
			return;
		}
		
		String oldLabelFormat = oldObj.get(field).asText();
		
		String newLabelFormat = oldLabelFormat.replace("{uid:", "{ident:");
		newLabelFormat = newLabelFormat.replace("{gid:", "{ident:");
		
		
		oldObj.put(field, newLabelFormat);
	}
}
