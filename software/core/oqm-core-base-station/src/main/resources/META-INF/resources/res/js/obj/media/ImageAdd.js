import {Rest} from "../../Rest.js";


export const ImageAdd = {
	defaultCroppieImage: Rest.webroot + "/media/logoSymbolSquare.svg",
	uploadCrop: $('#imageUploadCroppieDiv').croppie({
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
	}),
	getCroppieSlider(){
		return ImageAdd.uploadCrop.find(":input.cr-slider");
	},
	bindCroppie(bindVal) {
		console.log("Binding croppie to image: ", bindVal);
		return ImageAdd.uploadCrop.croppie('bind', {
			url: bindVal
		}).then(function () {
			let minVal = ImageAdd.getCroppieSlider().attr('min');
			minVal = minVal === undefined ? 0 : minVal;
			ImageAdd.uploadCrop.croppie(
				'setZoom',
				ImageAdd.getCroppieSlider().attr('min')
			);
		});
	},
	resetCroppie() {
		console.log("Resetting croppie.");
		ImageAdd.bindCroppie(ImageAdd.defaultCroppieImage);
	},
	initPage: function () {
		console.log("Created croppie instance: ", ImageAdd.uploadCrop);

		$('#imageUploadInput').on('change', function () {
			console.log("Got new image to put in croppie.");
			let reader = new FileReader();
			reader.onload = function (e) {
				ImageAdd.bindCroppie(e.target.result).then(function () {
					console.log('Loaded image selected by user.');
				});
			}
			reader.readAsDataURL(this.files[0]);
		});

		//TODO:: this fails. unsure why
		// ImageAdd.resetCroppie();
	}
};
