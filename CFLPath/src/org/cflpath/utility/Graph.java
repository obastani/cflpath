package org.cflpath.utility;

import java.util.HashSet;

public class Graph<V extends Graph.Vertex<E>, E extends Graph.Edge<V>> extends HashSet<E> {
	private static final long serialVersionUID = 1L;
	
	public static class Vertex<E> extends HashSet<E> {
		private static final long serialVersionUID = 1L;
	}
	
	public static class Edge<V> {
		private V source;
		private V sink;
		
		public Edge(V source, V sink) {
			this.source = source;
			this.sink = sink;
		}
		
		public V getSource() {
			return this.source;
		}
		
		public V getSink() {
			return this.sink;
		}
	}
}
