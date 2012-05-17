package application.appTheoric;

public class EstimateLayer {
	private final int MAX_NEIGHBOR = 2;
	
	class Neighbor{
		public GradientNode node;
		public SimTime estimate = new SimTime();
		public SimTime timestamp = new SimTime();
		
		public Neighbor(){
			node = null;
		}
		
		public SimTime getEstimate(SimTime t){
			
			return estimate.add(t.sub(timestamp));
		}
	}

	Neighbor[] neighbors = new Neighbor[MAX_NEIGHBOR];
	private int maxNeighbor = -1, minNeighbor = -1;
	private HardwareClock clock = null;
	
	public EstimateLayer(HardwareClock clock){
		this.clock = clock;
		
		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new Neighbor();
		}
	}
	
	public void addNeighbor(GradientNode node){
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].node == null){
				neighbors[i].node = node;
				return;
			}
		}
	}
	
	public void updateEstimate(GradientNode node, SimTime value){
		for (int i = 0; i < neighbors.length; i++) {
			if( neighbors[i].node == node ){
				neighbors[i].estimate = value;
				neighbors[i].timestamp = clock.read();
				return;
			}					
		}
	}
			
	private void findMinimumEstimate() {
		
		minNeighbor = -1;
		SimTime t = clock.read();
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].node != null ){
				if(minNeighbor == -1){
					minNeighbor = i;	
				}				
				else if(neighbors[i].getEstimate(t).compareTo(neighbors[minNeighbor].getEstimate(t)) ==-1){
					minNeighbor = i;
				}
			}
		}
	}

	private void findMaximumEstimate() {
		maxNeighbor = -1;
		
		SimTime t = clock.read();
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].node != null){
				if(maxNeighbor == -1){
					maxNeighbor = i;	
				}				
				else if(neighbors[i].getEstimate(t).compareTo(neighbors[maxNeighbor].getEstimate(t)) ==1 ){
					maxNeighbor = i;
				}
			}
		}
	}
	
	public SimTime getMaximumEstimate(){
		findMaximumEstimate();
		SimTime t = clock.read();
		
		if(maxNeighbor != -1){
			return neighbors[maxNeighbor].getEstimate(t);
		}
		
		return null;
	}
	
	public SimTime getMinimumEstimate(){
		findMinimumEstimate();
		SimTime t = clock.read();
		
		if(minNeighbor != -1){
			return neighbors[minNeighbor].getEstimate(t);	
		}
		
		return null;
	}
}