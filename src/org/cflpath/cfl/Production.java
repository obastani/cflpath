package org.cflpath.cfl;

import java.util.ArrayList;

import org.cflpath.cfl.Element.Variable;

public abstract class Production {
	public static class GeneralProduction extends Production {
		private ArrayList<Element> inputs = new ArrayList<Element>();
		
		public GeneralProduction(Variable target, ArrayList<Element> elements) {
			super(target);
			for(Element element : elements) {
				this.inputs.add(element);
			}
		}
		
		public GeneralProduction(Variable target, Element... elements) {
			super(target);
			for(Element element : elements) {
				this.inputs.add(element);
			}
		}
		
		@Override
		public ArrayList<Element> getInputs() {
			return this.inputs;
		}
		
		@Override
		public boolean matches(ArrayList<Element> inputs) {
			if(this.inputs.size() != inputs.size()) {
				return false;
			} else {
				for(int i=0; i<this.inputs.size(); i++) {
					if(this.inputs.get(i) != inputs.get(i)) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	public static class EmptyProduction extends Production {
		public EmptyProduction(Variable target) {
			super(target);
		}

		@Override
		public ArrayList<Element> getInputs() {
			return new ArrayList<Element>();
		}

		@Override
		public boolean matches(ArrayList<Element> inputs) {
			return inputs.size() == 0;
		}
	}

	public static class SingleProduction extends Production {
		private Element input;
		
		public SingleProduction(Variable target, Element input) {
			super(target);
			this.input = input;
		}
		
		public Element getInput() {
			return this.input;
		}
		
		public boolean matches(Element input) {
			return this.input.equals(input);
		}
		
		@Override
		public ArrayList<Element> getInputs() {
			ArrayList<Element> inputs = new ArrayList<Element>();
			inputs.add(this.input);
			return inputs;
		}
		
		@Override
		public boolean matches(ArrayList<Element> inputs) {
			return inputs.size() == 1 && this.input.equals(inputs.get(0));
		}
	}

	public static class PairProduction extends Production {
		private Element firstInput;
		private Element secondInput;
		
		public PairProduction(Variable target, Element firstInput, Element secondInput) {
			super(target);
			this.firstInput = firstInput;
			this.secondInput = secondInput;
		}
		
		public Element getFirstInput() {
			return this.firstInput;
		}
		
		public Element getSecondInput() {
			return this.secondInput;
		}
		
		public boolean matches(Element firstInput, Element secondInput) {
			return this.firstInput.equals(firstInput) && this.secondInput.equals(secondInput);
		}
		
		@Override
		public ArrayList<Element> getInputs() {
			ArrayList<Element> inputs = new ArrayList<Element>();
			inputs.add(this.firstInput);
			inputs.add(this.secondInput);
			return inputs;
		}
		
		@Override
		public boolean matches(ArrayList<Element> inputs) {
			return inputs.size() == 2 && this.firstInput.equals(inputs.get(0)) && this.secondInput.equals(inputs.get(1));
		}
	}

	private Variable target;
	
	public Production(Variable target) {
		this.target = target;
	}
	
	public Variable getTarget() {
		return this.target;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.target.toString()+" -> (");
		int hasInput = 0;
		for(Element input : this.getInputs()) {
			result.append(input.toString() + ",");
			hasInput = 1;
		}
		return result.substring(0, result.length()-hasInput) + ")";
	}
	
	@Override
	public int hashCode() {
		int hashCode = this.target.hashCode();
		for(Element input : this.getInputs()) {
			hashCode += input.hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		} else if(object == null || this.getClass() != object.getClass()) {
			return false;
		} else {
			Production production = (Production)object;
			if(this.getInputs().size() != production.getInputs().size()) {
				return false;
			}
			boolean equals = this.target.equals(production.getTarget());
			for(int i=0; i<this.getInputs().size(); i++) {
				equals &= this.getInputs().get(i).equals(production.getInputs().get(i));
			}
			return equals;
		}		
	}
	
	public abstract ArrayList<Element> getInputs();
	public abstract boolean matches(ArrayList<Element> inputs);
}

