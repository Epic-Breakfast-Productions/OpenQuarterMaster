Pricing = {
	PriceInput: {
		getInput: function (innerElem) {
			let output = innerElem;
			if (!output.jquery) {
				output = $(output);
			}
			if (!output.hasClass("pricingInputPrice")) {
				output = output.closest(".pricingInputPrice");
			}
			return output;
		},
		newInput: async function (unit, data = null) {
			let newInput = $(PageComponents.Inputs.storedPricing);

			await UnitUtils.getCompatibleUnitOptions(unit).then(function (options) {
				console.log("Populating price per unit units: ", unit, options);
				Pricing.PriceInput.getPerUnitUnitInput(newInput)
					.html(options);
			});

			if (data != null) {
				//TODO:: populate data
			}

			Pricing.PriceInput.updateInputDisplays(newInput);

			return newInput;
		},
		getPerUnitSwitch: function (priceInputJq) {
			return priceInputJq.find("input[name='pricePerUnitToggle']");
		},
		getPerUnitInputContainer: function (priceInputJq) {
			return priceInputJq.find(".pricingInputPricePerUnitContainer");
		},
		getPerUnitUnitInput: function (priceInputJq) {
			return priceInputJq.find("select[name='pricePerUnitUnit']");
		},

		updateInputDisplays: function (priceInnerElem) {
			console.log("Updating input displays for: ", priceInnerElem);
			let priceInput = Pricing.PriceInput.getInput(priceInnerElem);

			{// per unit
				let perUnitContainer = Pricing.PriceInput.getPerUnitInputContainer(priceInput);
				let perUnitSwitch = Pricing.PriceInput.getPerUnitSwitch(priceInput);
				let showPerUnit = perUnitSwitch.is(":checked");

				if (showPerUnit) {
					perUnitContainer.show();
				} else {
					perUnitContainer.hide();
				}
			}
		},
		getData: function (priceInputPriceJq) {
			//TODO
			let data = {
				label: "",
				flatPrice: {
					"valueStr": "",
					"currency": ""
				}
			};

			if (true) {

				//TODO:: per unit
				data["pricePerUnit"] = {
					"price": "",
					"unit": ""
				}
			}


			return data;
		}
	},
	getInput: function (innerElem) {
		let output = innerElem;
		if (!output.jquery) {
			output = $(output);
		}
		if (!output.hasClass("pricingInput")) {
			output = output.closest(".pricingInput");
		}
		return output;
	},
	getPricesContainer: function (priceInputJq) {
		return priceInputJq.find(".pricesContainer");
	},
	getPrices: function (priceInputJq) {
		let pricesContainer = Pricing.getPricesContainer(priceInputJq);

		return pricesContainer.find(".pricingInputPrice");
	},
	resetInput: function (priceInputJq) {
		Pricing.getPricesContainer(priceInputJq).html("");
		Pricing.setUnit(priceInputJq, "");
	},
	setUnit: async function (priceInputJq, unit) {
		if (Pricing.getUnit(priceInputJq) == unit) {
			console.debug("Unit set already the given unit: ", unit);
			return;
		}
		console.log("Setting price input to unit: ", unit);

		priceInputJq.data("unit", unit);

		let prices = Pricing.getPrices(priceInputJq);

		if (prices.length) {
			let needUnitUpdate = !UnitUtils.newUnitCompatible(
				Pricing.PriceInput.getPerUnitUnitInput($(prices[0])),
				unit
			);
			if (needUnitUpdate) {
				console.info("Updating pricing units.");
				let unitChangePromises = [];

				prices.each(function (i, curPriceInput) {
					let perUnitUnitInput = Pricing.PriceInput.getPerUnitUnitInput($(curPriceInput));

					unitChangePromises.push(
						UnitUtils.getCompatibleUnitOptions(unit).then(function (ops) {
							perUnitUnitInput.html(ops);
						})
					);
				});

				await Promise.all(unitChangePromises);
			} else {
				console.info("Did not need to update pricing units.");
			}
		} else {
			console.debug("No prices to update units of.");
		}
	},
	getUnit: function (priceInputJq) {
		return priceInputJq.data("unit");
	},

	addPrice: async function (priceInputJq, priceData = null) {
		console.info("Adding new price input to ", priceInputJq);
		let pricesContainer = Pricing.getPricesContainer(priceInputJq);

		let newInput = Pricing.PriceInput.newInput(
			Pricing.getUnit(priceInputJq),
			priceData
		);
		pricesContainer.append(await newInput);

		return newInput;
	},
	removePrice(remButtJq) {
		if (confirm("Are you sure?") === false) return;
		SelectedObjectDivUtils.removeSelected(
			Pricing.PriceInput.getInput(remButtJq)
		);
	},

	getPricingData: function (priceInputJq) {
		let output = [];

		let prices = Pricing.getPrices(priceInputJq);

		prices.forEach(function (i, priceJq) {
			output.append(
				Pricing.PriceInput.getData(priceJq)
			);
		});

		return output;
	},
	populateInput: function (priceInputJq, unit, priceList) {
		Pricing.setUnit(priceInputJq, unit);

		priceList.foreach(function (curPriceData) {
			Pricing.addPrice(curPriceData);
		});
	}
};
