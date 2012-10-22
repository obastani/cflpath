package org.cflpath.cfl;

import java.util.ArrayList;
import java.util.List;

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
	
	public CFL getCFL() {
		return this.cfl;
	}
	
	@Override
	public String toString() {
		return this.cfl.toString();
	}
}
