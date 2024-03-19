const UnitUtils = {
	unitOptions: $("#unitSelectOptions").children(),
	/**
	 * Example input: {"unit":{"string":"units","name":"Units","symbol":"units"},"scale":"ABSOLUTE","value":8}
	 * @param quantityObj
	 */
	quantityToDisplayStr(quantityObj) {
		return quantityObj.value + quantityObj.unit.symbol;
	},
	getUnitOptions(selectedVal) {
		let output = UnitUtils.unitOptions.clone();

//    if(selectedVal){
//        output.each(function(i, curOptGrp){
//            $(curOptGrp).children().each(function(j, curOption){
//                if(curOption.value == selectedVal){
//                    curOption.attributes["selected"] = true;
//                }
//            });
//        });
//    }
		return output;
	},
	//TODO:: review usage of ItemAddEdit.compatibleUnitOptions, and where it/this function should live
	updateCompatibleUnits(unitToCompatWith, containerToSearch) {
		Rest.call({
			url: Rest.passRoot + "/inventory/unit/compatibility/" + unitToCompatWith,
			extraHeaders: {accept: "text/html"},
			dataType: "html",
			returnType: "html",
			async: false,
			done: function (data) {
				ItemAddEdit.compatibleUnitOptions = data;

				containerToSearch.find(".unitInput").each(function (i, selectInput) {
					var selectInputJq = $(selectInput);
					selectInputJq.html(ItemAddEdit.compatibleUnitOptions);
					selectInputJq.change();
				});
			}
		});
	},
	getUnitObj(unitStr) {
		let output = {
			string: unitStr
		};

		return output;
	},
	getQuantityObj(value, unit) {
		let output = {
			unit: UnitUtils.getUnitObj(unit),
			scale: "ABSOLUTE",
			value: value
		};

		return output;
	}
};
