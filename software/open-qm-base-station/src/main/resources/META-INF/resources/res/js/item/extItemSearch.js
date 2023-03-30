function toDataURL(src, callback, outputFormat) {
	let image = new Image();
	image.crossOrigin = 'Anonymous';
	image.onload = function () {
		let canvas = document.createElement('canvas');
		let ctx = canvas.getContext('2d');
		let dataURL;
		canvas.height = this.naturalHeight;
		canvas.width = this.naturalWidth;
		ctx.drawImage(this, 0, 0);
		dataURL = canvas.toDataURL(outputFormat);
		callback(dataURL);
	};
	image.src = src;
	if (image.complete || image.complete === undefined) {
		image.src = "data:image/gif;base64, R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";
		image.src = src;
	}
}


const ExtItemSearch = {
	extSearchResults: $("#extSearchResults"),

	prodBarcodeSearchForm: $("#prodBarcodeSearchForm"),
	legoPartNumSearchForm: $("#legoPartNumSearchForm"),
	websiteScanSearchForm: $("#websiteScanSearchForm"),
	extItemSearchSearchFormMessages: $("#extItemSearchSearchFormMessages"),

	prodBarcodeSearchBarcodeInput: $("#prodBarcodeSearchBarcodeInput"),
	legoPartNumSearchInput: $("#legoPartNumSearchInput"),
	websiteScanSearchInput: $("#websiteScanSearchInput"),

	addEditProductSearchPane: $("#addEditProductSearchPane"),

	getUseButton(text){
		let newButton = $('<button type="button" class="btn btn-link mb-0 p-0" title="Use this value"></button>');

		if(text) {
			newButton.text(text + ": ");
		}
		newButton.append(Icons.useDatapoint);

		return newButton;
	},

	createSearchResultSection(name, value, targetInput){
		let section = $('<li class="list-group-item extProdResultSection"><h6 class="card-title"></h6></li>');

		if(targetInput) {
			let useButton = ExtItemSearch.getUseButton(name);
			useButton.on("click", function (e) {
				let valElement;
				if(e.target.tagName === "I"){
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
	async getImageBase64FromUrl(imageUrl){
		let output = false;
		await doRestCall({
			async: false,
			url: imageUrl,
			method: "GET",
			crossDomain: true,
			failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages,
			done: async function (imageData, status, xhr){
				//TODO:: handle cases where already have proper formatted string?
				//TODO:: refactor base64 test
				let base64regex = /^([0-9a-zA-Z+/]{4})*(([0-9a-zA-Z+/]{2}==)|([0-9a-zA-Z+/]{3}=))?$/;
				if(!base64regex.test(imageData)){
					console.log("Image data from url was not base 64");
					console.log(typeof imageData);

					// var binary = "";
					// imageData = xhr.responseText;
					// var responseTextLen = imageData.length;
					//
					// for (let i = 0; i < responseTextLen; i++ ) {
					// 	binary += String.fromCharCode(imageData.charCodeAt(i) & 255)
					// }
					// imageData = btoa(binary);

					imageData = new Blob([imageData], {
						type:  xhr.getResponseHeader("content-type")
					});
					function readAsDataURL(file) {
						return new Promise((resolve, reject) => {
							const fr = new FileReader();
							fr.onerror = reject;
							fr.onload = () => {
								resolve(fr.result);
							}
							fr.readAsDataURL(file);
						});
					}
					let readProm =  readAsDataURL(imageData);
					let tempImageData = await readProm;
					imageData = tempImageData;

					// let reader = new FileReader();
					// reader.onloadend = function () {
					// 	imageData = reader.result;
					// }
					//
					// let base64Data = new Promise((resolve, reject) => {
					// 	reader.onerror = reject;
					// 	reader.onload = () => {
					// 		resolve(reader.result);
					// 	}
					// 	reader.readAsDataURL(imageData);
					// });
					// imageData = base64Data;


					// let image = new Image();
					// image.crossOrigin = 'Anonymous';
					// image.onload = function () {
					// 	let canvas = document.createElement('canvas');
					// 	let ctx = canvas.getContext('2d');
					// 	let dataURL;
					// 	canvas.height = this.naturalHeight;
					// 	canvas.width = this.naturalWidth;
					// 	ctx.drawImage(this, 0, 0);
					// 	dataURL = canvas.toDataURL(outputFormat);
					// 	callback(dataURL);
					// };
					// image.src = src;
					// if (image.complete || image.complete === undefined) {
					// 	image.src = "data:image/gif;base64, R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";
					// 	image.src = src;
					// }

					// let imageData = null;
					// let downloadedImg = new Image();
					// downloadedImg.crossOrigin = "Anonymous";
					// downloadedImg.addEventListener("load", ()=> {
					// 	try {
					// 		const canvas = document.createElement("canvas");
					// 		const context = canvas.getContext("2d");
					//
					// 		canvas.width = downloadedImg.width;
					// 		canvas.height = downloadedImg.height;
					// 		canvas.innerText = downloadedImg.alt;
					//
					// 		context.drawImage(downloadedImg, 0, 0);
					// 		imageData = canvas.toDataURL("image/png");
					// 	} catch (err) {
					// 		imageData = `Error: ${err}`;
					// 	}
					// }, false);
					// downloadedImg.src = imageUrl;
					//
					// while(!imageData){}
					//
					// console.log("Data from img: " + imageData);

					// imageData = btoa(
					// 	encodeURIComponent(imageData).replace(/%([a-f0-9]{2})/gi, (m, $1) => String.fromCharCode(parseInt($1, 16)))
					// );

					// imageData = btoa(imageData.reduce((data, val)=> {
					// 	return data + String.fromCharCode(val);
					// }, ''));

					// imageData = btoa(unescape(encodeURIComponent(imageData)));

					// let reader = new FileReader();
					// reader.onload = function() {
					// 	imageData = reader.result;
					// 	console.log("Finished converting to base 64");
					// }
					// reader.readAsDataURL(imageData);

					console.log("Image data converted to base 64.");
				}
				// imageData = "data:" + xhr.getResponseHeader("content-type") + ";base64," + imageData;

				output = imageData;
			}
		});
		console.log("Got image data string: " + output);
		return output;
	},
	addOrGetAndSelectImage(imageUrl, resultUnifiedName, newCarImage){
		console.log("Setting image for item. Image source: " + imageUrl);

		doRestCall({
			url: "/api/v1/media/image?" + new URLSearchParams([["source", imageUrl]]).toString(),
			method: "GET",
			failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages,
			done: async function (data){
				let imageId;
				let imageName = resultUnifiedName;
				if(!data.length){
					console.log("No results for given source. Adding.")
					//TODO:: use image add form to add image, come back to this?

					let imageData = await ExtItemSearch.getImageBase64FromUrl(imageUrl);

					if(!imageData){
						return;
					}

					let saveImageFail = false;

					await doRestCall({
						async: false,
						url: "/api/v1/media/image",
						method: "POST",
						data: {
							title: resultUnifiedName,
							source: imageUrl,
							imageData: imageData
						},
						failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages,
						fail: function (){
							saveImageFail = true;
						},
						done: function (data){
							console.log("Added image from url! " + data);
							imageId = data;
						}
					});

					if(saveImageFail){
						return;
					}
					// return;
				} else {
					imageId = data[0].id;
					imageName = data[0].title;
				}

				curImagesSelectedDiv = addEditItemImagesSelected;
				selectImage(imageName, imageId);
			}
		});
		return true;
	},

	handleExtItemSearchResults(results) {
		console.log("Got Results! # results: " + results.results.length + "  # errors: " + Object.keys(results.serviceErrs).length);

		if (results.results.length === 0) {
			ExtItemSearch.extSearchResults.html("<p>No Results!</p>");
		}
		results.results.forEach(function (result) {
			//TODO:: better formatting, method for filling out values
			let resultCard = $('<div class="card col-12 p-0" style="height: fit-content"></div>');
			{
				let header = $('<div class="card-header"></div>');
				header.text(result.source);
				resultCard.append(header);
			}
			let resultMainBody = $('<ul class="list-group list-group-flush"></ul>');
			resultMainBody.append(ExtItemSearch.createSearchResultSection("Name", result.unifiedName, addEditItemNameInput));
			resultMainBody.append(ExtItemSearch.createSearchResultSection("Description", result.description, addEditItemDescriptionInput));

			/* TODO:: */
			if(result.images.length){
				//TODO:: add minimum height/width, set unique car id
				let imagesSection = $('<li class="list-group-item extProdResultSection"><h6 class="card-title">Images:</h6></li>');

				let carousel = $('<div id="carouselExample" class="carousel slide border border-1">\n' +
					'  <div class="carousel-inner">\n' +

					'  </div>\n' +
					'  <button class="carousel-control-prev" type="button" data-bs-target="#carouselExample" data-bs-slide="prev">\n' +
					'    <span class="carousel-control-prev-icon" aria-hidden="true"></span>\n' +
					'    <span class="visually-hidden">Previous</span>\n' +
					'  </button>\n' +
					'  <button class="carousel-control-next" type="button" data-bs-target="#carouselExample" data-bs-slide="next">\n' +
					'    <span class="carousel-control-next-icon" aria-hidden="true"></span>\n' +
					'    <span class="visually-hidden">Next</span>\n' +
					'  </button>\n' +
					'</div>');
				let carouselInner = carousel.find(".carousel-inner");

				result.images.forEach(function (curImageLoc, i){
					let newCarImageDir = $(
						'    <div class="carousel-item '+(i === 0? 'active':'')+'">\n' +
						'      <img src="" class="d-block w-100" alt="...">\n' +
						'      <div class="carousel-caption d-none d-md-block">' +
						'          ' +
						'      </div>' +
						'    </div>\n'
					);
					let newCarImage = newCarImageDir.find("img");
					newCarImage.prop("src", curImageLoc);
					newCarImage.on("error", function (){
						console.log("Failed to load external image " + curImageLoc);
						newCarImageDir.remove();
					})

					let useButton = $('<button type="button" class="btn btn-secondary" title="Use this value">Use this image '+Icons.useDatapoint+'</button>');
					useButton.on("click", function(){
						ExtItemSearch.addOrGetAndSelectImage(curImageLoc, result.unifiedName, newCarImage);
					});
					newCarImageDir.find(".carousel-caption").append(useButton);

					carouselInner.append(newCarImageDir)
				});

				imagesSection.append(carousel);
				resultMainBody.append(imagesSection);
			}/* */

			if(result.attributes){
				let attsSection = $('<li class="list-group-item extProdResultSection"><h6 class="card-title">Attributes:</h6></li>');

				let attsList = $('<span></span>');
				Object.keys(result.attributes).forEach(key => {
					let val = result.attributes[key];

					let curAtt = getAttDisplay(key, val);
					let useButt = ExtItemSearch.getUseButton();

					useButt.on("click", function (e){
						addAttInput(
							addEditAttDiv,
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
		});

		for (const [service, error] of Object.entries(results.serviceErrs)) {
			addMessageToDiv(ExtItemSearch.extItemSearchSearchFormMessages, "danger", error, "Failed calling " + service);
		}
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

	doRestCall({
		url: "/api/v1/externalItemLookup/webpage/scrape/" + encodeURIComponent(webpage),
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data)},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});

ExtItemSearch.prodBarcodeSearchForm.submit(function (event) {
	event.preventDefault();
	let barcodeText = ExtItemSearch.prodBarcodeSearchBarcodeInput.val();
	console.log("Searching for a barcode: " + barcodeText);
	addEditItemBarcodeInput.val(barcodeText);
	ExtItemSearch.extSearchResults.html("");

	doRestCall({
		url: "/api/v1/externalItemLookup/product/barcode/" + barcodeText,
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data)},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});

ExtItemSearch.legoPartNumSearchForm.submit(function (event) {
	event.preventDefault();
	let partNumber = ExtItemSearch.legoPartNumSearchInput.val();
	console.log("Searching for a lego part: " + partNumber);
	ExtItemSearch.extSearchResults.html("");

	doRestCall({
		url: "/api/v1/externalItemLookup/lego/part/" + partNumber,
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data)},
		failMessagesDiv: ExtItemSearch.extItemSearchSearchFormMessages
	});
});