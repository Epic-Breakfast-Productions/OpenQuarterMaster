package tech.ebp.oqm.core.baseStation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefImgOptions;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefObjType;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjRefOptions;
import tech.ebp.oqm.core.baseStation.model.objReference.ObjectReference;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

@Named("ObjReferenceService")
@Slf4j
@ApplicationScoped
public class ObjReferenceService {

	private static final Pattern REF_PATTERN = Pattern.compile("^(oqm:|OQM:)?[a-zA-Z]+:[a-zA-Z0-9]+(:[a-zA-Z0-9]+)?$");

	private String validateId(String id){
		if(id == null || id.isBlank()){
			throw new IllegalArgumentException("ID given null or blank.");
		}
		return id;
	}

	public String getRefValidationPattern(){
		return REF_PATTERN.pattern();
	}

	public ObjectReference parseObjReference(@NonNull String refString){
		if(!REF_PATTERN.matcher(refString).matches()){
			throw new IllegalArgumentException("Reference string does not match expected format.");
		}

		refString = refString.replaceFirst("(?i)OQM:", "");

		String[] parts = refString.split(":");

		if(parts.length != 2 && parts.length != 3){
			throw new IllegalArgumentException("Malformed Reference String.");
		}

		ObjectReference.ObjectReferenceBuilder builder = ObjectReference.builder();

		ObjRefObjType type = ObjRefObjType.toRef(parts[0]);
		builder.type(type);

		this.validateId(parts[1]);
		builder.id(parts[1]);

		if(type.hasSecondId){
			if(parts.length != 3){
				throw new IllegalArgumentException("Must supply second id for object of type " + type);
			}

			this.validateId(parts[2]);
			builder.id2(parts[2]);
		}

		return builder.build();
	}

	public String getObjRef(@Valid ObjRefOptions options) {
		StringBuilder output = new StringBuilder(options.getIncludeOqm() ? "OQM:" : "");

		output.append(options.getObjType().shortForm);
		output.append(":");
		output.append(validateId(options.getId()));

		options.getId2().ifPresentOrElse(
			(id2)->{
				if (!options.getObjType().hasSecondId) {
					throw new IllegalArgumentException("Cannot have second id for object type " + options.getObjType());
				}

				output.append(":");
				output.append(validateId(id2));
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
