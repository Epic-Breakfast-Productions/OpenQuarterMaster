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
			let newInput = $(PageComponents.Inputs.Pricing.storedPricing);

			await UnitUtils.getCompatibleUnitOptions(unit).then(function (options) {
				console.log("Populating price per unit units: ", unit, options);
				Pricing.PriceInput.getPerUnitUnitInput(newInput)
					.html(options);
			});

			if (data != null) {
				Pricing.PriceInput.getLabelInput(newInput).val(data.label);
				Pricing.PriceInput.getFlatPriceInput(newInput).val(data.flatPrice.amount);

				if(data.pricePerUnit){
					Pricing.PriceInput.getPerUnitSwitch(newInput).attr("checked", true);
					Pricing.PriceInput.getPerUnitAmountInput(newInput).val(data.pricePerUnit.price.amount);
					Pricing.PriceInput.getPerUnitUnitInput(newInput).val(data.pricePerUnit.unit.string);
				}
			}

			Pricing.PriceInput.updateInputDisplays(newInput);

			return newInput;
		},
		getLabelInput: function (priceInputJq) {
			return priceInputJq.find("input[name='label']");
		},
		getCurrency: function (priceInputJq) {
			return priceInputJq.data("currency");
		},
		getFlatPriceInput: function (priceInputJq) {
			return priceInputJq.find("input[name='flatPrice']");
		},
		getPerUnitSwitch: function (priceInputJq) {
			return priceInputJq.find("input[name='pricePerUnitToggle']");
		},
		getPerUnitInputContainer: function (priceInputJq) {
			return priceInputJq.find(".pricingInputPricePerUnitContainer");
		},
		getPerUnitAmountInput: function (priceInputJq) {
			return priceInputJq.find("input[name='pricePerUnitAmount']");
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
			let data = {
				label: Pricing.PriceInput.getLabelInput(priceInputPriceJq).val(),
				flatPrice: {
					"amount": Pricing.PriceInput.getFlatPriceInput(priceInputPriceJq).val(),
					"currency": Pricing.PriceInput.getCurrency(priceInputPriceJq)
				}
			};

			if (Pricing.PriceInput.getPerUnitSwitch(priceInputPriceJq).is(":checked")) {
				data["pricePerUnit"] = {
					"price": {
						"amount": Pricing.PriceInput.getPerUnitAmountInput(priceInputPriceJq).val(),
						"currency": Pricing.PriceInput.getCurrency(priceInputPriceJq)
					},
					"unit": UnitUtils.getUnitObj(Pricing.PriceInput.getPerUnitUnitInput(priceInputPriceJq).val())
				}
			}

			return data;
		}
	},
	newInput: async function(unit, data = null){
		let newInput = $(PageComponents.Inputs.Pricing.priceInput);

		await Pricing.populateInput(newInput, unit, data);

		return newInput;
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
		newInput = await newInput;
		pricesContainer.append(newInput);

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

		prices.each(function (i, priceJs) {
			output.push(
				Pricing.PriceInput.getData($(priceJs))
			);
		});

		return output;
	},
	populateInput: async function (priceInputJq, unit, priceList = null) {
		console.log("Populating pricing input: ",  priceInputJq, unit, priceList);
		let tasks = [];

		tasks.push(Pricing.setUnit(priceInputJq, unit));

		if(priceList) {
			priceList.forEach(function (curPriceData) {
				Pricing.addPrice(priceInputJq, curPriceData);
			});
		}

		await Promise.all(tasks);
	},
	View: {
		newPriceContainer: function () {
			return $(`
			<div class="prceDisplay card">
				
			</div>
			`);
		},

		StoredPricing: {
			showInDiv(divJq, pricingArray) {
				//TODO
			}
		},
		CalculatedPricing: {
			showInDiv(divJq, storedData) {
				storedData.calculatedPrices.forEach(function (curPriceData) {
					let newDisplay = Pricing.View.newPriceContainer();
					newDisplay.append(
						$(`<h5></h5>`).text(curPriceData.label)
					);
					newDisplay.append(
						$(`<p></p>`).text(curPriceData.totalPriceString)
					);
					divJq.append(newDisplay);

					//TODO:: more, detail
				});
			}
		},
		TotalPricing: {
			showInDiv(divJq, pricingArray) {
				pricingArray.each(function (curTotalPriceData) {
					let newDisplay = Pricing.View.newPriceContainer();
					newDisplay.append(
						$(`<h5></h5>`).text(curTotalPriceData.label)
					);
					newDisplay.append(
						$(`<p></p>`).text(curTotalPriceData.totalPriceString)
					);
					divJq.append(newDisplay);

					//TODO:: more, detail
				});
			}
		}
	}
};
