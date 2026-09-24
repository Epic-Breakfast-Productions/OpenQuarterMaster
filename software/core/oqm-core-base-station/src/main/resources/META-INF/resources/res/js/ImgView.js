import {PageUtility} from "./utilClasses/PageUtility.js";
import {ModalUtils} from "./ModalUtils.js";

export class ImgView extends PageUtility {
	static #viewModal = $("#imgViewModal");
	static #vireModalTitle = ImgView.#viewModal.find(".modalTitleText");
	static #viewImg = ImgView.#viewModal.find(".imgViewImg");

	static viewImage(imgButtonClicked) {
		let imgClickedJq = $(imgButtonClicked).find(".imgViewClickableImg");
		let imgSrc = imgClickedJq.prop("src");
		let imgAlt = imgClickedJq.prop("alt");

		console.log("Setting up image view model to view image: ", imgSrc);

		ImgView.#viewImg.prop("src", imgSrc);
		ImgView.#viewImg.prop("alt", imgAlt);
		ImgView.#vireModalTitle.text(imgAlt);

		ModalUtils.setReturnModal(ImgView.#viewModal, imgClicked);
	}
	static {
		window.ImgView = this;
	}
}
