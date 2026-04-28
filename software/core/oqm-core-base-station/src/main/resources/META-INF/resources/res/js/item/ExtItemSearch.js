import {Icons} from "../Icons.js";
import {Rest} from "../Rest.js";
import {ImageSearchSelect} from "../obj/media/ImageSearchSelect.js";
import {ItemAddEdit} from "../obj/item/ItemAddEdit.js";
import {KeywordAttUtils} from "../obj/ObjViewUtils.js";
import {KeywordAttEdit} from "../obj/ObjEditUtils.js";
import {PageMessageUtils} from "../PageMessageUtils.js";
import {Identifiers} from "../Identifiers.js";
import {Pricing} from "../Pricing.js";
import {PageUtility} from "../utilClasses/PageUtility.js";
import {AssociatedLinks} from "../AssociatedLinks.js";

export class ExtItemSearch extends PageUtility {
	static addEditProductSearchPane = $("#addEditProductSearchPane");

	static searchForm = $("#addEditProductSearchForm");

	static extSearchResultsContainer = $("#extSearchResults");
	static searchResultsCount = $("#extItemSearchSearchResultsTabNumResults");
	static searchResultsErrTab = $("#extItemSearchSearchResultsTabErrorsTab");
	static searchResultsErrCount = $("#extItemSearchSearchResultsTabNumErrors");
	static searchResultsErrContent = $("#extItemSearchSearchResultsTabErrorsTabContent");
	static extSearchResultsTableContent = $("#extItemSearchSearchResultsTableResults");
	static extItemSearchSearchFormMessages = $("#extItemSearchSearchFormMessages");


	static resetSearchResults() {
		ExtItemSearch.extSearchResultsContainer.hide();
		ExtItemSearch.searchResultsCount.text("-");
		ExtItemSearch.searchResultsErrCount.text("-");
		ExtItemSearch.extSearchResultsTableContent.text("");
		ExtItemSearch.searchResultsErrContent.text("");
		ExtItemSearch.searchResultsErrTab.prop("disabled", true);
	}

	static sourceToDisplay(source) {
		return source;//TODO
	}

	static serviceToDisplay(service) {
		return service;//TODO
	}

	static methodToDisplay(method) {
		return method;//TODO
	}


	static getUseButton(text) {
		let newButton = $('<button type="button" class="btn btn-link mb-0 p-0" title="Use this value"></button>');

		if (text) {
			newButton.text(text + ": ");
		}
		newButton.append(Icons.useDatapoint);

		return newButton;
	}

	static createSearchResultSection(name, value, targetInput) {
		let section = $('<div><h6 class="card-title"></h6></div>');

		if (targetInput) {
			let useButton = ExtItemSearch.getUseButton(name);
			useButton.on("click", function (e) {
				let valElement;
				if (e.target.tagName === "I") {
					valElement = $(e.target.parentElement.parentElement.nextElementSibling);
				} else {
					valElement = $(e.target.parentElement.nextElementSibling)
				}

				if (targetInput.jquery) {
					targetInput.val(valElement.text());
				} else {//assuming overType
					targetInput.setValue(valElement.text());
				}
			});
			section.children("h6").append(useButton);
		} else {
			section.children("h6").text(name + ":");
		}

		let sectionText = $('<p class="card-text"></p>');
		sectionText.text(value);
		section.append(sectionText);
		section.append($('<hr style="width:50%;">'));

		return section;
	}

	static async getImageBase64FromUrl(imageUrl) {
		let output = false;

		try {
			let imageData = null;
			let downloadedImg = new Image();
			downloadedImg.crossOrigin = "";
			downloadedImg.addEventListener("load", () => {
				try {
					const canvas = document.createElement("canvas");
					const context = canvas.getContext("2d");

					canvas.width = downloadedImg.width;
					canvas.height = downloadedImg.height;
					canvas.innerText = downloadedImg.alt;

					context.drawImage(downloadedImg, 0, 0);
					imageData = canvas.toDataURL("image/png");
				} catch (err) {
					imageData = `Error: ${err}`;
				}
			}, false);
			downloadedImg.src = imageUrl;

			output = await new Promise((resolve, reject) => {
				downloadedImg.onerror = reject;
				downloadedImg.onload = () => {
					resolve(imageData);
				}
				// downloadedImg.readAsDataURL(imageData);
			});
			console.log("Data from img: " + output);
		} catch (e) {
			console.log("FAILED to get image data: " + e);
		}

		return output;
	}

