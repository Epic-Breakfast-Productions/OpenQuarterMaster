const ItemCategoryInput = {
	getValueFromInput(itemCatInputJq){
		let val = itemCatInputJq.val();

		if(val.constructor === Array){
			val = val.filter(v => v !== "");
		} else {
			if(val === ""){
				val = null;
			}
		}
		return val;
	}
}