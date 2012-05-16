package application.appTheoric;

public class EstimateLayer {
	private final int MAX_NEIGHBOR = 2;
	
	class Neighbor{
		public GradientNode node;
		public double estimate;
		public double timestamp;
		
		public Neighbor(){
			node = null;
			estimate = 0;
			timestamp = 0;
		}
		
		public double getEstimate(double t){
			return estimate + t - timestamp;
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
	
	public void updateEstimate(GradientNode node, double value){
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
		double t = clock.read();
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].node != null ){
				if(minNeighbor == -1){
					minNeighbor = i;	
				}				
				else if(neighbors[i].getEstimate(t) < neighbors[minNeighbor].getEstimate(t)){
					minNeighbor = i;
				}
			}
		}
	}

	private void findMaximumEstimate() {
		maxNeighbor = -1;
		
		double t = clock.read();
		
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i].node != null){
				if(maxNeighbor == -1){
					maxNeighbor = i;	
				}				
				else if(neighbors[i].getEstimate(t) > neighbors[maxNeighbor].getEstimate(t)){
					maxNeighbor = i;
				}
			}
		}
	}
	
	public double getMaximumEstimate(){
		findMaximumEstimate();
		double t = clock.read();
		
		if(maxNeighbor != -1){
			return neighbors[maxNeighbor].getEstimate(t);
		}
		
		return -1;
	}
	
	public double getMinimumEstimate(){
		findMinimumEstimate();
		double t = clock.read();
		
		if(minNeighbor != -1){
			return neighbors[minNeighbor].getEstimate(t);	
		}
		
		return -1;
	}
}