	static addOrGetAndSelectImage(imageUrl, resultUnifiedName, imageData) {
		console.log("Setting image for item. Image source: " + imageUrl);

		Rest.call({
			url: Rest.passRoot + "/media/image?" + new URLSearchParams([["source", imageUrl]]).toString(),
			method: "GET",
			failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages,
			done: async function (data) {
				console.log("Got search result.");
				let imageId;
				let imageName = resultUnifiedName;
				if (!data.length) {
					console.log("No results for given source. Adding.");
					//TODO:: use image add form to add image?
					let saveImageFail = false;

					let filename = new URL(imageUrl).pathname;
					filename = filename.substring(filename.lastIndexOf('/') + 1);
					if (filename.includes(".")) {
						filename = filename.split('.').slice(0, -1).join('.')
					}
					filename += "." + imageData.split(';')[0].split('/')[1];

					let addData = new FormData();
					addData.append("fileName", filename);
					addData.append("description", "");
					addData.append("source", imageUrl);
					addData.append("file", await (await (await fetch(imageData)).blob()));

					await Rest.call({
						async: false,
						url: Rest.passRoot + "/media/image",
						method: "POST",
						data: addData,
						dataType: false,
						failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages,
						fail: function () {
							saveImageFail = true;
						},
						done: function (data) {
							console.log("Added image from url! " + data);
							imageId = data.id;
						}
					});

					if (saveImageFail) {
						return;
					}
					// return;
				} else {
					imageId = data[0].id;
					imageName = data[0].title;
				}

				ImageSearchSelect.curImagesSelectedDiv = ItemAddEdit.addEditItemImagesSelected;
				ImageSearchSelect.selectImage(imageName, imageId);
			}
		});
		return true;
	}

	static errCount = 0;

	static async handleErrResult(result) {
		ExtItemSearch.errCount++;

		let newAlert = $('<div class="alert alert-danger" role="alert"></div>');

		newAlert.append($('<h4 class="alert-heading">Service:</h4>').text("Service: " + result.service));
		newAlert.append($('<p></p>').text(result.displayMessage));

		ExtItemSearch.searchResultsErrContent.append(newAlert);
	}

	static async handleNotFoundResult(result) {
		//TODO
	}

	static resultCount = 0;

	static getItemResultRow(result) {
		let resultRow = $('<tr></tr>');

		resultRow.append($('<td class="text-center align-middle"></td>').text(++ExtItemSearch.resultCount));


		let service = ExtItemSearch.serviceToDisplay(result.service);

		resultRow.append($('<td class=" align-middle"></td>')
			.append($('<span></span>').text(result.name))
			.append($('<br />'))
			.append(
				$('<small class="text-muted"></small>')
					.append($('<span></span>').text(
							result.service === result.source ?
								service :
								service + " (" + ExtItemSearch.sourceToDisplay(result.source) + ")"
						)
					)
					.append(' / ')
					.append($('<span></span>').text(ExtItemSearch.methodToDisplay(result.method)))
			)

		);

		resultRow.append($('<td class="text-center align-middle"></td>').text(result.images.length));
		resultRow.append($('<td class="text-center align-middle"></td>').text(Object.keys(result.prices).length));
		resultRow.append($('<td class="text-center align-middle"></td>').text(Object.keys(result.links).length));
		resultRow.append($('<td class="text-center align-middle"></td>').text(Object.keys(result.identifiers).length));
		resultRow.append($('<td class="text-center align-middle"><button class="btn btn-sm btn-primary viewButton">' + Icons.view + '</button></td>'));

		return resultRow;
	}

	static carouselNum = 0;

