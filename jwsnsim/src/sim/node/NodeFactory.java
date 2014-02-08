package sim.node;

import java.lang.reflect.Constructor;

import sim.topology.Topology;

public class NodeFactory {
	static public int numNodes;
	static public Node[] nodes = null;
	
	public static void createNodes(String classToLoad, int numNodes,Topology topology){
		NodeFactory.numNodes = numNodes;
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
			System.out.println("Problem loading/finding class "+ className );
			System.exit(-1);
		}
		
		return (Node)object;
	}
}
