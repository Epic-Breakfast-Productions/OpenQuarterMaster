

function addCapacityInput(container, value, selectedUnit){
    console.log("Adding capacity input.");
    var newInputDiv = $('<div class="input-group mb-1"> \
  <input type="number" class="form-control capacityInput" placeholder="Capacity" name="capacity" min="0" required value=""> \
  <select class="form-select unitSelect" required> \
  </select> \
  <button type="button" class="input-group-text" onclick="keywordsAttsInputRem(this);"><i class="fas fa-trash"></i></button> \
</div>');

    newInputDiv.find(".unitSelect").append(getUnitOptions(selectedUnit));
    newInputDiv.find(":input").val(value);
    container.append(
        newInputDiv
    );
    return newInputDiv;
}