package tech.ebp.oqm.core.api.service.identifiers.unique;

import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralIdType;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdType;
import tech.ebp.oqm.core.api.service.identifiers.IdBarcodeService;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.Code2Of5;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.Upc;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service to generate barcode images.
 *
 * TODO:: move to own service?
 * TODO:: add better labels to images https://github.com/jfree/jfreesvg
 */
@ApplicationScoped
public class UniqueIdBarcodeService extends IdBarcodeService {
	
	public String getUniqueIdData(String data, String label){
		String dataIn = data;
		int hQuietZone = 10;
		
		Symbol barcode = new Code128();
		
		barcode.setFontName("Monospaced");
		barcode.setFontSize(8);
//		barcode.setModuleWidth(1);
//		barcode.setBarHeight(50);
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
//		barcode.setFontSize(16);
//		barcode.setModuleWidth(2);
//		barcode.setBarHeight(50);
		barcode.setQuietZoneHorizontal(2);
		barcode.setQuietZoneVertical(2);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(objectId.toHexString());
		
		return processBarcodeData(barcode,null);
	}
}
