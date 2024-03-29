package org.cflpath.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cflpath.cfl.CFL;
import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.NormalCFL;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;

public class FlowsToGraph extends CFLGraph {
	private static final long serialVersionUID = 1L;

	private Set<String> references = new HashSet<String>();
	
	// terminals
	
	private Terminal new_terminal = new Terminal("new", 0.0);
	
	private Terminal assign = new Terminal("assign", 0.0);
	
	private Terminal store_star = new Terminal("store_*", 1.0);
	private Terminal store_(String reference) { return new Terminal("store_" + reference, 0.0); }
	
	private Terminal load_star = new Terminal("load_*", 1.0);
	private Terminal load_(String reference) { return new Terminal("load_" + reference, 0.0); }	
	
	// variables
	
	private Variable flowsTo = new Variable("flowsTo");
	private Variable flowsToBar = new Variable("flowsToBar");

	private Variable flowsToRightRight_star = new Variable("flowsToRightRight_*");
	private Variable flowsToRightRight_(String reference) { return new Variable("flowsToRightRight_" + reference); }
	private Variable flowsToRight = new Variable("flowsToRight");
	
	private Variable alias = new Variable("alias");
	
	// various functions
	
	public Terminal getAssign() {
		return this.assign;
	}
	
	public Terminal getNew() {
		return this.new_terminal;
	}
	
	public void addReference(String reference) {
		this.references.add(reference);
	}
	
	public Terminal getLoadStar() {
		return this.load_star;
	}
	
	public Terminal getLoad(String field) {
		return this.load_(field);
	}
	
	public Terminal getStoreStar() {
		return this.store_star;
	}
	
	public Terminal getStore(String field) {
		return this.store_(field);
	}
	
	public void addMethod(List<Vertex> args, Vertex ret, String methodSignature) {
		// temporary variable in the method
		Vertex temp_method = new Vertex("temp_" + methodSignature);
		super.addEdge(temp_method, temp_method, this.load_star);
		super.addEdge(temp_method, temp_method, this.store_star);
		
		// new object in the method
		Vertex obj_method = new Vertex("obj_" + methodSignature);
		super.addEdge(obj_method, temp_method, this.new_terminal);
		
		// add edges to and from args
		for(Vertex arg : args) {
			super.addEdge(arg, temp_method, this.assign);
			super.addEdge(temp_method, arg, this.assign);
		}
		super.addEdge(temp_method, ret, this.assign);
	}
	
	public NormalCFL getFlowsToCfl() {
		NormalCFL normalCfl = new NormalCFL();

		// add production flowsToRightRight_* -> alias load_*
		normalCfl.add(new PairProduction(this.flowsToRightRight_star, this.alias, this.load_star));
		
		// add production flowsToRight -> assign
		normalCfl.add(new SingleProduction(this.flowsToRight, this.assign));
		
		// add production flowsToRight -> flowsToRight flowsToRight
		normalCfl.add(new PairProduction(this.flowsToRight, this.flowsToRight, this.flowsToRight));
		
		// add production flowsToRight -> store_* flowsToRightRight_*
		normalCfl.add(new PairProduction(this.flowsToRight, this.store_star, this.flowsToRightRight_star));
		
		// add production flowsTo ->  new flowsToRight
		normalCfl.add(new PairProduction(this.flowsTo, this.new_terminal, this.flowsToRight));
		
		// add production flowsTo -> new
		normalCfl.add(new SingleProduction(this.flowsTo, this.new_terminal));

		// add production alias -> flowsToBar flowsTo
		normalCfl.add(new PairProduction(this.alias, this.flowsToBar, this.flowsTo));
		
		for(String reference : this.references) {
			// add production flowsToRightRight_f -> alias load_f
			normalCfl.add(new PairProduction(this.flowsToRightRight_(reference), this.alias, this.load_(reference)));
			
			// add production flowsToRight -> store_f flowsToRight_f
			normalCfl.add(new PairProduction(this.flowsToRight, this.store_(reference), this.flowsToRightRight_(reference)));
			
			// add production flowsToRight -> store_* flowsToRight_f
			normalCfl.add(new PairProduction(this.flowsToRight, this.store_star, this.flowsToRightRight_(reference)));
			
			// add production flowsToRight -> store_f flowsToRight_*
			normalCfl.add(new PairProduction(this.flowsToRight, this.store_(reference), this.flowsToRightRight_star));
		}
		
		return normalCfl;
	}

	public CFL getGraphCFL() {
		CFL graphCfl = super.getGraphCFL(this.getFlowsToCfl());
		// add production flowsToBar(u, v) -> flowsTo 
		for(Vertex source : this) {
			for(Vertex sink : this) {
				Variable flowsTo_sink_source = CFLGraph.getGraphVariable(this.flowsTo, sink, source);
				Variable flowsToBar_source_sink = CFLGraph.getGraphVariable(this.flowsToBar, source, sink);

				// add production flowsToBar(u,v) -> flowsTo(v,u)
				graphCfl.add(new SingleProduction(flowsToBar_source_sink, flowsTo_sink_source));
			}
		}
		return graphCfl;
	}
}
