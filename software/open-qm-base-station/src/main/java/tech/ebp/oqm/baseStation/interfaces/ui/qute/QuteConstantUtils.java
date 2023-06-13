package tech.ebp.oqm.baseStation.interfaces.ui.qute;

import io.quarkus.qute.TemplateData;
import tech.ebp.oqm.baseStation.service.importExport.csv.InvItemCsvConverter;

@TemplateData
public class QuteConstantUtils {
	public static final Character CSV_COMMENT = InvItemCsvConverter.COMMENT_CHAR;
	public static final String INV_ITEM_CSV_HEADERS = String.join(", ", InvItemCsvConverter.CSV_HEADERS);
}
