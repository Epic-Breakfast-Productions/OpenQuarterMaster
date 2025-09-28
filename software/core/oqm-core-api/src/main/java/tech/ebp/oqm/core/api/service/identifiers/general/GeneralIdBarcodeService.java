package tech.ebp.oqm.core.api.service.identifiers.general;

import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralIdType;
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
public class GeneralIdBarcodeService {
	public static final String DATA_MEDIA_TYPE = "image/svg+xml";
	
	private static String toImageData(Symbol code){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SvgRenderer renderer = new SvgRenderer(os, 1, Color.WHITE, Color.BLACK, true);
		try {
			renderer.render(code);
		} catch(IOException e) {
			throw new IllegalStateException(new RuntimeException(e));
		}
		return os.toString();
	}
	
	
	public String getGeneralIdData(GeneralIdType type, String data){
		String dataIn = data;
		int hQuietZone = 10;
		
		Symbol barcode = switch (type){
			case UPC_A -> {
				Upc upcaCode = new Upc(Upc.Mode.UPCA);
				dataIn = data.substring(0, 11);//shave off check bit
				upcaCode.setShowCheckDigit(true);
				yield upcaCode;
			}
			case UPC_E -> {
				Upc upceCode = new Upc(Upc.Mode.UPCE);
				dataIn = data.substring(0, 7);//shave off check bit
				yield upceCode;
			}
			case ISBN_10 -> {
				Ean isbn10Code = new Ean(Ean.Mode.EAN13);//TODO:: definately not this
				yield isbn10Code;
			}
			case ISBN_13 -> {
				Ean isbn13Code = new Ean(Ean.Mode.EAN13);//TODO:: likely not this
				dataIn = data.substring(0, 12);//shave off check bit
				yield isbn13Code;
			}
			case EAN_8 -> {
				Ean ean8Code = new Ean(Ean.Mode.EAN8);
				dataIn = data.substring(0, 7);//shave off check bit
				yield ean8Code;
			}
			case EAN_13 -> {
				Ean ean13Code = new Ean(Ean.Mode.EAN13);
				dataIn = data.substring(0, 12);//shave off check bit
				yield ean13Code;
			}
			case GTIN_14 -> {
				Code2Of5 gtin14Code = new Code2Of5(Code2Of5.ToFMode.ITF14);
				dataIn = data.substring(0, 13);//shave off check bit
				yield gtin14Code;
			}
			case GENERIC -> {
				Code128 genericCode = new Code128();
				yield genericCode;
			}
			
			default -> throw new IllegalStateException("Unexpected value: " + type);
		};
		
		barcode.setFontName("Monospaced");
		barcode.setFontSize(8);
//		barcode.setModuleWidth(1);
//		barcode.setBarHeight(50);
		barcode.setQuietZoneHorizontal(hQuietZone);
		barcode.setQuietZoneVertical(2);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(dataIn);
		
		return toImageData(barcode);
	}
	
	public String getGeneralIdData(GeneralId identifier){
		return this.getGeneralIdData(identifier.getType(), identifier.getValue());
	}
	
}
