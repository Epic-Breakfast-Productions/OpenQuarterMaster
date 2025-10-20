package tech.ebp.oqm.lib.core.api.java.search;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public abstract class QParamVal<T> {
	public static StringParamVal of(String str){
		return new StringParamVal(str);
	}
	
	public static ListParamVal of(String... str){
		return new ListParamVal(Arrays.stream(str).toList());
	}
	
	protected T value;
	
	public T get(){
		return value;
	}
}
