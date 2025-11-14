package tech.ebp.oqm.core.baseStation.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.core.baseStation.model.CodeImageType;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.Code2Of5;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.Upc;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service to generate barcode images.
 *
 * TODO:: move to separate service
 * TODO:: add better labels to images https://github.com/jfree/jfreesvg
 */
@ApplicationScoped
public class BarcodeService {
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
	
	@WithSpan
	public String getBarcodeData(String data){
		Code128 barcode = new Code128();
		barcode.setFontName("Monospaced");
		barcode.setFontSize(16);
		barcode.setModuleWidth(2);
		barcode.setBarHeight(50);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(data);
		
		return toImageData(barcode);
	}
	
	@WithSpan
	public String getQrCodeData(String data){
		QrCode qrCode = new QrCode();
		qrCode.setContent(data);
		
		return toImageData(qrCode);
	}
	
	@WithSpan
	public String getCodeData(CodeImageType type, String data){
		switch (type){
			case qrcode:
				return this.getQrCodeData(data);
			case barcode:
				return this.getBarcodeData(data);
		}
		throw new IllegalStateException();
	}
	
}
