export const SelectedObjectDivUtils = {
	moveUp(objectDivJq) {
		let objectDiv = objectDivJq[0];
		if (objectDiv.previousSibling) {
			console.log("Moving object up");
			objectDiv.parentElement.insertBefore(objectDiv, objectDiv.previousSibling);
		}
	},
	moveDown(objectDivJq) {
		let objectDiv = objectDivJq[0];
		if (objectDiv.nextSibling) {
			console.log("Moving object down");
			if (objectDiv.nextSibling.nextSibling) {
				objectDiv.parentElement.insertBefore(objectDiv, objectDiv.nextSibling.nextSibling);
			} else {
				objectDiv.parentElement.appendChild(objectDiv);
			}
		}
	},
	removeSelected(objectDiv) {
		objectDiv.remove();
	}
};