package org.cflpath.utility;

import java.util.HashMap;
import java.util.Map;

public class Factory<T> {
	private int curId=0;
	private Map<T,Integer> elementToId = new HashMap<T,Integer>();
	private Map<Integer,T> idToElement = new HashMap<Integer,T>(); 
	
	public int getIdByElement(T t) {
		Integer id = this.elementToId.get(t);
		if(id == null) {
			this.elementToId.put(t, this.curId);
			this.idToElement.put(this.curId, t);
			return this.curId++;
		} else {
			return id;
		}
	}
	
	public T getElementById(int id) {
		return this.idToElement.get(id);
	}
}
