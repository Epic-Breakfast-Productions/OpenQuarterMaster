

//TODO:: rework when we feel like actually doing something with capacities

// function addCapacityInput(container, value, selectedUnit){
//     console.log("Adding capacity input.");
//     var newInputDiv = $('<div class="input-group mb-1"> \
//   <input type="number" class="form-control capacityInput" placeholder="Capacity" name="capacity" min="0" required value="'+value+'"> \
//   <select class="form-select unitSelect dselect-select" name="unit" required> \
//   </select> \
//   <button type="button" class="input-group-text" onclick="KeywordAttEdit.keywordsAttsInputRem(this);" title="Remove Capacity">'+Icons.remove +'</button> \
// </div>');
//
//     newInputDiv.find(".unitSelect").append(UnitUtils.getUnitOptions(selectedUnit));
//     newInputDiv.find(":input.capacityInput").val(value);
//     newInputDiv.find(".unitSelect").val(selectedUnit);
//     container.append(
//         newInputDiv
//     );
//     // Dselect.setupDselect($(newInputDiv.find(".unitSelect")[0])); //TODO:: why this no work?
//
//     return newInputDiv;
// }
//
// function addCapacityInputs(container, capacities){
//     capacities.forEach(function(curCap){
//         addCapacityInput(container, curCap.value, curCap.unit);
//     });
// }