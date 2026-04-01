
export class KeywordAttEdit {
	static addKeywordInput(container, keyword) {
		console.log("Adding keyword input.");
		let newInputDiv = $('<div class="input-group mb-1"> \
  <input type="text" class="form-control keywordInput" placeholder="Keyword" name="keyword" required> \
  <button type="button" class="input-group-text" onclick="KeywordAttEdit.keywordsAttsInputRem(this);">' + Icons.remove + '</button> \
</div>');
		container.append(
			newInputDiv
		);
		newInputDiv.find(":input").val(keyword);
		return newInputDiv;
	}
	static addKeywordInputs(container, keywords) {
		if (keywords.length > 0) {
			console.log("had keywords");
			keywords.forEach(function (keyword) {
				console.log("Keyword: " + keyword);
				KeywordAttEdit.addKeywordInput(container, keyword);
			});
		}
	}
	static addAttInput(container, key, val) {
		console.log("Adding attribute input.");
		let newInputDiv = $('<div class="input-group mb-1"> \
  <input type="text" class="form-control attInputKey" placeholder="key" name="attributeKey" required> \
  <input type="text" class="form-control attInputValue" placeholder="value" name="attributeValue"> \
  <button type="button" class="input-group-text" onclick="KeywordAttEdit.keywordsAttsInputRem(this);">' + Icons.remove + '</button> \
</div>');
		container.append(
			newInputDiv
		);
		newInputDiv.find(".attInputKey").val(key);
		newInputDiv.find(".attInputValue").val(val);
		return newInputDiv;
	}
	static addAttInputs(container, atts) {
		if (Object.keys(atts).length > 0) {
			console.log("had attributes");

			Object.entries(atts).forEach(entry => {
				const [key, val] = entry;
				console.log("Att: " + key + "/" + val);
				KeywordAttEdit.addAttInput(container, key, val);
			});
		}
	}
	static keywordsAttsInputRem(target) {
		console.log("Removing keyword or att input row");
		target.parentElement.remove();
	}
	static addKeywordData(data, container) {
		data.keywords = [];
		container.find(".keywordInput").each(function (i, keywordInput) {
			data.keywords.push(keywordInput.value);
		});
	}
	static addAttData(data, container) {
		data.attributes = {};
		let attValArr = container.find(".attInputValue");
		container.find(".attInputKey").each(function (i, attKeyInput) {
			data.attributes[attKeyInput.value] = attValArr[i].value;
		});
	}
	static addKeywordAttData(data, keywordContainer, attContainer) {
		KeywordAttEdit.addKeywordData(data, keywordContainer);
		KeywordAttEdit.addAttData(data, attContainer)
	}
}
