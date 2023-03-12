package tech.ebp.oqm.baseStation.interfaces.ui.qute;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.qute.TemplateData;
import tech.ebp.oqm.lib.core.jackson.ColorModule;
import tech.ebp.oqm.lib.core.object.ObjectUtils;

import java.awt.*;

@TemplateData
public class QuteUtils {
	public static String colorToHex(Color color) throws JsonProcessingException {
		return ColorModule.toHexString(color);
	}
	
}
