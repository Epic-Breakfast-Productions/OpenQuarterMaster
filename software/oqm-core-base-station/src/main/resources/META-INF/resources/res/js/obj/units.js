const UnitUtils = {
	allUnitOptionsCache: null,
	compatibleUnitOptionsCache: {},

	getAllUnitOptions: async function (selectedVal = null) {
		if (this.allUnitOptionsCache == null) {
			await Rest.call({
				method: "GET",
				url: Rest.apiRoot + "/pageComponents/unit/inputs/all",
				returnType: "html",
				extraHeaders: {
					"accept": "text/html",
				},
				done: function (allUnitOptions) {
					UnitUtils.allUnitOptionsCache = allUnitOptions;
				}
			});
		}

		let output = $(this.allUnitOptionsCache);

		if (selectedVal == null) {
			//TODO:: find value, mark as selected
		}
		return output;
	},
	getCompatibleUnitOptions: async function (unit, selectedVal = null) {
		if (!(unit in this.compatibleUnitOptionsCache)) {
			console.debug("Cache miss, getting compatible units for ", unit);
			await Rest.call({
				method: "GET",
				url: Rest.apiRoot + "/pageComponents/unit/inputs/compatibleWith/"+unit,
				returnType: "html",
				extraHeaders: {
					"accept": "text/html",
				},
				done: function (compatibleUnitOptions) {
					UnitUtils.compatibleUnitOptionsCache[unit] = compatibleUnitOptions;
				}
			});
		}

		let output = $(this.compatibleUnitOptionsCache[unit]);

		if (selectedVal == null) {
			//TODO:: find value, mark as selected
		}
		return output;
	},

	/**
	 * Example input: {"unit":{"string":"units","name":"Units","symbol":"units"},"scale":"ABSOLUTE","value":8}
	 * @param quantityObj
	 */
	quantityToDisplayStr(quantityObj) {
		return quantityObj.value + quantityObj.unit.symbol;
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
	},
	getQuantityFromInputs(inputsContainerJq) {
		let output = this.getQuantityObj(
			inputsContainerJq.find('input[name="amountStored"]').val(),
			inputsContainerJq.find('select[name="amountStoredUnit"]').val()
		);
		console.debug("Got quantity from form: ", output);

		return output;
	}
};
