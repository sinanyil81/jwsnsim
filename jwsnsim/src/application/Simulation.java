package application;

import sim.clock.Clock;
import sim.clock.ConstantDriftClock;
import sim.clock.Timer;
import sim.clock.TimerHandler;
import sim.configuration.NodeConfiguration;
import sim.simulator.Simulator;
import sim.statistics.Distribution;

public class Simulation implements TimerHandler {
	
	private int PERIOD = 20000000;
	public static long MAXSECOND =20000;	
	protected Clock clock = new ConstantDriftClock(1.0);
	protected Timer timer = new Timer(clock,this);
	protected Logger logger;
	
	public Simulation(String logFile, int durationTime){
		logger = new Logger(logFile);
		clock.start();
		timer.startOneshot(PERIOD);	
		
		for(int i=0;i<NodeConfiguration.numNodes;i++){
			try {
				NodeConfiguration.nodes[i].on();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
						
		MAXSECOND = durationTime;
		run();
	}
	
	public void run(){
		
		while(Simulator.getInstance().getSecond() < MAXSECOND){
			Simulator.getInstance().tick();
		}
		
		Simulator.getInstance().reset();
		
		exit();
	}
	
	public void exit() {
		logger.close();		
	}

	@Override
	public void fireEvent(Timer timer) {
		log();
		timer.startOneshot((int) (PERIOD + ((Distribution.getRandom().nextInt() % 4) + 1)*1000000));
	}

	private void log() {
		for(int i=0;i<NodeConfiguration.nodes.length;i++){
			logger.log(NodeConfiguration.nodes[i].toString());
		}
	}
}