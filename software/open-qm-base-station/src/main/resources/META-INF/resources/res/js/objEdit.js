
function addKeywordInput(container, keyword){
    console.log("Adding keyword input.");
    var newInputDiv = $('<div class="input-group mb-1"> \
  <input type="text" class="form-control keywordInput" placeholder="Keyword" name="keyword" required> \
  <button type="button" class="input-group-text" onclick="keywordsAttsInputRem(this);"><i class="fas fa-trash"></i></button> \
</div>');
    container.append(
        newInputDiv
    );
    newInputDiv.find(":input").val(keyword);
    return newInputDiv;
}

function addKeywordInputs(container, keywords){
    if(keywords.length > 0){
        console.log("had keywords");
        keywords.forEach(function(keyword){
            console.log("Keyword: " + keyword);
            addKeywordInput(container, keyword);
        });
    }
}


function addAttInput(container, key, val){
    console.log("Adding attribute input.");
    var newInputDiv = $('<div class="input-group mb-1"> \
  <input type="text" class="form-control attInputKey" placeholder="key" required> \
  <input type="text" class="form-control attInputValue" placeholder="value"> \
  <button type="button" class="input-group-text" onclick="keywordsAttsInputRem(this);"><i class="fas fa-trash"></i></button> \
</div>');
    container.append(
        newInputDiv
    );
    newInputDiv.find(".attInputKey").val(key);
    newInputDiv.find(".attInputValue").val(val);
    return newInputDiv;
}

function addAttInputs(container, atts){
    if(Object.keys(atts).length > 0){
        console.log("had attributes");

        Object.entries(atts).forEach(entry => {
            const [key, val] = entry;
            console.log("Att: " + key + "/" + val);
            addAttInput(container, key, val);
        });
    }
}

function keywordsAttsInputRem(target){
    console.log("Removing keyword or att input row");
    target.parentElement.remove();
}

function addKeywordData(data, container){
    data.keywords = [];
    container.find(".keywordInput").each(function(i, keywordInput){
        data.keywords.push(keywordInput.value);
    });
}
function addAttData(data, container){
    data.attributes = {};
    var attValArr = container.find(".attInputValue");
    container.find(".attInputKey").each(function(i, attKeyInput){
        data.attributes[attKeyInput.value] = attValArr[i].value;
    });
}

function addKeywordAttData(data, keywordContainer, attContainer){
    addKeywordData(data, keywordContainer);
    addAttData(data, attContainer)
}