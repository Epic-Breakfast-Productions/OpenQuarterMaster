

function addCapacityInput(container, value, selectedUnit){
    console.log("Adding capacity input.");
    var newInputDiv = $('<div class="input-group mb-1"> \
  <input type="number" class="form-control capacityInput" placeholder="Capacity" name="capacity" min="0" required value="'+value+'"> \
  <select class="form-select unitSelect" name="unit" required> \
  </select> \
  <button type="button" class="input-group-text" onclick="keywordsAttsInputRem(this);"><i class="fas fa-trash"></i></button> \
</div>');

    newInputDiv.find(".unitSelect").append(getUnitOptions(selectedUnit));
    newInputDiv.find(":input.capacityInput").val(value);
    newInputDiv.find(".unitSelect").val(selectedUnit);
    container.append(
        newInputDiv
    );
    return newInputDiv;
}

function addCapacityInputs(container, capacities){
    capacities.forEach(function(curCap){
        addCapacityInput(container, curCap.value, curCap.unit);
    });
}