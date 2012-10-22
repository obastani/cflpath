package org.cflpath.cfl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;

public class Temp extends HashSet<Production> {
	private static final long serialVersionUID = 1L;

	public HashSet<Variable> variables() {
		HashSet<Variable> variables = new HashSet<Variable>();
		for(Production production : this) {
			variables.add(production.getTarget());
		}
		return variables;
	}
	
	public HashSet<Terminal> terminals() {
		HashSet<Terminal> terminals = new HashSet<Terminal>();
		for(Production production : this) {
			for(Element element : production.getInputs()) {
				if(element.isTerminal()) {
					terminals.add((Terminal)element);
				}
			}
		}
		return terminals;
	}
	
	public HashSet<Element> getTargets(ArrayList<Element> inputs) {
		HashSet<Element> targets = new HashSet<Element>();
		for(Production production : this) {
			if(production.matches(inputs)) {
				targets.add(production.getTarget());
			}
		}
		return targets;
	}
	
	public HashMap<Element,Double> getShortestPaths() {
		HashMap<Element,Double> mu = new HashMap<Element,Double>();
		
		for(Terminal terminal : this.terminals()) {
			mu.put(terminal, terminal.getWeight());
		}
		
		for(int i=0; i<this.variables().size(); i++) {
			HashMap<Element,Double> nu = new HashMap<Element,Double>();
			for(Element element : mu.keySet()) {
				if(mu.get(element) == null) {
					Double minLength = null;
					for(Production production : this) {
						if(production.getTarget().equals(element)) {
							boolean inputsInD = true;
							for(Element input : production.getInputs()) {
								if(!mu.containsKey(input)) {
									inputsInD = false;
								}
							}
							if(inputsInD) {
								Double length = 0.0;
								for(Element input : production.getInputs()) {
									length += mu.get(input);
								}
								if(minLength == null || length < minLength) {
									minLength = length;
								}
							}
						}
					}
					nu.put(element, minLength);
				}
			}
			
			/*
			for(Map.Entry<Element,Double> entry : nu.entrySet()) {
				System.out.println(entry.getKey().getName() + " " + entry.getValue());
			}
			System.out.println();
			*/
			
			Double globalMinLength = null;
			Element globalMinElement = null;
			for(Map.Entry<Element,Double> entry : nu.entrySet()) {
				if(globalMinLength == null || entry.getValue() < globalMinLength) {
					globalMinLength = entry.getValue();
					globalMinElement = entry.getKey();
				}
			}
			mu.put(globalMinElement, globalMinLength);
		}
		return mu;
	}
}
