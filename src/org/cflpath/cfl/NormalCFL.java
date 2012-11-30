package org.cflpath.cfl;

import java.util.ArrayList;
import java.util.List;

import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;

public class NormalCFL {

	private CFL cfl = new CFL();
	private List<SingleProduction> singleProductions = new ArrayList<SingleProduction>();
	private List<PairProduction> pairProductions = new ArrayList<PairProduction>();
	
	public void add(SingleProduction singleProduction) {
		this.cfl.add(singleProduction);
		this.singleProductions.add(singleProduction); 
	}
	
	public List<SingleProduction> getSingleProductions() {
		return this.singleProductions;
	}
	
	public void add(PairProduction pairProduction) {
		this.cfl.add(pairProduction);
		this.pairProductions.add(pairProduction);
	}
	
	public List<PairProduction> getPairProductions() {
		return this.pairProductions;
	}
	
	// return a list of binary productions equivalent to the given production
	// assume the production isn't empty!
	public void add(Production production) {
		switch(production.getInputs().size()) {
		case 0:
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
	
	     	String targetString = production.getTarget() + "->" + firstInput.getName() + "^" + secondInput.getName();
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
