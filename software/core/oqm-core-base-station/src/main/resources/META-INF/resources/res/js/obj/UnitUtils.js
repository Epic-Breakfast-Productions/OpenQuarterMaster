import {Rest} from "../Rest.js";

export const UnitUtils = {
	compatibleUnitMap: null,
	allUnitOptionsCache: null,
	compatibleUnitOptionsCache: {},

	newUnitCompatible(unitSelectJq, newUnit){
		let found = unitSelectJq.find("option[value='"+newUnit+"']").length;
		return !!found;
	},
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
	getCompatibleUnitOptions: async function (unitStrOrAmtStored) {
		let wasStored = false;
		let unitStr = unitStrOrAmtStored;

		if (typeof unitStrOrAmtStored === 'object' && unitStrOrAmtStored !== null && !Array.isArray(unitStrOrAmtStored)) {
			wasStored = true;
			unitStr = unitStrOrAmtStored.amount.unit.string;
		}
		if(unitStrOrAmtStored == null){
			console.warn("Empty unit given. Can't get compatible unit options.");
			return null;
		}
		console.log("Getting compatible inputs for unit: ", unitStr, unitStrOrAmtStored);

		if (!(unitStr in this.compatibleUnitOptionsCache)) {
			console.debug("Cache miss, getting compatible units for ", unitStr);
			await Rest.call({
				method: "GET",
				url: Rest.apiRoot + "/pageComponents/unit/inputs/compatibleWith/"+unitStr,
				returnType: "html",
				extraHeaders: {
					"accept": "text/html",
				},
				done: function (compatibleUnitOptions) {
					UnitUtils.compatibleUnitOptionsCache[unitStr] = compatibleUnitOptions;
				}
			});
		}

		let output = $(this.compatibleUnitOptionsCache[unitStr]).clone();

		if(wasStored){
			console.log("Had stored object.");
			let quantity = unitStrOrAmtStored.amount;
			let convData = [];

			output.each(function(i, option){
				let optionJq = $(option);
				let curUnit = UnitUtils.getUnitObj(optionJq.val());
				convData.push(UnitUtils.getConvertRequestObj(quantity, curUnit));
			});

			let convertedQuantities = await UnitUtils.convertQuantity(convData);

			output.each(function(i, option){
				let optionJq = $(option);
				let convertedQuantity = convertedQuantities[i];

				optionJq.attr("data-amount-converted-original", convertedQuantity.value);
				optionJq.attr("data-max-value", convertedQuantity.value);
			});
		}

		console.debug("Got compatible unit options: ", output)
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
		return {
			string: unitStr
		};
	},
	getQuantityObj(value, unit) {
		return {
			unit: UnitUtils.getUnitObj(unit),
			scale: "ABSOLUTE",
			value: value
		};
	},
	getQuantityFromInputs(inputsContainerJq) {
		let output = this.getQuantityObj(
			inputsContainerJq.find('input[name="amountStored"]').val(),
			inputsContainerJq.find('select[name="amountStoredUnit"]').val()
		);
		console.debug("Got quantity from form: ", output);

		return output;
	},
	getConvertRequestObj(quantityObj, unitObj){
		return {
			quantity: quantityObj,
			newUnit: unitObj,
		};
	},
	convertQuantity: async function(requestObj){
		let output = null;
		await Rest.call({
			spinnerContainer: null,
			method: "PUT",
			url: Rest.passRoot + "/inventory/unit/convert",
			data: requestObj,
			done: function (resultQuantity) {
				console.log("Got converted quantity: ", resultQuantity);
				output = resultQuantity;
			}
		});
		return output;
	}
};
