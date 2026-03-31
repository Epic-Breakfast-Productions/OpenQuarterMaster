import {PageUtility} from "../utilClasses/PageUtility.js";


export class TestClass extends PageUtility {
	static num = 0;


	static {
		console.log("TestClass static block executed");
	}

	static initialized(){
		super.init();
		console.log(this.name + " initialized");
	}

	constructor() {
		super();
		console.log("Test class constructed");
		this.test = "test" + TestClass.num++;
	}

	getTest(){
		return this.test;
	}
}

export const Test = new TestClass();