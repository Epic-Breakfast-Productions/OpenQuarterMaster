export const ItemCategoryInput = {
	getValueFromInput(itemCatInputJq){
		let inputIsMultiple = itemCatInputJq.prop("multiple");
		let val = itemCatInputJq.val();

		if(val.constructor === Array){
			val = val.filter(v => v !== "");
		} else {
			if(val === ""){
				val = null;
			}
		}

		if(inputIsMultiple){
			if(val === null){
				val = [];
			}
		}

		return val;
	}
}