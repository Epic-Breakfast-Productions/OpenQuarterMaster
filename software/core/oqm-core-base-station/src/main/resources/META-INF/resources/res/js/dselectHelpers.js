const Dselect = {
	standardOps: {search: true, clearable: true},
	setupDselect(selectJs) {
		dselect(selectJs, Dselect.standardOps);
	},
	setupPageDselects() {
		document.querySelectorAll(".dselect-select").forEach(function (cur) {
			try {
				Dselect.setupDselect(cur);
			} catch (error) {
				console.error(error);
			}
		})
	},
	setValues(selectJq, value){
		selectJq.val(value);
		Dselect.setupDselect(selectJq[0]);
	},
	resetDselect(selectJq) {
		if (selectJq.prop("multiple")) {
			selectJq.val('');
		} else {
			selectJq.val(selectJq.find("option:first").val());
		}

		Dselect.setupDselect(selectJq[0]);
	}
};
Dselect.setupPageDselects();
Main.processStop();