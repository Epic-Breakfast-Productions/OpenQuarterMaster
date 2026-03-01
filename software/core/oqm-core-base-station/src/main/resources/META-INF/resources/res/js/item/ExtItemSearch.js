import {Icons} from "../Icons.js";
import {Rest} from "../Rest.js";
import {ImageSearchSelect} from "../obj/media/ImageSearchSelect.js";
import {ItemAddEdit} from "../obj/item/ItemAddEdit.js";
import {KeywordAttUtils} from "../obj/ObjViewUtils.js";
import {KeywordAttEdit} from "../obj/ObjEditUtils.js";
import {PageMessageUtils} from "../PageMessageUtils.js";
import {Identifiers} from "../Identifiers.js";

export const ExtItemSearch = {
	extSearchResults: $("#extSearchResults"),

	prodBarcodeSearchForm: $("#prodBarcodeSearchForm"),
	legoPartNumSearchForm: $("#legoPartNumSearchForm"),
	websiteScanSearchForm: $("#websiteScanSearchForm"),
	extItemSearchSearchFormMessages: $("#extItemSearchSearchFormMessages"),

	prodBarcodeSearchBarcodeInput: $("#prodBarcodeSearchBarcodeInput"),
	legoPartNumSearchInput: $("#legoPartNumSearchInput"),
	websiteScanSearchInput: $("#websiteScanSearchInput"),

	addEditProductSearchPane: $("#addEditProductSearchPane"),

	getUseButton(text) {
		let newButton = $('<button type="button" class="btn btn-link mb-0 p-0" title="Use this value"></button>');

		if (text) {
			newButton.text(text + ": ");
		}
		newButton.append(Icons.useDatapoint);

		return newButton;
	},

	createSearchResultSection(name, value, targetInput) {
		let section = $('<li class="list-group-item extProdResultSection"><h6 class="card-title"></h6></li>');

		if (targetInput) {
			let useButton = ExtItemSearch.getUseButton(name);
			useButton.on("click", function (e) {
				let valElement;
				if (e.target.tagName === "I") {
					valElement = $(e.target.parentElement.parentElement.nextElementSibling);
				} else {
					valElement = $(e.target.parentElement.nextElementSibling)
				}
				targetInput.val(valElement.text());
			});
			section.children("h6").append(useButton);
		} else {
			section.children("h6").text(name + ":");
		}

		let sectionText = $('<p class="card-text"></p>');
		sectionText.text(value);
		section.append(sectionText);

		return section;
	},
	async getImageBase64FromUrl(imageUrl) {
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
	},
	addOrGetAndSelectImage(imageUrl, resultUnifiedName, imageData) {
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
					//TODO:: use image add form to add image, come back to this?
					let saveImageFail = false;

					let filename = new URL(imageUrl).pathname;
					filename = filename.substring(filename.lastIndexOf('/') + 1);
					if(filename.includes(".")){
						filename = filename.split('.').slice(0, -1).join('.')
					}
					filename += "."+imageData.split(';')[0].split('/')[1];

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
	},
	carouselNum: 0,
	async handleExtItemSearchResult(result){
		//TODO:: better formatting, method for filling out values
		let resultCard = $('<div class="card col-12 p-0" style="height: fit-content"></div>');
		{
			let header = $('<div class="card-header"></div>');
			header.text(result.source);
			resultCard.append(header);
		}
		let resultMainBody = $('<ul class="list-group list-group-flush"></ul>');
		resultMainBody.append(ExtItemSearch.createSearchResultSection("Name", result.unifiedName, ItemAddEdit.addEditItemNameInput));
		resultMainBody.append(ExtItemSearch.createSearchResultSection("Description", result.description, ItemAddEdit.addEditItemDescriptionInput));

		/* TODO:: */
		if (result.images.length) {
			//TODO:: add minimum height/width, set unique car id
			let carouselId = "extSearchResultImgCarousel-" + ExtItemSearch.carouselNum++;
			let imagesSection = $('<li class="list-group-item extProdResultSection"><h6 class="card-title">Images:</h6></li>');

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

			let imgPromises = [];
			result.images.forEach(function (curImageLoc, i) {
				let curPromise = async function () {
					console.log("Getting image " + i);

					let imageData = await ExtItemSearch.getImageBase64FromUrl(curImageLoc);

					if (!imageData) {
						console.error("FAILED to get image data for " + i + " - " + curImageLoc);
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


			//TODO:: if no images, don't append

			console.log("Finished getting " + carouselInner.children().length + " images");
			if (carouselInner.children().length) {
				imagesSection.append(carousel);
				resultMainBody.append(imagesSection);
			}
		}/* */

		if (result.attributes) {
			let attsSection = $('<li class="list-group-item extProdResultSection"><h6 class="card-title">Attributes:</h6></li>');

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

		resultCard.append(resultMainBody);
		ExtItemSearch.extSearchResults.append(resultCard);
	},
	async handleExtItemSearchResults(results) {
		console.log("Got Results! # results: " + results.results.length + "  # errors: " + Object.keys(results.serviceErrs).length);

		if (results.results.length === 0) {
			ExtItemSearch.extSearchResults.html("<p>No Results!</p>");
		}
		let resultPromises = [];
		results.results.forEach(function (result) {
				resultPromises.push(ExtItemSearch.handleExtItemSearchResult(result));
			}
		);

		for (const [service, error] of Object.entries(results.serviceErrs)) {
			PageMessageUtils.addMessageToDiv(ExtItemSearch.extItemSearchSearchFormMessages, "danger", error, "Failed calling " + service);
		}
		await Promise.all(resultPromises);
		console.log("Finished processing ext item search results.");
	},


	toggleAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.toggle();
	},

	hideAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.hide();
	}
	,

	showAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.show();
	}
}

ExtItemSearch.websiteScanSearchForm.submit(function (event) {
	event.preventDefault();
	let webpage = ExtItemSearch.websiteScanSearchInput.val();
	console.log("Scanning a web page: " + webpage);
	ExtItemSearch.extSearchResults.html("");

	Rest.call({
		url: Rest.passRoot + "/plugin/itemLookup/webpage/scrape/" + encodeURIComponent(webpage),
		done: async function (data) {
			await ExtItemSearch.handleExtItemSearchResults(data);
		},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});

ExtItemSearch.prodBarcodeSearchForm.submit(function (event) {
	event.preventDefault();
	let barcodeText = ExtItemSearch.prodBarcodeSearchBarcodeInput.val();
	console.log("Searching for a barcode: ", barcodeText);
	Identifiers.getNewIdentifierInput(ItemAddEdit.identifierInputContainer).val(barcodeText);
	Identifiers.addIdentifier(ItemAddEdit.identifierInputContainer);
	ExtItemSearch.extSearchResults.html("");

	Rest.call({
		url: Rest.passRoot + "/plugin/itemLookup/barcode/" + barcodeText,
		done: async function (data) {
			await ExtItemSearch.handleExtItemSearchResults(data);
		},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});

ExtItemSearch.legoPartNumSearchForm.submit(function (event) {
	event.preventDefault();
	let partNumber = ExtItemSearch.legoPartNumSearchInput.val();
	console.log("Searching for a lego part: " + partNumber);
	ExtItemSearch.extSearchResults.html("");

	Rest.call({
		url: Rest.passRoot + "/plugin/itemLookup/lego/part/" + partNumber,
		done: async function (data) {
			await ExtItemSearch.handleExtItemSearchResults(data)
		},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});
