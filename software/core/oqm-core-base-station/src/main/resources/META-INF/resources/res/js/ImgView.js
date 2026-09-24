import {PageUtility} from "./utilClasses/PageUtility.js";
import {ModalUtils} from "./ModalUtils.js";

export class ImgView extends PageUtility {
	static #viewModal = $("#imgViewModal");
	static #viewImg = ImgView.#viewModal.find(".imgViewImg");

	static viewImage(imgClicked) {
		let imgClickedJq = $(imgClicked);
		let imgSrc = imgClickedJq.prop("src");
		console.log("Setting up image view model to view image: ", imgSrc);

		ImgView.#viewImg.prop("src", imgSrc);

		ModalUtils.setReturnModal(ImgView.#viewModal, imgClicked);
	}
	static {
		window.ImgView = this;
	}
}
