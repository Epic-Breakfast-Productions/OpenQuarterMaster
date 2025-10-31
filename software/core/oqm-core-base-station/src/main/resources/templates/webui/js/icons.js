
const Icons = {
	add: '{#icons/add /}',
	copy: '{#icons/copy /}',
	copyChecked: '{#icons/copyChecked /}',
	itemCheckin: '',
	itemCheckout: '',
	itemCheckinout: '{#icons/checkinout /}',
	edit: '{#icons/edit /}',
	info: '{#icons/info /}',
	locked: '{#icons/locked /}',
	item: '{#icons/item /}',
	items: '{#icons/items /}',
	stored: '{#icons/stored /}',
	newTab: '{#icons/newTab /}',
	remove: '{#icons/remove /}',
	unlocked: '{#icons/unlocked /}',
	storageBlock: '{#icons/storageBlock /}',
	storageBlocks: '{#icons/storageBlocks /}',
	useDatapoint: '{#icons/useDatapoint /}',
	view: '{#icons/view /}',
	viewClose: '{#icons/viewClose /}',
	//transactions
	transaction: '{#icons/transaction /}',
	addTransaction: '{#icons/transactionAdd /}',
	subtractTransaction: '{#icons/transactionSubtract /}',
	checkoutTransaction: '{#icons/transactionCheckout /}',
	checkinTransaction: '{#icons/transactionCheckin /}',
	transferTransaction: '{#icons/transactionTransfer /}',
	setTransaction: '{#icons/transactionSet /}',
	idGenerators: '{#icons/idGenerators /}',

	iconWithSub(icon, subIcon){
		return icon + '<sup>' + subIcon + '</sup>';
	}
}