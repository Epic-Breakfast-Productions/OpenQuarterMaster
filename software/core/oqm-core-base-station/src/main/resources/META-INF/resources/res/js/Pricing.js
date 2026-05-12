import {UnitUtils} from "./obj/UnitUtils.js";
import {PageComponents} from "./PageComponents.js";
import {SelectedObjectDivUtils} from "./SelectedObjectDivUtils.js";
import {PageUtility} from "./utilClasses/PageUtility.js";

export class Pricing extends PageUtility {
	static PriceInput = class {
		static getInput(innerElem) {
			let output = innerElem;
			if (!output.jquery) {
				output = $(output);
			}
			if (!output.hasClass("pricingInputPrice")) {
				output = output.closest(".pricingInputPrice");
			}
			return output;
		}
		static async newInput(unit, data = null) {
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
		}
		static getLabelInput(priceInputJq) {
			return priceInputJq.find("input[name='label']");
		}
		static getCurrency(priceInputJq) {
			return priceInputJq.data("currency");
		}
		static getFlatPriceInput(priceInputJq) {
			return priceInputJq.find("input[name='flatPrice']");
		}
		static getPerUnitSwitch(priceInputJq) {
			return priceInputJq.find("input[name='pricePerUnitToggle']");
		}
		static getPerUnitInputContainer(priceInputJq) {
			return priceInputJq.find(".pricingInputPricePerUnitContainer");
		}
		static getPerUnitAmountInput(priceInputJq) {
			return priceInputJq.find("input[name='pricePerUnitAmount']");
		}
		static getPerUnitUnitInput(priceInputJq) {
			return priceInputJq.find("select[name='pricePerUnitUnit']");
		}

		static updateInputDisplays(priceInnerElem) {
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
		}
		static getData(priceInputPriceJq) {
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
	}
	static async newInput(unit, data = null){
		let newInput = $(PageComponents.Inputs.Pricing.priceInput);

		await Pricing.populateInput(newInput, unit, data);

		return newInput;
	}
	static getInput(innerElem) {
		let output = innerElem;
		if (!output.jquery) {
			output = $(output);
		}
		if (!output.hasClass("pricingInput")) {
			output = output.closest(".pricingInput");
		}
		return output;
	}
	static getPricesContainer(priceInputJq) {
		return priceInputJq.find(".pricesContainer");
	}
	static getPrices (priceInputJq) {
		let pricesContainer = Pricing.getPricesContainer(priceInputJq);

		return pricesContainer.find(".pricingInputPrice");
	}
	static resetInput (priceInputJq) {
		Pricing.getPricesContainer(priceInputJq).html("");
		Pricing.setUnit(priceInputJq, "");
	}
	static async setUnit(priceInputJq, unit) {
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
	}
	static getUnit (priceInputJq) {
		return priceInputJq.data("unit");
	}

	static async addPrice(priceInputJq, priceData = null) {
		console.info("Adding new price input to ", priceInputJq);
		let pricesContainer = Pricing.getPricesContainer(priceInputJq);

		let newInput = Pricing.PriceInput.newInput(
			Pricing.getUnit(priceInputJq),
			priceData
		);
		newInput = await newInput;
		pricesContainer.append(newInput);

		return newInput;
	}
	static removePrice(remButtJq) {
		if (confirm("Are you sure?") === false) return;
		SelectedObjectDivUtils.removeSelected(
			Pricing.PriceInput.getInput(remButtJq)
		);
	}

	static getPricingData(priceInputJq) {
		let output = [];

		let prices = Pricing.getPrices(priceInputJq);

		prices.each(function (i, priceJs) {
			output.push(
				Pricing.PriceInput.getData($(priceJs))
			);
		});

		return output;
	}
	static async populateInput(priceInputJq, unit, priceList = null) {
		console.log("Populating pricing input: ",  priceInputJq, unit, priceList);
		let tasks = [];

		tasks.push(Pricing.setUnit(priceInputJq, unit));

		if(priceList) {
			priceList.forEach(function (curPriceData) {
				Pricing.addPrice(priceInputJq, curPriceData);
			});
		}

		await Promise.all(tasks);
	}
	static View = class {
		static newPriceContainer(priceData, extraClasses="") {
			let output = $(`
			<div class="priceDisplayContainer p-1 `+extraClasses+`">
				<div class="priceDisplay card">
					<div class="card-header">
						<span class="card-body priceFromDefaultInd px-0 d-none" title="Default from item">*</span>
						<span class="priceLabel h4"></span> -
						<span class="pricePrice h4"></span>
						<span class="priceAsOfDateContainer"></span>
						<button class="btn btn-sm btn-link float-end showPriceDropdownButton d-none" type="button" title="Show Price Breakdown" onclick="Pricing.View.toggleBreakdownView($(this))">`+Icons.dropdown+`</button>
					</div>
					<div class="card-body priceBreakdownContainer d-none">
						<div class="mb-1 priceBreakdownFixedPriceContainer">
							<span class="h5">Fixed Price:</span>
							<span class="priceBreakdownFixedPrice"></span>
						</div>
						<div class="mb-1 priceBreakdownPerUnitPriceContainer d-none">
							+
							<span class="h5">Per-Unit Price:</span>
							<span class="priceBreakdownPerUnitPrice"></span>
						</div>
					</div>
				</div>
			</div>
			`);

			output.find(".priceLabel").text(priceData.label);

			if(priceData.asOfDate){
				let dateDisplay = output.find(".priceAsOfDateContainer");
				dateDisplay.text(priceData.asOfDate);
				dateDisplay.show();
			}

			return output;
		}
		static toggleBreakdownView(breakdownButtonJq){
			breakdownButtonJq.parent().parent().find(".priceBreakdownContainer").toggleClass("d-none");
		}

		static CalculatedPricing = class {
			static showInDiv(divJq, pricingArray, extraClasses="") {
				pricingArray.forEach(function (curPriceData) {
					let newDisplay = Pricing.View.newPriceContainer(curPriceData, extraClasses);
					newDisplay.find(".pricePrice").text(curPriceData.totalPriceString);
					divJq.append(newDisplay);

					if(curPriceData.fromDefault){
						newDisplay.find(".priceFromDefaultInd").removeClass("d-none");
					}

					newDisplay.find(".priceBreakdownFixedPrice").text(curPriceData.flatPriceString);

					if(curPriceData.perUnitPriceString) {
						newDisplay.find(".priceBreakdownPerUnitPrice").text(curPriceData.perUnitPriceString);
						newDisplay.find(".priceBreakdownPerUnitPriceContainer").removeClass("d-none");
					}

					newDisplay.find(".showPriceDropdownButton").removeClass("d-none");
				});
			}
		}
		static TotalPricing = class {
			static showInDiv(divJq, pricingArray, extraClasses="") {
				pricingArray.forEach(function (curTotalPriceData) {
					let newDisplay = Pricing.View.newPriceContainer(curTotalPriceData, extraClasses);

					newDisplay.find(".pricePrice").text(curTotalPriceData.totalPriceString);

					divJq.append(newDisplay);
				});
			}
		}
	}
	static {
		window.Pricing = Pricing;
	}
}
