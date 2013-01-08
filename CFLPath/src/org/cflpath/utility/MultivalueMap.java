package org.cflpath.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultivalueMap<K,V> extends HashMap<K,List<V>> {
	private static final long serialVersionUID = 1L;

	public void add(K k, V v) {
		List<V> vList = super.get(k);
		if(vList == null) {
			super.put(k, vList = new ArrayList<V>());
		}
		vList.add(v);
	}
	
	@Override
	public List<V> get(Object k) {
		List<V> vList = super.get(k);
		return vList == null ? new ArrayList<V>() : vList;
	}
}
