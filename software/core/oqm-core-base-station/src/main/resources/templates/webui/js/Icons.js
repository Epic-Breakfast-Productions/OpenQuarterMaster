
export const Icons = {
	add: '{#icons/add /}',
	copy: '{#icons/copy /}',
	copyChecked: '{#icons/copyChecked /}',
	dropdown: '{#icons/dropdown /}',
	itemCheckin: '',
	itemCheckout: '',
	itemCheckouts: '{#icons/itemCheckouts /}',
	edit: '{#icons/edit /}',
	info: '{#icons/info /}',
	link: '{#icons/link /}',
	locked: '{#icons/locked /}',
	item: '{#icons/item /}',
	items: '{#icons/items /}',
	pricing: '{#icons/pricing /}',
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
	identifiers: '{#icons/identifiers /}',
	idGenerators: '{#icons/idGenerators /}',
	//interacting entities
	user: '{#icons/user /}',
	extService: '{#icons/extService /}',
	coreApi: '{#icons/coreApi /}',

	iconWithSub(icon, subIcon){
		return icon + '<sup>' + subIcon + '</sup>';
	}
}