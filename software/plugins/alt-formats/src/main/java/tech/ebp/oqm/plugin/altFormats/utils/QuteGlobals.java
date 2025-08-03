package tech.ebp.oqm.plugin.altFormats.utils;

import io.quarkus.qute.TemplateGlobal;
import tech.ebp.oqm.plugin.altFormats.service.csv.InvItemCsvConverter;

@TemplateGlobal
public class QuteGlobals {
	public static final Character CSV_COMMENT = InvItemCsvConverter.COMMENT_CHAR;
	public static final String INV_ITEM_CSV_HEADERS = String.join(", ", InvItemCsvConverter.CSV_HEADERS);
}
