package tech.ebp.oqm.core.baseStation.model.objReference;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.core.baseStation.model.CodeImageType;

import java.util.Optional;

@ToString
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjRefImgOptions {

	@NotNull
	@QueryParam("objType")
	ObjRefObjType objType;

	@NotNull
	@QueryParam("id")
	String id;

	@QueryParam("id2")
	Optional<String> id2;

	@QueryParam("includeOqm")
	Optional<Boolean> includeOqm;

	@NotNull
	@QueryParam("imageType")
	@DefaultValue("barcode")
	@Builder.Default
	CodeImageType imageType = CodeImageType.barcode;

	@QueryParam("label")
	Optional<Boolean> label;

	public ObjRefOptions getObjRefOptions(){
		return ObjRefOptions.builder()
				   .objType(this.getObjType())
				   .id(this.getId())
				   .id2(this.getId2())
				   .includeOqm(
					   this.getIncludeOqm().orElseGet(()->switch(this.getImageType()){
						   case qrcode -> Boolean.TRUE;
						   case barcode -> Boolean.FALSE;
					   })
				   )
				   .build();
	}
}
