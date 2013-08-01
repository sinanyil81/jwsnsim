package sim.jprowler;

public interface RadioListener {
	
	public void startedReceiving();
	public void stoppedReceiving();
	
	public void startedTransmitting();
	public void stoppedTransmitting();
	
	public void sleepTimerExpired();
}
