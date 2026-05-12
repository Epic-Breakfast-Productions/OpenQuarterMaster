import { PageUtility } from "./utilClasses/PageUtility.js";

export class Icons extends PageUtility {
	static add = '{#icons/add /}';
	static copy = '{#icons/copy /}';
	static copyChecked = '{#icons/copyChecked /}';
	static dropdown = '{#icons/dropdown /}';
	static itemCheckin = '';
	static itemCheckout = '';
	static itemCheckouts = '{#icons/itemCheckouts /}';
	static edit = '{#icons/edit /}';
	static info = '{#icons/info /}';
	static link = '{#icons/link /}';
	static locked = '{#icons/locked /}'
	static item = '{#icons/item /}';
	static items = '{#icons/items /}';
	static pricing = '{#icons/pricing /}';
	static stored = '{#icons/stored /}';
	static newTab = '{#icons/newTab /}';
	static remove = '{#icons/remove /}';
	static unlocked = '{#icons/unlocked /}';
	static storageBlock = '{#icons/storageBlock /}';
	static storageBlocks = '{#icons/storageBlocks /}';
	static useDatapoint = '{#icons/useDatapoint /}';
	static view = '{#icons/view /}';
	static viewClose = '{#icons/viewClose /}';
	//transactions
	static transaction = '{#icons/transaction /}';
	static addTransaction = '{#icons/transactionAdd /}';
	static subtractTransaction = '{#icons/transactionSubtract /}';
	static checkoutTransaction = '{#icons/transactionCheckout /}';
	static checkinTransaction = '{#icons/transactionCheckin /}';
	static transferTransaction = '{#icons/transactionTransfer /}';
	static setTransaction = '{#icons/transactionSet /}';
	static identifiers = '{#icons/identifiers /}';
	static idGenerators = '{#icons/idGenerators /}';
	//interacting entities
	static user = '{#icons/user /}';
	static extService = '{#icons/extService /}';
	static coreApi = '{#icons/coreApi /}';
		
	static iconWithSub(icon, subIcon){
		return icon + '<sup>' + subIcon + '</sup>';
	}

	static {
		window.Icons = this;
		console.log(this.name + " done initializing.");
	}
}