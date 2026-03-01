
export const DselectUtils = {
	standardOps: {search: true, clearable: true},
	setupDselect(selectJs) {
		dselect(selectJs, DselectUtils.standardOps);
	},
	setupPageDselects() {
		document.querySelectorAll(".dselect-select").forEach(function (cur) {
			try {
				DselectUtils.setupDselect(cur);
			} catch (error) {
				console.error(error);
			}
		})
	},
	setValues(selectJq, value){
		selectJq.val(value);
		DselectUtils.setupDselect(selectJq[0]);
	},
	resetDselect(selectJq) {
		if (selectJq.prop("multiple")) {
			selectJq.val('');
		} else {
			selectJq.val(selectJq.find("option:first").val());
		}

		DselectUtils.setupDselect(selectJq[0]);
	},
	initPage(){
		console.log("Setting up dselects");
		DselectUtils.setupPageDselects();
		Main.processStop();
		console.log("Finished setting up dselects");
	}
};
