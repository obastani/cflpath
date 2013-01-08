package org.cflpath.utility;

public class Utility {
	public static class Pair<X,Y> {
		private X x;
		private Y y;
		
		public Pair(X x, Y y) {
			this.x = x;
			this.y = y;
		}
		
		public X getX() {
			return this.x;
		}
		
		public Y getY() {
			return this.y;
		}
		
		@Override
		public int hashCode() {
			return this.x.hashCode() + this.y.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				@SuppressWarnings("rawtypes")
				Pair pair = (Pair)object;
				return this.x.equals(pair.getX()) && this.y.equals(pair.getY());
			}		
		}
	}
}
