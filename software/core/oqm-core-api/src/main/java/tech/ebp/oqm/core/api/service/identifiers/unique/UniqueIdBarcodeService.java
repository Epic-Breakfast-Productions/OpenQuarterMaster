package tech.ebp.oqm.core.api.service.identifiers.unique;

import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdType;
import tech.ebp.oqm.core.api.service.identifiers.IdBarcodeService;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.Symbol;

/**
 * Service to generate barcode images for unique ids and objects.
 */
@ApplicationScoped
public class UniqueIdBarcodeService extends IdBarcodeService {
	
	public String getUniqueIdData(String data, String label){
		String dataIn = data;
		int hQuietZone = 10;
		
		Symbol barcode = new Code128();
		
		barcode.setFontName("Monospaced");
		barcode.setFontSize(8);
		barcode.setQuietZoneHorizontal(hQuietZone);
		barcode.setQuietZoneVertical(2);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(dataIn);
		
		return processBarcodeData(barcode, label);
	}
	
	public String getUniqueIdData(UniqueId identifier){
		return this.getUniqueIdData(identifier.getValue(), identifier.getLabel());
	}
	
	public String getObjectIdData(ObjectId objectId){//TODO:: rework for full object (type, label)
		Code128 barcode = new Code128();
		barcode.setFontName("Monospaced");
		barcode.setFontSize(8);
//		barcode.setBarHeight(50);
		barcode.setQuietZoneHorizontal(2);
		barcode.setQuietZoneVertical(2);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(objectId.toHexString());
		
		return processBarcodeData(barcode,null);
	}
}
