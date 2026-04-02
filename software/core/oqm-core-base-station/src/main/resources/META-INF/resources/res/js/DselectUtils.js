//import "../../lib/dselect/1.0.4/dist/js/dselect.min.js";//doesn't work. Unsure why

import {PageUtility} from "./utilClasses/PageUtility.js";

export class DselectUtils extends PageUtility {
	static standardOps = {search: true, clearable: true};
	static setupDselect(selectJs) {
		dselect(selectJs, DselectUtils.standardOps);
	}
	static setupPageDselects() {
		document.querySelectorAll(".dselect-select").forEach(function (cur) {
			try {
				DselectUtils.setupDselect(cur);
			} catch (error) {
				console.error(error);
			}
		})
	}
	static setValues(selectJq, value){
		selectJq.val(value);
		DselectUtils.setupDselect(selectJq[0]);
	}
	static resetDselect(selectJq) {
		if (selectJq.prop("multiple")) {
			selectJq.val('');
		} else {
			selectJq.val(selectJq.find("option:first").val());
		}

		DselectUtils.setupDselect(selectJq[0]);
	}
	static {
		window.DselectUtils = this;

		console.log("Setting up dselects");
		DselectUtils.setupPageDselects();
		Main.processStop();
		console.log("Finished setting up dselects");
	}
}
