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
		newInput: function (data = null) {
			let newInput = $(PageComponents.Inputs.storedPricing);

			if(data != null) {
				//TODO:: populate data
			}

			Pricing.PriceInput.updateInputDisplays(newInput);

			return newInput;
		},
		getPerUnitSwitch: function(priceInputJq){
			return priceInputJq.find("input[name='pricePerUnitToggle']");
		},
		getPerUnitInputContainer: function(priceInputJq){
			return priceInputJq.find(".pricingInputPricePerUnitContainer");
		},

		updateInputDisplays: function (priceInnerElem) {
			console.log("Updating input displays for: ", priceInnerElem);
			let priceInput = Pricing.PriceInput.getInput(priceInnerElem);

			{// per unit
				let perUnitContainer = Pricing.PriceInput.getPerUnitInputContainer(priceInput);
				let perUnitSwitch = Pricing.PriceInput.getPerUnitSwitch(priceInput);
				let showPerUnit = perUnitSwitch.is(":checked");

				if(showPerUnit){
					perUnitContainer.show();
				} else {
					perUnitContainer.hide();
				}
			}
		},
		getData: function (priceInputPriceJq) {

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

	addPrice: function (priceInputJq, priceData = null) {
		let pricesContainer = Pricing.getPricesContainer(priceInputJq);

		let newInput = Pricing.PriceInput.newInput(priceData);
		pricesContainer.append(newInput);

		return newInput;
	},

	getPricingData: function (priceInputJq) {
		let output = [];

		let prices = Pricing.getPrices(priceInputJq);

		prices.forEach(function(i, priceJq){
			output.append(
				Pricing.PriceInput.getData(priceJq)
			);
		});

		return output;
	},

	populateInput: function (priceInputJq, priceList){

	}
};
