package sim.clock;

import hardware.clock.Timer;

public interface TimerHandler {
	void fireEvent(Timer timer);
}
