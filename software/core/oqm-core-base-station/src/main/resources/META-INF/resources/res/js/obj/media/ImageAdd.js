import {Rest} from "../../Rest.js";
import "../../../../lib/Croppie/2.6.4/croppie.min.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class ImageAdd extends PageUtility {

	static Croppie = class {
		static defaultCroppieImage = Rest.webroot + "/media/logoSymbolSquare.svg";

		static init(croppieDivJq) {
			croppieDivJq.croppie({
				enableExif: true,
				viewport: {
					width: 250,
					height: 250,
					type: 'square'
				},
				boundary: {
					width: 300,
					height: 300
				}
			});
		}

		static getCroppieSlider(croppieDivJq) {
			return croppieDivJq.find(":input.cr-slider")
		}

		static bind(croppieDivJq, bindVal) {
			console.log("Binding croppie to image: ", bindVal);
			return croppieDivJq.croppie('bind', {
				url: bindVal
			}).then(function () {
				let croppieSlider = ImageAdd.Croppie.getCroppieSlider(croppieDivJq);
				let minVal = croppieSlider.attr('min');
				minVal = minVal === undefined ? 0 : minVal;
				croppieDivJq.croppie(
					'setZoom',
					croppieSlider.attr('min')
				);
			});
		}

		static reset(croppieDivJq) {
			console.log("Resetting croppie.");
			ImageAdd.Croppie.bind(croppieDivJq, ImageAdd.Croppie.defaultCroppieImage);
		}
	}

	static Input = class {
		static getUpload(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInput");
		}

		static getUploadControls(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInputControls");
		}

		static getControlsAdjustSwitch(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInputControlsAdjustSwitch");
		}

		static getControlsInputsContainer(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInputControlsInputsContainer");
		}

		static getCroppieDiv(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadCroppieDiv");
		}

		static getPreviewContainer(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInputPreviewContainer");
		}

		static getPreviewImage(imageInputContainerJq) {
			return imageInputContainerJq.find(".imageUploadInputPreview");
		}

		static getInputFromInner(innerElementJq) {
			return innerElementJq.closest(".imageUploadInputContainer");
		}

		static updateInputControls(adjustmentSwitch) {
			console.debug("Updating image upload adjustment control visibility.")
			if (!adjustmentSwitch.jquery) {
				adjustmentSwitch = $(adjustmentSwitch);
			}

			let input = ImageAdd.Input.getInputFromInner(adjustmentSwitch);
			let checked = adjustmentSwitch.prop("checked");

			if (checked) {
				ImageAdd.Input.getControlsInputsContainer(input).show();
			} else {
				ImageAdd.Input.getControlsInputsContainer(input).hide();
			}
		}

		static getFileType(imageInputContainerJq) {
			return ImageAdd.Input.getUpload(imageInputContainerJq)[0].files[0].type;
		}

		static getFileName(imageInputContainerJq) {
			return ImageAdd.Input.getUpload(imageInputContainerJq)[0].files[0].name;
		}
	}


	static reset(imageInputContainerJq) {
		ImageAdd.Input.getUpload(imageInputContainerJq).val(null);
		ImageAdd.Input.getUploadControls(imageInputContainerJq).hide();
		ImageAdd.Input.getPreviewContainer(imageInputContainerJq).hide();
		ImageAdd.Input.getPreviewImage(imageInputContainerJq).attr('src', "");
		let controlsAdjustmentSwitch = ImageAdd.Input.getControlsAdjustSwitch(imageInputContainerJq);
		controlsAdjustmentSwitch.prop('checked', true);
		ImageAdd.Input.updateInputControls(controlsAdjustmentSwitch);
		ImageAdd.Croppie.reset(ImageAdd.Input.getCroppieDiv(imageInputContainerJq));
	}

	static #decodeDataUri(dataUri) {
		const base64 = dataUri.split(',')[1];

		// base64 → binary string → proper UTF-8 decode
		const bin  = atob(base64);
		const bytes = Uint8Array.from(bin, c => c.charCodeAt(0));
		return new TextDecoder('utf-8').decode(bytes);
	}

	static getImageData(imageInputContainerJq, e) {
		let fileName = ImageAdd.Input.getFileType(imageInputContainerJq);

		if (fileName === "image/svg+xml") {
			let base64data = ImageAdd.Input.getPreviewImage(imageInputContainerJq).attr("src");

			return Promise.resolve(
				ImageAdd.#decodeDataUri(base64data)
			)
		} else {
			return ImageAdd.Input.getCroppieDiv(imageInputContainerJq).croppie('result', {
				type: 'blob',
				size: 'original'
			});
		}
	}

	static {
		window.ImageAdd = this;

		$(".imageUploadInputContainer").each(function (i, imageInputContainer) {
			let imageInputContainerJq = $(imageInputContainer);
			let croppieDiv = ImageAdd.Input.getCroppieDiv(imageInputContainerJq);
			let croppieDivJq = $(croppieDiv);

			$(imageInputContainerJq.find(".imageUploadInput")).on('change', function (e) {
				console.log("Got new image.");
				let fileType = e.target.files[0].type;

				let reader = new FileReader();
				reader.onload = function (e) {
					let base64ImageData = e.target.result;
					if (fileType === "image/svg+xml") {
						console.log("SVG Uploaded. Not using croppie.");
						ImageAdd.Input.getUploadControls(imageInputContainerJq).hide();
						ImageAdd.Input.getPreviewContainer(imageInputContainerJq).show();
						ImageAdd.Input.getPreviewImage(imageInputContainerJq).attr("src", base64ImageData)
					} else {
						ImageAdd.Input.getUploadControls(imageInputContainerJq).show();
						ImageAdd.Input.getPreviewContainer(imageInputContainerJq).hide();

						ImageAdd.Croppie.bind(
							croppieDivJq,
							base64ImageData
						).then(function () {
							console.log('Loaded image selected by user.');
						});
					}
				}
				reader.readAsDataURL(this.files[0]);
			});
			ImageAdd.Croppie.init(croppieDivJq);

			// console.log("Created croppie instance:");
		});
	}
}
