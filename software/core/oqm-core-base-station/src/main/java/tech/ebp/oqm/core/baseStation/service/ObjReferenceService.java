package tech.ebp.oqm.core.baseStation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefImgOptions;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefOptions;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ApplicationScoped
public class ObjReferenceService {

	public String getObjRef(@Valid ObjRefOptions options) {
		StringBuilder output = new StringBuilder(options.getIncludeOqm() ? "OQM:" : "");

		output.append(options.getObjType().shortForm);
		output.append(":");
		output.append(options.getId());

		options.getId2().ifPresentOrElse(
			(id2)->{
				if (!options.getObjType().hasSecondId) {
					throw new IllegalArgumentException("Cannot have second id for object type " + options.getObjType());
				}

				output.append(":");
				output.append(id2);
			},
			()->{
				if(options.getObjType().hasSecondId){
					throw new IllegalArgumentException("Must specify second id for object type " + options.getObjType());
				}
			}
		);

		return output.toString();
	}


	public String getObjRefImage(
		@Valid ObjRefImgOptions imgOps
	) {
		String objRef = this.getObjRef(imgOps.getObjRefOptions());

		Symbol code = null;

		switch (imgOps.getImageType()){
			case qrcode -> {
				QrCode qrCode = new QrCode();

				//tweaks?
				if(imgOps.getLabel().orElse(false)){
					//TODO:: add label
				}

				code = qrCode;
			}
			case barcode -> {
				Code128 barcode = new Code128();
				barcode.setFontName("Monospaced");
				barcode.setFontSize(16);
				barcode.setModuleWidth(2);
				barcode.setBarHeight(50);
				barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);

				if(imgOps.getLabel().orElse(true)){
					//TODO:: add label
				}

				code = barcode;
			}
		}

		code.setContent(objRef);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SvgRenderer renderer = new SvgRenderer(os, 1, Color.WHITE, Color.BLACK, true);
		try {
			renderer.render(code);
		} catch(IOException e) {
			throw new IllegalStateException(new RuntimeException(e));
		}
		return os.toString();
	}


}
