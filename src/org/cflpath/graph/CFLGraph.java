package org.cflpath.graph;

import java.util.HashSet;

import org.cflpath.cfl.CFL;
import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.NormalCFL;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;
import org.cflpath.graph.CFLGraph.Vertex;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = 1L;
	
	public static class Vertex {
		private HashSet<Edge> incomingEdges = new HashSet<Edge>();
		private HashSet<Edge> outgoingEdges = new HashSet<Edge>();
		
		private String name;
		
		public Vertex(String name) {
			this.name = name;
		}
		
		public void addOutgoingEdge(Edge edge) {
			this.outgoingEdges.add(edge);
		}
		
		public HashSet<Edge> getOutgoingEdges() {
			return this.outgoingEdges;
		}
		
		public void addIncomingEdge(Edge edge) {
			this.incomingEdges.add(edge);
		}
		
		public HashSet<Edge> getIncomingEdges() {
			return this.incomingEdges;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	public static class Edge {
		private Vertex source;
		private Vertex sink;
		
		private Terminal terminal;
		
		public Edge(Vertex source, Vertex sink, Terminal terminal) {
			this.source = source;
			this.sink = sink;
			this.terminal = terminal;
		}
		
		public Vertex getSource() {
			return this.source;
		}
		
		public Vertex getSink() {
			return this.sink;
		}
		
		public Terminal getTerminal() {
			return this.terminal;
		}
		
		@Override
		public String toString() {
			return "(" + source.toString() + "," + sink.toString() + "," + terminal.toString() + ")";
		}
	}
	
	private HashSet<Edge> edges = new HashSet<Edge>();
	
	public boolean addEdge(Vertex source, Vertex sink, Terminal terminal) {
		super.add(source);
		super.add(sink);
		
		Edge edge = new Edge(source, sink, terminal);

		source.addOutgoingEdge(edge);
		sink.addIncomingEdge(edge);
		
		return this.edges.add(edge);
	}
	
	public static Terminal getGraphTerminal(Terminal terminal, Vertex source, Vertex sink) {
		return new Terminal(terminal.getName() + "(" + source.getName() + "," + sink.getName() + ")", terminal.getWeight());
	}
	
	public static Variable getGraphVariable(Variable variable, Vertex source, Vertex sink) {
		return new Variable(variable.getName() + "(" + source.getName() + "," + sink.getName() + ")");
	}
	
	public CFL getGraphCFL(NormalCFL normalCfl) {
		CFL graphCfl = new CFL();
		for(SingleProduction singleProduction : normalCfl.getSingleProductions()) {
			Variable target = singleProduction.getTarget();
			if(singleProduction.getInput().isVariable()) {
				Variable input = (Variable)singleProduction.getInput();
				for(Vertex source : this) {
					for(Vertex sink : this) {
						Variable graphInput = getGraphVariable(input, source, sink);
						Variable graphTarget = getGraphVariable(target, source, sink);
						graphCfl.add(new SingleProduction(graphTarget, graphInput));
					}
				}
			} else if(singleProduction.getInput().isTerminal()){
				Terminal input = (Terminal)singleProduction.getInput();
				for(Edge edge : this.edges) {
					if(input.equals(edge.getTerminal())) {
						Variable graphTarget = getGraphVariable(target, edge.getSource(), edge.getSink());
						Terminal graphInput = getGraphTerminal(input, edge.getSource(), edge.getSink());
						graphCfl.add(new SingleProduction(graphTarget, graphInput));
						
					}
				}
			}
		}
		for(PairProduction pairProduction : normalCfl.getPairProductions()) {
			Variable target = pairProduction.getTarget();
			if(pairProduction.getFirstInput().isVariable()) {
				Variable firstInput = (Variable)pairProduction.getFirstInput();
				for(Vertex source : this) {
					for(Vertex intermediate : this) {
						Variable graphFirstInput = getGraphVariable(firstInput, source, intermediate);
						if(pairProduction.getSecondInput().isVariable()) {
							Variable secondInput = (Variable)pairProduction.getSecondInput();
							for(Vertex sink : this) {
								Variable graphTarget = getGraphVariable(target, source, sink);
								Variable graphSecondInput = getGraphVariable(secondInput, intermediate, sink);
								graphCfl.add(new PairProduction(graphTarget, graphFirstInput, graphSecondInput));
							}
						} else if(pairProduction.getSecondInput().isTerminal()) {
							Terminal secondInput = (Terminal)pairProduction.getSecondInput();
							for(Edge secondEdge : intermediate.getOutgoingEdges()) {
								Vertex sink = secondEdge.getSink();
								if(secondInput.equals(secondEdge.getTerminal())) {
									Variable graphTarget = getGraphVariable(target, source, sink);
									Terminal graphSecondInput = getGraphTerminal(secondInput, intermediate, sink);
									graphCfl.add(new PairProduction(graphTarget, graphFirstInput, graphSecondInput));
								}
							}
						}
					}
				}	
			} else if(pairProduction.getFirstInput().isTerminal()) {
				Terminal firstInput = (Terminal)pairProduction.getFirstInput();
				for(Edge firstEdge : this.edges) {
					Vertex source = firstEdge.getSource();
					Vertex intermediate = firstEdge.getSink();
					if(firstInput.equals(firstEdge.getTerminal())) {
						Terminal graphFirstInput = getGraphTerminal(firstInput, source, intermediate);
						if(pairProduction.getSecondInput().isVariable()) {
							Variable secondInput = (Variable)pairProduction.getSecondInput();
							for(Vertex sink : this) {
								Variable graphTarget = getGraphVariable(target, firstEdge.getSource(), sink);
								Variable graphSecondInput = getGraphVariable(secondInput, firstEdge.getSink(), sink);
								graphCfl.add(new PairProduction(graphTarget, graphFirstInput, graphSecondInput));
							}
						} else if(pairProduction.getSecondInput().isTerminal()) {
							Terminal secondInput = (Terminal)pairProduction.getSecondInput();
							for(Edge secondEdge : firstEdge.getSink().getOutgoingEdges()) {
								Vertex sink = secondEdge.getSink();
								if(secondInput.equals(secondEdge.getTerminal())) {
									Variable graphTarget = getGraphVariable(target, source, sink);
									Terminal graphSecondInput = getGraphTerminal(secondInput, intermediate, sink);
									graphCfl.add(new PairProduction(graphTarget, graphFirstInput, graphSecondInput));
								}
							}
						}
					}
				}
			}
		}
		return graphCfl;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Edge edge : this.edges) {
			result.append(edge.toString()+"\n");
		}
		return result.toString();
	}
}