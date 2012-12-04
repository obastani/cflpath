package org.cflpath.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cflpath.cfl.CFL;
import org.cflpath.cfl.CFL.Path;
import org.cflpath.cfl.Element;
import org.cflpath.cfl.Element.Terminal;
import org.cflpath.cfl.Element.Variable;
import org.cflpath.cfl.Production;
import org.cflpath.cfl.Production.PairProduction;
import org.cflpath.cfl.Production.SingleProduction;
import org.cflpath.graph.CFLGraph.Edge;
import org.cflpath.graph.CFLGraph.Vertex;
import org.cflpath.graph.FlowsToContextGraph;
import org.cflpath.utility.MultivalueMap;

public class Main {
	
	public static CFL getInput(BufferedReader input) throws IOException {
		// graph
		FlowsToContextGraph graph = new FlowsToContextGraph();
		
		String line;
		while((line = input.readLine()) != null) {
			String[] tokens = line.split(" ");
			if(tokens.length == 3) {
				Vertex source = new Vertex(tokens[0]);
				Vertex sink = new Vertex(tokens[1]);
	
				Terminal label = null;
				if(tokens[2].startsWith("new")) {
					label = graph.getNew();
				} else if(tokens[2].startsWith("load_")) {
					String field = tokens[2].substring(5);
					graph.addField(field);
					label = graph.getLoad(field);
				} else if(tokens[2].startsWith("store_")) {
					String reference = tokens[2].substring(6);
					graph.addField(reference);
					label = graph.getStore(reference);
				} else if(tokens[2].startsWith("assign")) {
					label = graph.getAssign();
				} else if(tokens[2].startsWith("enter_")) {
					String method = tokens[2].substring(6);
					graph.addMethod(method);
					label = graph.getEnter(method);
				} else if(tokens[2].startsWith("exit_")) {
					String method = tokens[2].substring(5);
					graph.addMethod(method);
					label = graph.getExit(method);
				}
				graph.addEdge(source, sink, label);
			}
		}
		
		MultivalueMap<String,Vertex> methodArgs = new MultivalueMap<String,Vertex>();
		HashMap<String,Vertex> methodRet = new HashMap<String,Vertex>();
		for(Vertex vertex : graph) {
			String name = vertex.getName();
			if(name.startsWith("$PARAM$STUB")) {
				String[] tokens = name.substring(12, name.length()-1).split("\\]\\[");
				//System.out.println(tokens[0] + "," + tokens[1]);
				if(tokens[1].equals("ret")) {
					methodRet.put(tokens[0], new Vertex(name));
				} else {
					methodArgs.add(tokens[0], new Vertex(name));
				}
			}
		}
		for(String methodName : methodArgs.keySet()) {
			graph.addStubMethod(methodArgs.get(methodName), methodRet.get(methodName), methodName);
		}
		
		graph.addProductions(graph.getFlowsToCFL());
		for(Edge edge : graph.getEdges()) {
			if(edge.getElement().equals(new Variable("flowsTo"))) {
				System.out.println(edge.toString());
			}
		}
				
		//System.out.println(graph);
		//System.out.println(graph.getFlowsToCFL());
		
		//return graph.getFlowsToGraphCFL();
		return new CFL();
	}
	
	public static CFL getSimpleCFLGraph() {
		// graph
		FlowsToContextGraph graph = new FlowsToContextGraph();
		
		// references
		graph.addField("f");
		
		// terminals
		Terminal assign = graph.getAssign();
		Terminal new_terminal = graph.getNew();
		Terminal store_f = graph.getStore("f");
		Terminal load_f = graph.getLoad("f");
		
		// vertices
		Vertex v = new Vertex("v");
		Vertex w = new Vertex("w");
		Vertex x = new Vertex("x");
		Vertex y = new Vertex("y");
		Vertex z = new Vertex("z");
		
		Vertex o1 = new Vertex("o1");
		Vertex o2 = new Vertex("o2");
		
		// edges
		graph.addEdge(o1, x, new_terminal);
		graph.addEdge(o2, z, new_terminal);
		graph.addEdge(z, y, store_f);
		graph.addEdge(x, y, assign);
		graph.addEdge(x, w, assign);
		graph.addEdge(w, v, load_f);
		
		// flows to cfl
		//System.out.println(graph.getFlowsToCFL());
		
		// return graph cfl
		return graph.getFlowsToGraphCFL();
	}
	
	public static CFL getSimpleCFL() {
		CFL cfl = new CFL();
		Terminal A = new Terminal("A", 1.0);
		Terminal B = new Terminal("B", 5.0);
		Variable C = new Variable("C");
		Variable D = new Variable("D");
		Production P1 = new PairProduction(C, A, B);
		Production P2 = new SingleProduction(D, B);
		Production P3 = new PairProduction(C, A, C);
		cfl.add(P1);
		cfl.add(P2);
		cfl.add(P3);
		return cfl;
	}
	
	public static void main(String[] args) {
		/*
		FlowsToContextGraph contextGraph = new FlowsToContextGraph();
		contextGraph.addMethod("foo");
		NormalCFL cfl = contextGraph.getContextCFL();
		System.out.println(cfl.getCFL());
		*/
		try {
			CFL graphCfl = getInput(new BufferedReader(new FileReader("input.txt")));
			//CFL graphCfl = getSimpleCFLGraph();
			//System.out.println(graphCfl);
			Map<Element,Path> shortestPaths = graphCfl.getShortestPaths();
			for(Map.Entry<Element,Path> entry : shortestPaths.entrySet()) {
				if(entry.getKey().getName().startsWith("flowsTo(")) {
					System.out.println("element: " + entry.getKey().getName() + ", weight: " + entry.getValue().getWeight());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
