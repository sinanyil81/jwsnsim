package sim.configuration;

import java.lang.reflect.Constructor;

import sim.node.Node;
import sim.node.Position;
import sim.topology.Topology;

public class NodeConfiguration {
	static public int numNodes;
	static public Node[] nodes = null;
	
	public static void createNodes(String classToLoad, int numNodes,Topology topology){
		NodeConfiguration.numNodes = numNodes;
		topology.initialize();
		nodes = new Node[numNodes];	
		for(int i=0;i<numNodes;i++){
			nodes[i] = createNode(classToLoad, i+1, topology.getNextPosition());
		}

	}
	
	static Node createNode(String className,int id, Position position){
		Class<?> c;
		Object object = null;
		try {
			c = Class.forName(className);
			Constructor<?> cons = c.getConstructor(int.class,Position.class);
			object = cons.newInstance(new Object[] {id,position});
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		return (Node)object;
	}
}
