package org.cflpath.cfl;

public abstract class Element {
	public static class Variable extends Element {
		public Variable(String name) {
			super(name);
		}

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public boolean isVariable() {
			return true;
		}
	}
	
	public static class Terminal extends Element {
		private double weight;
		
		public Terminal(String name, double weight) {
			super(name);
			this.weight = weight;
		}
		
		public double getWeight() {
			return this.weight;
		}

		@Override
		public boolean isTerminal() {
			return true;
		}

		@Override
		public boolean isVariable() {
			return false;
		}
		
		@Override
		public String toString() {
			return "(" + super.toString() + "," + this.weight + ")"; 
		}
	}
	
	private String name;
	
	public Element(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public abstract boolean isTerminal();
	public abstract boolean isVariable();
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		} else if(object == null || this.getClass() != object.getClass()) {
			return false;
		} else {
			Element element = (Element)object;
			return this.name.equals(element.getName());
		}
	}
}
