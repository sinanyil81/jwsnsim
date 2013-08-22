package sim.jprowler.mac;

import java.util.Iterator;
import java.util.Vector;

import sim.jprowler.Node;
import sim.jprowler.radio.RadioListener;
import sim.jprowler.radio.RadioPacket;

public abstract class MacLayer implements RadioListener{
	Node node;
	Vector<MacListener> listeners = new Vector<MacListener>();
	
	public MacLayer(Node node){
		this.node = node;
		node.getRadio().setListener(this);
	}
	
	public void addListener(MacListener listener){
		listeners.add(listener);
	}
	
	public void packetReceipt(RadioPacket packet){
		for (Iterator<MacListener> iterator = listeners.iterator(); iterator.hasNext();) {
			MacListener listener = (MacListener) iterator.next();
			listener.receiveMessage(packet);			
		}
	}
	
	public void notifyOn(){
		for (Iterator<MacListener> iterator = listeners.iterator(); iterator.hasNext();) {
			MacListener listener = (MacListener) iterator.next();
			listener.on();			
		}
	}
	
	public void notifyOff(){
		for (Iterator<MacListener> iterator = listeners.iterator(); iterator.hasNext();) {
			MacListener listener = (MacListener) iterator.next();
			listener.off();			
		}
	}
	
	public void notifyPacketLost(){
		for (Iterator<MacListener> iterator = listeners.iterator(); iterator.hasNext();) {
			MacListener listener = (MacListener) iterator.next();
			listener.packetLost();			
		}
	}
}
