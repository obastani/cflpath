package org.cflpath.cfl;

import java.util.ArrayList;
import java.util.List;

import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.Production.EmptyProduction;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;
import org.cflpath.utility.MultivalueMap;
import org.cflpath.utility.Utility.Pair;

public class NormalCFL {

	private CFL cfl = new CFL();
	private List<EmptyProduction> emptyProductions = new ArrayList<EmptyProduction>();
	private List<SingleProduction> singleProductions = new ArrayList<SingleProduction>();
	private List<PairProduction> pairProductions = new ArrayList<PairProduction>();
	
	private MultivalueMap<Element,SingleProduction> singleProductionsByInput = new MultivalueMap<Element,SingleProduction>();
	private MultivalueMap<Pair<Element,Element>,PairProduction> pairProductionsByInputs = new MultivalueMap<Pair<Element,Element>,PairProduction>();
	private MultivalueMap<Element,PairProduction> pairProductionsByFirstInput = new MultivalueMap<Element,PairProduction>();
	private MultivalueMap<Element,PairProduction> pairProductionsBySecondInput = new MultivalueMap<Element,PairProduction>();
	
	public void add(EmptyProduction emptyProduction) {
		this.cfl.add(emptyProduction);
		this.emptyProductions.add(emptyProduction);
	}
	
	public List<EmptyProduction> getEmptyProductions() {
		return this.emptyProductions;
	}
	
	public void add(SingleProduction singleProduction) {
		this.cfl.add(singleProduction);
		this.singleProductions.add(singleProduction);
		this.singleProductionsByInput.add(singleProduction.getInput(), singleProduction);
	}
	
	public List<SingleProduction> getSingleProductionsByInput(Element input) {
		return this.singleProductionsByInput.get(input);
	}
	
	public List<SingleProduction> getSingleProductions() {
		return this.singleProductions;
	}
	
	public void add(PairProduction pairProduction) {
		this.cfl.add(pairProduction);
		this.pairProductions.add(pairProduction);
		this.pairProductionsByInputs.add(new Pair<Element,Element>(pairProduction.getFirstInput(), pairProduction.getSecondInput()), pairProduction);
		this.pairProductionsByFirstInput.add(pairProduction.getFirstInput(), pairProduction);
		this.pairProductionsBySecondInput.add(pairProduction.getSecondInput(), pairProduction);
	}
	
	public List<PairProduction> getPairProductions() {
		return this.pairProductions;
	}
	
	public List<PairProduction> getPairProductionsByFirstInput(Element input) {
		return this.pairProductionsByFirstInput.get(input);
	}
	
	public List<PairProduction> getPairProductionsBySecondInput(Element input) {
		return this.pairProductionsBySecondInput.get(input);
	}
	
	// return a list of binary productions equivalent to the given production
	// assume the production isn't empty!
	public void add(Production production) {
		switch(production.getInputs().size()) {
		case 0:
			this.add(new EmptyProduction(production.getTarget()));
			break;
	    case 1:
	    	this.add(new SingleProduction(production.getTarget(), production.getInputs().get(0)));
	    	break;
	    case 2:
	    	this.add(new PairProduction(production.getTarget(), production.getInputs().get(0), production.getInputs().get(1)));
	    	break;
	    default:
	      	Element firstInput = production.getInputs().get(0);
	     	Element secondInput = production.getInputs().get(1);
	
	     	//String targetString = production.getTarget() + "->" + firstInput.getName() + "^" + secondInput.getName();
	     	String targetString = firstInput.getName() + "^" + secondInput.getName();
	     	Variable target = new Variable(targetString);
	     	this.add(new PairProduction(target, firstInput, secondInput));
	
	     	for(int i=2; i<production.getInputs().size()-1; i++) {
	     		firstInput = target;
	     		secondInput = production.getInputs().get(i);
	     		targetString += "^" + secondInput.getName();
	     		target = new Variable(targetString);
	     		this.add(new PairProduction(target, firstInput, secondInput));
	     	}
	
	     	firstInput = target;
	     	secondInput = production.getInputs().get(production.getInputs().size()-1);
	     	target = production.getTarget();
	     	this.add(new PairProduction(target, firstInput, secondInput));
	     	break;
		}
	}
	
	public CFL getCFL() {
		return this.cfl;
	}
	
	@Override
	public String toString() {
		return this.cfl.toString();
	}
}
