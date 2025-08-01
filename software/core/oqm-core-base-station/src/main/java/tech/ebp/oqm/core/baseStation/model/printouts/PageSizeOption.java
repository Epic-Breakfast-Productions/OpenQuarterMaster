package tech.ebp.oqm.core.baseStation.model.printouts;

import com.itextpdf.kernel.geom.PageSize;

public enum PageSizeOption {
	DEFAULT(PageSize.DEFAULT),
	
	LETTER(PageSize.LETTER),
	LEGAL(PageSize.LEGAL),
	TABLOID(PageSize.TABLOID),
	EXECUTIVE(PageSize.EXECUTIVE),
	
	A0(PageSize.A0),
	A1(PageSize.A1),
	A2(PageSize.A2),
	A3(PageSize.A3),
	A4(PageSize.A4),
	A5(PageSize.A5),
	A6(PageSize.A6),
	A7(PageSize.A7),
	A8(PageSize.A8),
	A9(PageSize.A9),
	A10(PageSize.A10),
	
	B0(PageSize.B0),
	B1(PageSize.B1),
	B2(PageSize.B2),
	B3(PageSize.B3),
	B4(PageSize.B4),
	B5(PageSize.B5),
	B6(PageSize.B6),
	B7(PageSize.B7),
	B8(PageSize.B8),
	B9(PageSize.B9),
	B10(PageSize.B10),
	
	;
	
	
	public final PageSize size;
	
	PageSizeOption(PageSize size) {
		this.size = size;
	}
}
