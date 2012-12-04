package org.cflpath.utility;

import java.util.HashSet;
import java.util.LinkedList;

import org.cflpath.utility.CFL.Element;
import org.cflpath.utility.CFL.EmptyProduction;
import org.cflpath.utility.CFL.PairProduction;
import org.cflpath.utility.CFL.SingleProduction;
import org.cflpath.utility.CFLGraph.Vertex;

public class CFLGraph extends HashSet<Vertex> {
	private static final long serialVersionUID = 1L;
	
	public static class Vertex {
		private HashSet<Edge> incomingEdges = new HashSet<Edge>();
		private HashSet<Edge> outgoingEdges = new HashSet<Edge>();
		
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
	}
	
	public abstract static class Edge {
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
		public int hashCode() {
			return 17*(31*this.source.hashCode() + this.sink.hashCode()) + this.element.hashCode();
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
		
		public abstract int getLength();
	}
	
	public class BaseEdge extends Edge {
		private int length;
		
		public BaseEdge(Vertex source, Vertex sink, Element element, int length) {
			super(source, sink, element);
			this.length = length;
		}

		@Override
		public int getLength() {
			return this.length;
		}
	}
	
	public class SingleProductionEdge extends Edge {
		private Edge edge;
		
		public SingleProductionEdge(Vertex source, Vertex sink, Element element, Edge edge) {
			super(source, sink, element);
			this.edge = edge;
		}
		
		public Edge getEdge() {
			return this.edge;
		}
		
		@Override
		public int getLength() {
			return this.edge.getLength();
		}
		
		@Override
		public int hashCode() {
			return 41*super.hashCode() + this.edge.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				SingleProductionEdge singleProductionEdge = (SingleProductionEdge)object;
				return super.equals(object)	&& this.edge.equals(singleProductionEdge.getEdge());
			}
		}
	}
	
	public class PairProductionEdge extends Edge {
		private Edge firstEdge;
		private Edge secondEdge;
		
		public PairProductionEdge(Vertex source, Vertex sink, Element element, Edge firstEdge, Edge secondEdge) {
			super(source, sink, element);
			this.firstEdge = firstEdge;
			this.secondEdge = secondEdge;
		}
		
		public Edge getFirstEdge() {
			return this.firstEdge;
		}
		
		public Edge getSecondEdge() {
			return this.secondEdge;
		}
		
		@Override
		public int getLength() {
			return this.firstEdge.getLength() + this.secondEdge.getLength();
		}
		
		@Override
		public int hashCode() {
			return 53*(41*super.hashCode() + this.firstEdge.hashCode()) + this.secondEdge.hashCode();
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			} else if(object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				PairProductionEdge pairProductionEdge = (PairProductionEdge)object;
				return super.equals(object)
						&& this.firstEdge.equals(pairProductionEdge.getFirstEdge())
						&& this.secondEdge.equals(pairProductionEdge.getSecondEdge());
			}
		}
	}
	
	private HashSet<Edge> edges = new HashSet<Edge>();
	
	public boolean addEdge(Edge edge) {
		super.add(edge.getSource());
		super.add(edge.getSink());
		edge.getSource().addOutgoingEdge(edge);
		edge.getSink().addIncomingEdge(edge);
		return this.edges.add(edge);
	}
	
	public void addProductions(CFL cfl) {
		LinkedList<Edge> workflow = new LinkedList<Edge>();
		for(Edge edge : this.edges){
			workflow.add(edge);
		}
		
		for(EmptyProduction emptyProduction : cfl.getEmptyProductions()) {
			for(Vertex vertex : this) {
				BaseEdge baseEdge = new BaseEdge(vertex, vertex, emptyProduction.getTarget(), 0);
				if(this.addEdge(baseEdge)) {
					workflow.add(baseEdge);	
				}
			}
		}
		
		while(!workflow.isEmpty()) {
			Edge currentEdge = workflow.remove();
			
			for(SingleProduction singleProduction : cfl.getSingleProductions()) {
				if(currentEdge.getElement().equals(singleProduction.getInput())) {
					SingleProductionEdge singleProductionEdge = new SingleProductionEdge(currentEdge.getSource(), currentEdge.getSink(), singleProduction.getTarget(), currentEdge);
					if(this.addEdge(singleProductionEdge)) {
						workflow.add(singleProductionEdge);
					}
				}
			}
			
			for(PairProduction pairProduction : cfl.getPairProductions()) {
				if(currentEdge.getElement().equals(pairProduction.getFirstInput())) {
					for(Edge edge : currentEdge.getSink().getOutgoingEdges()) {
						if(edge.getElement().equals(pairProduction.getSecondInput())) {
							PairProductionEdge pairProductionEdge = new PairProductionEdge(currentEdge.getSource(), edge.getSink(), pairProduction.getTarget(), currentEdge, edge);
							if(this.addEdge(pairProductionEdge)) {
								workflow.add(pairProductionEdge);
							}
						}
					}
				}
			}
			
			for(PairProduction pairProduction : cfl.getPairProductions()) {
				if(currentEdge.getElement().equals(pairProduction.getSecondInput())) {
					for(Edge edge : currentEdge.getSource().getIncomingEdges()) {
						if(edge.getElement().equals(pairProduction.getFirstInput())) {
							PairProductionEdge pairProductionEdge = new PairProductionEdge(edge.getSource(), currentEdge.getSink(), pairProduction.getTarget(), edge, currentEdge);
							if(this.addEdge(pairProductionEdge)) {
								workflow.add(pairProductionEdge);
							}
						}
					}
				}
			}
		}
	}
}
