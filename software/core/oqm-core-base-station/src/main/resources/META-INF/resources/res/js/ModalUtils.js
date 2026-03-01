
export const ModalUtils = {

	getModalCloseButton: function (modalJq){
		return modalJq.find("#" + modalJq.prop("id") + "LabelCloseButton")
	},
	clearModalReturn: function(destModalJq){
		//   data-bs-target="#exampleModalToggle" data-bs-toggle="modal"
		//  data-bs-otherModalId
		destModalJq.removeAttr("data-bs-otherModalId");
		let modalCloseButton = this.getModalCloseButton(destModalJq);
		modalCloseButton.removeAttr("data-bs-target");
		modalCloseButton.removeAttr("data-bs-toggle");
	},
	getModalOfElement(element){
		return element.closest(".modal");
	},
	/**
	 *
	 * @param destModalJq The jQuery object of the modal we are going to.
	 * @param returnModal Mixed type. The modal being returned to. If String, the tag to find using jquery. Can also be the modal element or an element within the modal both as plain js or jQuery. If Event, gets the element from the event's target. Null to reset the return modal.
	 */
	setReturnModal: function(destModalJq, returnModal = null){
		//ensure using jquery object
		if(returnModal === undefined || returnModal == null){
			this.clearModalReturn(destModalJq);
			return;
		} else if(typeof returnModal === "string" || returnModal instanceof String || returnModal instanceof Element){
			returnModal = $(returnModal);
		} else if(returnModal instanceof Event){
			returnModal = $(returnModal.target);
		}
		//get modal parent, if applicable
		returnModal = ModalUtils.getModalOfElement(returnModal);

		if(returnModal === undefined || returnModal == null || returnModal.length === 0){
			this.clearModalReturn(destModalJq);
			return;
		}

		let returnModalId = returnModal.prop("id");
		console.log("Setting modal to return to ", returnModalId, returnModal);

		destModalJq.attr("data-bs-otherModalId", returnModalId);

		let destModalCloseButton = this.getModalCloseButton(destModalJq);
		destModalCloseButton.attr("data-bs-target", "#" + returnModalId);
		destModalCloseButton.attr("data-bs-toggle", "modal");
	}
};