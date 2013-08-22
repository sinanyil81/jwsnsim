package sim.jprowler;

import sim.jprowler.clock.Clock;
import sim.jprowler.radio.Radio;

public class Node {
		
	protected int id;	
	private Radio radio;
	private Clock clock;
	
	private boolean isOn = false;
	protected Position position = new Position();
	

	public Node(int id, Clock clock){
		this.id = id;
		this.clock = clock;
		this.radio = new Radio(this,clock);
	} 
	
	public boolean isOn(){
		return isOn;
	}

	public void turnOn(){
		isOn = true;
		clock.start();
	}
	
	public void turnOff(){
		isOn = false;
		clock.stop();
	}
	
	public Position getPosition(){
		return position;
	}

	public void setId( int id ){
		this.id = id;
	}

	public void setPosition( Position pos ){
		 
		this.position.xCoord = pos.xCoord;
		this.position.yCoord = pos.yCoord;
		this.position.zCoord = pos.zCoord;
	}
	
	public int getId(){
		return id;
	}
	
	public Clock getClock(){
		return clock;
	}

	public Radio getRadio() {
		return this.radio;
	}
}
