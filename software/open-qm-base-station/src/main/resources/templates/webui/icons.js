
const Icons = {
	add: '{#icons/add}{/icons/add}',
	copy: '{#icons/copy}{/icons/copy}',
	copyChecked: '{#icons/copyChecked}{/icons/copyChecked}',
	itemCheckin: '{#icons/checkin}{/icons/checkin}',
	itemCheckout: '{#icons/checkout}{/icons/checkout}',
	itemCheckinout: '{#icons/checkinout}{/icons/checkinout}',
	edit: '{#icons/edit}{/icons/edit}',
	info: '{#icons/info}{/icons/info}',
	locked: '{#icons/locked}{/icons/locked}',
	item: '{#icons/item}{/icons/item}',
	items: '{#icons/items}{/icons/items}',
	remove: '{#icons/remove}{/icons/remove}',
	unlocked: '{#icons/unlocked}{/icons/unlocked}',
	storageBlock: '{#icons/storageBlock}{/icons/storageBlock}',
	storageBlocks: '{#icons/storageBlocks}{/icons/storageBlocks}',
	useDatapoint: '{#icons/useDatapoint}{/icons/useDatapoint}',

	iconWithSub(icon, subIcon){
		return icon + '<sup>' + subIcon + '</sup>';
	}
}