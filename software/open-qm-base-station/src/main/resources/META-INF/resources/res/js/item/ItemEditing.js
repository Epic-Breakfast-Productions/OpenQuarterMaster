

function getQuantityObj(value, unit){
	let output = {
		unit: {
			string: unit
		},
		scale: "ABSOLUTE",
		value: value
	};



	return output;
}