	static async getItemResultViewRow(result) {
		//TODO:: promise for waiting on image load... need to diagnose this

		let resultViewRow = $('<tr></tr>');
		let resultMainBody = $('<td colspan="7"></td>');

		resultMainBody.append(ExtItemSearch.createSearchResultSection("Name", result.unifiedName, ItemAddEdit.addEditItemNameInput));
		resultMainBody.append(ExtItemSearch.createSearchResultSection("Description", result.description, ItemAddEdit.addEditItemDescriptionInput));


		if (Object.keys(result.prices).length) {
			let pricesSection = $('<div class="extProdResultSection"><h6 class="card-title">Prices:</h6></div>');

			let pricesList = $('<span></span>');
			Object.keys(result.prices).forEach(key => {
				let val = result.prices[key];

				let curAtt = KeywordAttUtils.getAttDisplay(key, val);
				let useButt = ExtItemSearch.getUseButton();

				useButt.on("click", function (e) {
					Pricing.addPrice(ItemAddEdit.addEditItemPricingInput, {
						"label": key,
						"flatPrice": {
							"amount": val
						}
					})
				});

				curAtt.append(useButt);

				pricesList.append(curAtt);
			});
			pricesSection.append(pricesList);

			pricesSection.append($('<hr style="width:50%;">'));
			resultMainBody.append(pricesSection);
		}

		if (Object.keys(result.identifiers).length) {
			let identifierSection = $('<div class="extProdResultSection"><h6 class="card-title">Identifiers:</h6></div>');

			let idList = $('<span></span>');
			Object.keys(result.identifiers).forEach(key => {
				let val = result.identifiers[key];

				let curAtt = KeywordAttUtils.getAttDisplay(key, val);
				let useButt = ExtItemSearch.getUseButton();

				useButt.on("click", function (e) {
					Identifiers.addIdentifierFromValue(
						ItemAddEdit.identifierInputContainer,
						val
					)
				});

				curAtt.append(useButt);

				idList.append(curAtt);
			});
			identifierSection.append(idList);

			identifierSection.append($('<hr style="width:50%;">'));
			resultMainBody.append(identifierSection);
		}

		if (Object.keys(result.links).length) {
			let linksSection = $('<div class="extProdResultSection"><h6 class="card-title">Links:</h6></div>');

			let linkList = $('<span></span>');
			Object.keys(result.links).forEach(key => {
				let val = result.links[key];

				let curAtt = KeywordAttUtils.getAttDisplay(key, val);
				let useButt = ExtItemSearch.getUseButton();

				useButt.on("click", function (e) {
					AssociatedLinks.Form.addLink(
						ItemAddEdit.linkInput,
						{
							"label": key,
							"link": val
						}
					)
				});

				curAtt.append(useButt);
				curAtt.append(
					$('<a target="_blank"></a>')
						.html(Icons.link)
						.attr("href", val)
				);

				linkList.append(curAtt);
			});
			linksSection.append(linkList);


			linksSection.append($('<hr style="width:50%;">'));
			resultMainBody.append(linksSection);
		}

		if (result.attributes) {
			let attsSection = $('<div class="extProdResultSection"><h6 class="card-title">Attributes:</h6></div>');

			let attsList = $('<span></span>');
			Object.keys(result.attributes).forEach(key => {
				let val = result.attributes[key];

				let curAtt = KeywordAttUtils.getAttDisplay(key, val);
				let useButt = ExtItemSearch.getUseButton();

				useButt.on("click", function (e) {
					KeywordAttEdit.addAttInput(
						ItemAddEdit.addEditAttDiv,
						key,
						val
					);
				});

				curAtt.append(useButt);

				attsList.append(curAtt);
			});
			attsSection.append(attsList);

			resultMainBody.append(attsSection);
		}

		if (result.images.length) {
			let carouselId = "extSearchResultImgCarousel-" + ExtItemSearch.carouselNum++;
			let imagesSection = $('<div class="extProdResultSection"><h6 class="card-title">Images:</h6></div>');

			let carousel = $('<div id="' + carouselId + '" class="carousel slide border border-1 extProductResultCarousel">\n' +
				'  <div class="carousel-inner">\n' +

				'  </div>\n' +
				'  <button class="carousel-control-prev" type="button" data-bs-target="#' + carouselId + '" data-bs-slide="prev">\n' +
				'    <span class="carousel-control-prev-icon" aria-hidden="true"></span>\n' +
				'    <span class="visually-hidden">Previous</span>\n' +
				'  </button>\n' +
				'  <button class="carousel-control-next" type="button" data-bs-target="#' + carouselId + '" data-bs-slide="next">\n' +
				'    <span class="carousel-control-next-icon" aria-hidden="true"></span>\n' +
				'    <span class="visually-hidden">Next</span>\n' +
				'  </button>\n' +
				'</div>');
			let carouselInner = carousel.find(".carousel-inner");

			let failedImages = [];
			let imgPromises = [];
			result.images.forEach(function (curImageLoc, i) {
				let curPromise = async function () {
					console.log("Getting image ", i);

					let imageData = await ExtItemSearch.getImageBase64FromUrl(curImageLoc);

					if (!imageData) {
						console.error("FAILED to get image data for " + i + " - " + curImageLoc);
						failedImages.push(curImageLoc);
						return;
					}
					let newCarImageDir = $(
						'    <div class="carousel-item">\n' +
						'      <img src="" class="d-block w-100" alt="External Item Search Result Image">\n' +
						'      <div class="carousel-caption d-md-block">' +
						'          ' +
						'      </div>' +
						'    </div>\n'
					);
					let newCarImage = newCarImageDir.find("img");
					newCarImage.prop("src", imageData);

					let useButton = $('<button type="button" class="btn btn-secondary" title="Use this image">Add & Select ' + Icons.useDatapoint + '</button>');
					useButton.on("click", function () {
						ExtItemSearch.addOrGetAndSelectImage(curImageLoc, result.unifiedName, imageData);
					});
					newCarImageDir.find(".carousel-caption").append(useButton);

					carouselInner.append(newCarImageDir);
					console.log("Finished getting image " + i);
				}

				imgPromises.push(curPromise());
			});
			await Promise.all(imgPromises);

			$(carouselInner.children()[0]).addClass('active');

			console.log("Finished getting " + carouselInner.children().length + " images");
			let hadContent = false;
			if (carouselInner.children().length) {
				hadContent = true;
				imagesSection.append(carousel);
			}
			if (failedImages.length) {
				hadContent = true;
				imagesSection.append($("<p>Failed to load images:</p>"));
				let failedImgList = $('<ul></ul>');
				failedImages.forEach(function (curFailedImg) {
					failedImgList.append(
						$("<li></li>").append(
							$('<a target="_blank"></a>')
								.text(curFailedImg)
								.attr("href", curFailedImg)
						)
					);
				});
				imagesSection.append(failedImgList);
			}
			if (hadContent) {
				imagesSection.append($('<hr style="width:50%;">'));
				resultMainBody.append(imagesSection);
			}
		}

		resultViewRow.append(resultMainBody);
		resultViewRow.hide();
		return resultViewRow;
	}

