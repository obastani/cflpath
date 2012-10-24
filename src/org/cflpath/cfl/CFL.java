package org.cflpath.cfl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.utility.MultivalueMap;

public class CFL {

	// map from variables to a list of productions that produce them
	private MultivalueMap<Variable,Production> targetProductionLists = new MultivalueMap<Variable,Production>();

	// map from inputs to a list of productions to which they are an input
	private MultivalueMap<Element,Production> inputProductionLists = new MultivalueMap<Element,Production>();
	
	// the set of productions
	private Set<Production> productions = new HashSet<Production>();
	
	// the set of terminals
	private HashSet<Terminal> terminals = new HashSet<Terminal>();
	
	public void add(Production production) {
		// add the production to the target production list
		this.targetProductionLists.add(production.getTarget(), production);
		
		// add the production to the input production list
		for(Element input : production.getInputs()) {
			this.inputProductionLists.add(input, production);
		}
		
		// add the production to the set
		this.productions.add(production);
		
		// add the terminals to the set
		for(Element element : production.getInputs()) {
			if(element.isTerminal()) {
				terminals.add((Terminal)element);
			}
		}
	}
	
	public Set<Variable> getVariables() {
		return this.targetProductionLists.keySet();
	}
	
	public Set<Terminal> getTerminals() {
		return this.terminals;
	}
	
	public HashSet<Element> getTargets(ArrayList<Element> inputs) {
		HashSet<Element> targets = new HashSet<Element>();
		for(Production production : this.productions) {
			if(production.matches(inputs)) {
				targets.add(production.getTarget());
			}
		}
		return targets;
	}
	
	public static class Path {
		private List<Terminal> terminals = new LinkedList<Terminal>();
		private double weight;
		
		public Path() {
			this.weight = 0.0;
		}
		
		public Path(Terminal ... terminals) {
			this.weight = 0.0;
			for(Terminal terminal : terminals) {
				this.add(terminal);
			}
		}
		
		public Path(Path ... paths) {
			this.weight = 0.0;
			for(Path path : paths) {
				this.add(path);
			}
		}
		
		public void add(Terminal terminal) {
			this.terminals.add(terminal);
			this.weight += terminal.getWeight();
		}
		
		public void add(Path path) {
			this.terminals.addAll(path.getTerminals());
			this.weight += path.getWeight();
		}
		
		public List<Terminal> getTerminals() {
			return this.terminals;
		}
		
		public double getWeight() {
			return this.weight;
		}
	}
	
	// run Knuth's algorithms
	public HashMap<Element,Path> getShortestPaths() {
		// map from elements to weights
		HashMap<Element,Path> mu = new HashMap<Element,Path>();
		
		// map from targets to list of productions with inputs all finite
		MultivalueMap<Variable,Production> finiteProductions = new MultivalueMap<Variable,Production>();
		
		// add the set of terminals and their weights
		for(Terminal terminal : this.getTerminals()) {
			//mu.put(terminal, terminal.getWeight());
			mu.put(terminal, new Path(terminal));
		}
		
		// add the initial set of variables that have finite lengths
		for(Production production : this.productions) {
			boolean inputsFinite = true;
			for(Element element : production.getInputs()) {
				inputsFinite &= mu.get(element) != null;
			}
			if(inputsFinite) {
				finiteProductions.add(production.getTarget(), production);
			}
		}
		
		// loop through each of the variables
		while(true) {
			// a list of the minimum length path for each element in mu that maps to null
			Map<Variable,Path> nu = new HashMap<Variable,Path>();
			// loop over all productions with all finite inputs that are not yet in mu
			for(Map.Entry<Variable,List<Production>> targetProductionList : finiteProductions.entrySet()) {
				if(mu.get(targetProductionList.getKey()) == null) {
					// compute the minimum length production ad add to nu
					Path minPath = null;
					for(Production production : targetProductionList.getValue()) {
						Path path = new Path();
						for(Element input : production.getInputs()) {
							// TODO: optimize this so it links the lists directly
							path.add(mu.get(input));
						}
						if(minPath == null || path.getWeight() < minPath.getWeight()) {
							minPath = path;
						}
					}
					nu.put(targetProductionList.getKey(), minPath);
				}
			}
			
			/*
			for(Map.Entry<Element,Double> entry : nu.entrySet()) {
				System.out.println(entry.getKey().getName() + " " + entry.getValue());
			}
			System.out.println();
			*/
			
			// get the minimum length entry in nu
			Map.Entry<Variable,Path> minNuEntry = null;
			for(Map.Entry<Variable,Path> entry : nu.entrySet()) {
				if(minNuEntry == null || entry.getValue().getWeight() < minNuEntry.getValue().getWeight()) {
					minNuEntry = entry;
				}
			}
			
			// terminate if there are no entries in nu
			if(minNuEntry == null) {
				return mu;
			}
			
			// add the minimum length entry to mu
			mu.put(minNuEntry.getKey(), minNuEntry.getValue());
			
			// for each production for which the new element is an input
			for(Production production : this.inputProductionLists.get(minNuEntry.getKey())) {
				// check if the production has all inputs
				boolean inputsFinite = true;
				for(Element element : production.getInputs()) {
					inputsFinite &= mu.get(element) != null;
				}
				// if so, add it to the list of finite productions
				if(inputsFinite) {
					finiteProductions.add(production.getTarget(), production);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Production production : this.productions) {
			result.append(production.toString()+"\n");
		}
		return result.toString();
	}
}
