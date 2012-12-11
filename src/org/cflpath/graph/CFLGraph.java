package org.cflpath.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.cflpath.cfl.CFL;
import org.cflpath.cfl.Element;
import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.NormalCFL;
import org.cflpath.cfl.Production.EmptyProduction;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;
import org.cflpath.graph.CFLGraph.Vertex;
import org.cflpath.utility.MultivalueMap;
import org.cflpath.utility.Utility.Pair;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = 1L;
	
	public static class Vertex {		
		private String name;
		
		public Vertex(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
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
				Vertex vertex = (Vertex)object;
				return this.name.equals(vertex.getName());
			}
		}
	}
	
	public static class Edge {
		private Vertex source;
		private Vertex sink;
		
		private Element element;
		
		public Edge(Vertex source, Vertex sink, Element element) {
			this.source = source;
			this.sink = sink;
			this.element = element;
		}
		
		public Vertex getSource() {
			return this.source;
		}
		
		public Vertex getSink() {
			return this.sink;
		}
		
		public Element getElement() {
			return this.element;
		}
		
		@Override
		public String toString() {
			return "(" + this.source.toString() + "," + this.sink.toString() + "," + this.element.toString() + ")";
		}
		
		@Override
		public int hashCode() {
			return 37*(59*this.source.hashCode() + this.sink.hashCode()) + this.element.hashCode(); 
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				Edge edge = (Edge)object;
				return this.source.equals(edge.getSource())
						&& this.sink.equals(edge.getSink())
						&& this.element.equals(edge.getElement());
			}
		}
	}
	
	private Set<Edge> edges = new HashSet<Edge>();
	private MultivalueMap<Vertex,Edge> incomingEdges = new MultivalueMap<Vertex,Edge>();
	private MultivalueMap<Vertex,Edge> outgoingEdges = new MultivalueMap<Vertex,Edge>();
	private MultivalueMap<Pair<Vertex,Element>,Edge> incomingEdgesByElement = new MultivalueMap<Pair<Vertex,Element>,Edge>();
	private MultivalueMap<Pair<Vertex,Element>,Edge> outgoingEdgesByElement = new MultivalueMap<Pair<Vertex,Element>,Edge>();
	
	public MultivalueMap<Vertex,Edge> getIncomingEdges() {
		return this.incomingEdges;
	}
	
	public MultivalueMap<Pair<Vertex,Element>,Edge> getIncomingEdgesByElement() {
		return this.incomingEdgesByElement;
	}
	
	public MultivalueMap<Vertex,Edge> getOutgoingEdges() {
		return this.outgoingEdges;
	}
	
	public MultivalueMap<Pair<Vertex,Element>,Edge> getOutgoingEdgesByElement() {
		return this.outgoingEdgesByElement;
	}
	
	public Edge addEdge(Vertex source, Vertex sink, Element element) {
		super.add(source);
		super.add(sink);
		
		Edge edge = new Edge(source, sink, element);

		this.outgoingEdges.add(source, edge);
		this.incomingEdges.add(sink, edge);

		this.outgoingEdgesByElement.add(new Pair<Vertex,Element>(source, element), edge);
		this.incomingEdgesByElement.add(new Pair<Vertex,Element>(sink, element), edge);
		
		return this.edges.add(edge) ? edge : null;
	}
	
	public Set<Edge> getEdges() {
		return this.edges;
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
					if(input.equals(edge.getElement())) {
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
							for(Edge secondEdge : this.outgoingEdges.get(intermediate)) {
								Vertex sink = secondEdge.getSink();
								if(secondInput.equals(secondEdge.getElement())) {
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
					if(firstInput.equals(firstEdge.getElement())) {
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
							for(Edge secondEdge : this.outgoingEdges.get(firstEdge.getSink())) {
								Vertex sink = secondEdge.getSink();
								if(secondInput.equals(secondEdge.getElement())) {
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
	
	public void addProductions(NormalCFL normalCfl) {
		LinkedList<Edge> workflow = new LinkedList<Edge>();
		Edge newEdge;
		
		for(Edge edge : this.edges){
			workflow.add(edge);
		}
		
		for(EmptyProduction emptyProduction : normalCfl.getEmptyProductions()) {
			for(Vertex vertex : this) {
				if((newEdge = this.addEdge(vertex, vertex, emptyProduction.getTarget())) != null) {
					workflow.add(newEdge);
				}
			}
		}
		
		int i=0;
		while(!workflow.isEmpty()) {
			if(i%1000 == 0) {
				System.out.println(i);
				System.out.println(workflow.size());
				System.out.println();
			}
			i++;
			
			Edge currentEdge = workflow.remove();

			for(SingleProduction singleProduction : normalCfl.getSingleProductionsByInput(currentEdge.getElement())) {
				if((newEdge = this.addEdge(currentEdge.getSource(), currentEdge.getSink(), singleProduction.getTarget())) != null) {
					workflow.add(newEdge);
				}
			}
			
			for(PairProduction pairProduction : normalCfl.getPairProductionsByFirstInput(currentEdge.getElement())) {
				Set<Edge> outgoingEdges = new HashSet<Edge>(this.outgoingEdges.get(currentEdge.getSink()));
				for(Edge edge : outgoingEdges) {
					if(edge.getElement().equals(pairProduction.getSecondInput())) {
						if((newEdge = this.addEdge(currentEdge.getSource(), edge.getSink(), pairProduction.getTarget())) != null) {
							workflow.add(newEdge);
						}
					}
				}
				/*
				Edge edge = null;
				if((edge = this.outgoingEdgesByElement.get(new Pair<Vertex, Element>(currentEdge.getSink(), pairProduction.getSecondInput()))) != null) {
					if((newEdge = this.addEdge(currentEdge.getSource(), edge.getSink(), pairProduction.getTarget())) != null) {
						workflow.add(newEdge);
					}
				}
				*/
			}
			
			for(PairProduction pairProduction : normalCfl.getPairProductionsBySecondInput(currentEdge.getElement())) {
				
				Set<Edge> incomingEdges = new HashSet<Edge>(this.incomingEdges.get(currentEdge.getSource()));
				for(Edge edge : incomingEdges) {
					if(edge.getElement().equals(pairProduction.getFirstInput())) {
						if((newEdge = this.addEdge(edge.getSource(), currentEdge.getSink(), pairProduction.getTarget())) != null) {
							workflow.add(newEdge);
						}
					}
				}
				/*
				Edge edge = null;
				if((edge = this.incomingEdgesByElement.get(new Pair<Vertex,Element>(currentEdge.getSource(), pairProduction.getFirstInput()))) != null) {
					if((newEdge = this.addEdge(edge.getSource(), currentEdge.getSink(), pairProduction.getTarget())) != null) {
						workflow.add(newEdge);
					}
				}
				*/
			}
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Edge edge : this.edges) {
			result.append(edge.toString()+"\n");
		}
		return result.toString();
	}
}