	static async handleItemResult(result) {
		let resultRow = ExtItemSearch.getItemResultRow(result);
		let resultViewRow = await ExtItemSearch.getItemResultViewRow(result);

		resultRow.find("button.viewButton").on("click", function () {
			resultViewRow.toggle();
		});

		ExtItemSearch.extSearchResultsTableContent.append(resultRow);
		ExtItemSearch.extSearchResultsTableContent.append(resultViewRow);
	}

	static handleExtItemSearchResult(result) {
		console.debug("Handling external item search result: ", result);
		switch (result.type) {
			case "SUCCESS":
				return ExtItemSearch.handleItemResult(result);
			case "NO_RESULTS":
				return ExtItemSearch.handleNotFoundResult(result);
			case "ERROR":
				return ExtItemSearch.handleErrResult(result);
		}
	}

	static async handleExtItemSearchResults(results) {
		console.log("Got Results! # results: " + results.length);


		ExtItemSearch.errCount = 0;
		ExtItemSearch.resultCount = 0;
		let resultPromises = [];
		results.forEach(
			function (result) {
				resultPromises.push(ExtItemSearch.handleExtItemSearchResult(result));
			}
		);

		if (ExtItemSearch.resultCount === 0) {
			ExtItemSearch.extSearchResultsTableContent.html("<tr><td colspan='7'>No Results!</td></tr>");
		}

		await Promise.all(resultPromises);
		ExtItemSearch.searchResultsCount.text(ExtItemSearch.resultCount);
		ExtItemSearch.searchResultsErrCount.text(ExtItemSearch.errCount);
		ExtItemSearch.extSearchResultsContainer.show();

		if (ExtItemSearch.errCount) {
			ExtItemSearch.searchResultsErrTab.prop("disabled", false);
		}

		console.log("Finished processing ext item search results.");
	}


	static toggleAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.toggle();
	}

	static hideAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.hide();
	}

	static showAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.show();
	}

	static {
		window.ExtItemSearch = this;

		ExtItemSearch.resetSearchResults();

		ExtItemSearch.searchForm.on("submit", function (event) {
			event.preventDefault();
			console.log("Performing external item search.");
			ExtItemSearch.resetSearchResults();

			let formData = new FormData(event.target);
			let params = new URLSearchParams(formData);

			Rest.call({
				url: Rest.passRoot + "/plugin/itemLookup/search?" + params.toString(),
				done: async function (data) {
					await ExtItemSearch.handleExtItemSearchResults(data);
				},
				failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
			});
		});

	}
}
