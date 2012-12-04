package org.cflpath.utility;

import java.util.ArrayList;
import java.util.HashSet;

public class CFL {
	public static class Element {
		private String name;
		
		public Element(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
		
		@Override
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
	
	public static class EmptyProduction {
		private Element target;
		
		public EmptyProduction(Element target) {
			this.target = target;
		}
		
		public Element getTarget() {
			return this.target;
		}
		
		@Override
		public int hashCode() {
			return this.target.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				EmptyProduction emptyProduction = (EmptyProduction)object;
				return this.target.equals(emptyProduction.getTarget());
			}
		}
	}
	
	public static class SingleProduction {
		private Element input;
		private Element target;
		
		public SingleProduction(Element input, Element target) {
			this.input = input;
			this.target = target;
		}
		
		public Element getInput() {
			return this.input;
		}
		
		public Element getTarget() {
			return this.target;
		}
		
		public boolean matches(Element input) {
			return this.input.equals(input);
		}
		
		@Override
		public int hashCode() {
			return 17*this.input.hashCode() + this.target.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				SingleProduction singleProduction = (SingleProduction)object;
				return this.input.equals(singleProduction.getInput())
						&& this.target.equals(singleProduction.getTarget());
			}
		}
	}
	
	public static class PairProduction {
		private Element firstInput;
		private Element secondInput;
		
		private Element target;
		
		public PairProduction(Element firstInput, Element secondInput, Element target) {
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.target = target;
		}
		
		public Element getFirstInput() {
			return this.firstInput;
		}
		
		public Element getSecondInput() {
			return this.secondInput;
		}
		
		public Element getTarget() {
			return this.target;
		}
		
		public boolean matches(Element firstInput, Element secondInput) {
			return this.firstInput.equals(firstInput) && this.secondInput.equals(secondInput);
		}
		
		@Override
		public int hashCode() {
			return 17*(31*this.firstInput.hashCode() + this.secondInput.hashCode()) + this.target.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				PairProduction pairProduction = (PairProduction)object;
				return this.firstInput.equals(pairProduction.getFirstInput())
						&& this.secondInput.equals(pairProduction.getSecondInput())
						&& this.target.equals(pairProduction.getTarget());
			}
		}
	}
	
	private HashSet<EmptyProduction> emptyProductions = new HashSet<EmptyProduction>();
	private HashSet<SingleProduction> singleProductions = new HashSet<SingleProduction>();
	private HashSet<PairProduction> pairProductions = new HashSet<PairProduction>();
	
	public boolean addEmptyProduction(Element target) {
		return this.emptyProductions.add(new EmptyProduction(target));
	}
	
	public boolean addSingleProduction(Element input, Element target) {
		return this.singleProductions.add(new SingleProduction(input, target));
	}
	
	public boolean addPairProduction(Element firstInput, Element secondInput, Element target) {
		return this.pairProductions.add(new PairProduction(firstInput, secondInput, target));
	}
	
	public HashSet<EmptyProduction> getEmptyProductions() {
		return this.emptyProductions;
	}
	
	public HashSet<SingleProduction> getSingleProductions() {
		return this.singleProductions;
	}
	
	public HashSet<PairProduction> getPairProductions() {
		return this.pairProductions;
	}
	
	public ArrayList<Element> getEmptyTargets() {
		ArrayList<Element> targets = new ArrayList<Element>();
		for(EmptyProduction emptyProduction : this.emptyProductions) {
			targets.add(emptyProduction.getTarget());
		}
		return targets;
	}
	
	public ArrayList<Element> getSingleTargets(Element input) {
		ArrayList<Element> targets = new ArrayList<Element>();
		for(SingleProduction singleProduction : this.singleProductions) {
			if(singleProduction.matches(input)) {
				targets.add(singleProduction.getTarget());
			}
		}
		return targets;
	}
	
	public ArrayList<Element> getPairTargets(Element firstInput, Element secondInput) {
		ArrayList<Element> targets = new ArrayList<Element>();
		for(PairProduction pairProduction : this.pairProductions) {
			if(pairProduction.matches(firstInput, secondInput)) {
				targets.add(pairProduction.getTarget());
			}
		}
		return targets;
	}
}
