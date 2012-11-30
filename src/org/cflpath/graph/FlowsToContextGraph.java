package org.cflpath.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cflpath.cfl.CFL;
import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.NormalCFL;
import org.cflpath.cfl.Production.GeneralProduction;
import org.cflpath.cfl.Production.SingleProduction;

public class FlowsToContextGraph extends CFLGraph {
	private static final long serialVersionUID = 1L;

	private Set<String> fields = new HashSet<String>();
	private Set<String> methods = new HashSet<String>();

	// terminals for flows-to cfl
	
	private Terminal new_terminal = new Terminal("new", 0.0);
	
	private Terminal assign = new Terminal("assign", 0.0);
	
	private Terminal store_star = new Terminal("store_*", 1.0);
	private Terminal store_(String field) { return new Terminal("store_" + field, 0.0); }
	
	private Terminal load_star = new Terminal("load_*", 1.0);
	private Terminal load_(String field) { return new Terminal("load_" + field, 0.0); }	
	
	// variables for flows-to cfl
	
	private Variable flowsTo = new Variable("flowsTo");
	private Variable flowsToBar = new Variable("flowsToBar");
	private Variable flowsToRight = new Variable("flowsToRight");
	private Variable alias = new Variable("alias");
	private Variable contextTerminal = new Variable("contextTerminal");
	
	// terminals for context cfl

	private Terminal enter_(String method) { return new Terminal("enter_" + method, 0.0); }
	private Terminal exit_(String method) { return new Terminal("exit_" + method, 0.0); }
	
	// variables for context cfl

	private Variable context = new Variable("context");
	private Variable flowsToTerminal = new Variable("flowsToTerminal");
	
	// functions for adding fields and methods

	public void addField(String field) {
		this.fields.add(field);
	}
	
	public void addMethod(String method) {
		this.methods.add(method);
	}	
	
	// functions for getting flows-to terminals
	
	public Terminal getAssign() {
		return this.assign;
	}
	
	public Terminal getNew() {
		return this.new_terminal;
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
	
	// functions for getting context terminals
	
	public Terminal getEnter(String method) {
		return this.enter_(method);
	}
	
	public Terminal getExit(String method) {
		return this.exit_(method);
	}
	
	// add a stub method to the graph
	// TODO: modify to include context sensitivity
	
	public void addStubMethod(List<Vertex> args, Vertex ret, String methodSignature) {
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
	
	public NormalCFL getFlowsToCFL() {
		NormalCFL cfl = new NormalCFL();

		// add production flowsToRight -> store_* alias load_*
		cfl.add(new GeneralProduction(this.flowsToRight, this.store_star, this.alias, this.load_star));
		
		// add production flowsToRight -> assign
		cfl.add(new GeneralProduction(this.flowsToRight, this.assign));
		
		// add production flowsToRight -> flowsToRight flowsToRight
		cfl.add(new GeneralProduction(this.flowsToRight, this.flowsToRight, this.flowsToRight));

		// add production flowsTo -> new
		cfl.add(new GeneralProduction(this.flowsTo, this.new_terminal));
		
		// add production flowsTo ->  new flowsToRight
		cfl.add(new GeneralProduction(this.flowsTo, this.new_terminal, this.flowsToRight));

		// add production alias -> flowsToBar flowsTo
		cfl.add(new GeneralProduction(this.alias, this.flowsToBar, this.flowsTo));
		
		for(String field : this.fields) {
			// add production flowsToRight -> store_f alias load_f
			cfl.add(new GeneralProduction(this.flowsToRight, this.store_(field), this.alias, this.load_(field)));
			
			// add production flowsToRight -> store_* alias load_f
			cfl.add(new GeneralProduction(this.flowsToRight, this.store_star, this.alias, this.load_(field)));

			// add production flowsToRight -> store_f alias load_*
			cfl.add(new GeneralProduction(this.flowsToRight, this.store_(field), this.alias, this.load_star));
		}
		
		// ignore context terminals
		for(String method : this.methods) {
			// add production contextTerminal -> enter_m
			cfl.add(new GeneralProduction(this.contextTerminal, this.enter_(method)));
			
			// add production contextTerminal -> exit_m
			cfl.add(new GeneralProduction(this.contextTerminal, this.exit_(method)));
		}
		
		cfl.add(new GeneralProduction(this.flowsTo, this.contextTerminal, this.flowsTo));
		cfl.add(new GeneralProduction(this.flowsTo, this.flowsTo, this.contextTerminal));
		
		cfl.add(new GeneralProduction(this.flowsToRight, this.contextTerminal, this.flowsToRight));
		cfl.add(new GeneralProduction(this.flowsToRight, this.flowsToRight, this.contextTerminal));

		cfl.add(new GeneralProduction(this.alias, this.contextTerminal, this.alias));
		cfl.add(new GeneralProduction(this.alias, this.alias, this.contextTerminal));
		
		return cfl;
	}

	public CFL getFlowsToGraphCFL() {
		CFL graphCfl = super.getGraphCFL(this.getFlowsToCFL());
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
	
	public NormalCFL getContextCFL() {
		NormalCFL cfl = new NormalCFL();
		for(String method : this.methods) {
			// add production context -> enter_m exit_m
			cfl.add(new GeneralProduction(this.context, this.enter_(method), this.exit_(method)));
			
			// add production context -> enter_m context exit_m
			cfl.add(new GeneralProduction(this.context, this.enter_(method), this.context, this.exit_(method)));
			
			// add production context -> context context
			cfl.add(new GeneralProduction(this.context, this.context, this.context));
		}
		
		// ignore flows-to terminals
		cfl.add(new GeneralProduction(this.flowsToTerminal, this.new_terminal));
		cfl.add(new GeneralProduction(this.flowsToTerminal, this.assign));
		cfl.add(new GeneralProduction(this.flowsToTerminal, this.store_star));
		cfl.add(new GeneralProduction(this.flowsToTerminal, this.load_star));
		
		for(String field : this.fields) {
			cfl.add(new GeneralProduction(this.flowsToTerminal, this.store_(field)));
			cfl.add(new GeneralProduction(this.flowsToTerminal, this.load_(field)));
		}
		
		cfl.add(new GeneralProduction(this.context, this.flowsToTerminal, this.context));
		cfl.add(new GeneralProduction(this.context, this.context, this.flowsToTerminal));
		
		return cfl;
	}

	public CFL getContextGraphCFL() {
		return super.getGraphCFL(this.getContextCFL());
	}
}
