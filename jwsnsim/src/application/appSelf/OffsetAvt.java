package application.appSelf;

import sim.type.UInt32;

public class OffsetAvt {
	final private float INCREASE_FACTOR = 2.0f;
	final private float DECREASE_FACTOR = 3.0f;
	
	final static public int FEEDBACK_GREATER = 0;
	final static public int FEEDBACK_LOWER = 1;
	final static public int FEEDBACK_GOOD = 2;
	 
	UInt32 value = new UInt32();
	
	float delta, deltaMin, deltaMax;
	
	int lastFeedback = FEEDBACK_GOOD;
			
    public OffsetAvt(float dMin, float dMax)
    {	
		deltaMin = dMin;
		deltaMax = dMax;
		delta = (dMin + dMax)/2.0f;
    }
    
    public UInt32 getValue(){
    
    	return new UInt32(value);
    }
    
    void increaseDelta(){
    	delta = delta * INCREASE_FACTOR;    
    	
    	if(delta > deltaMax){
    		delta = deltaMax;
    	}	
    }
    
    void decreaseDelta(){
    	delta = delta / DECREASE_FACTOR;
    	
    	if(delta < deltaMin){
    		delta = deltaMin;
    	}     
    }
        
    void updateDelta(int feedback){
    	if (lastFeedback == FEEDBACK_GOOD) {
			if (feedback == FEEDBACK_GOOD) {
				decreaseDelta();
			} else {
				increaseDelta();
			}
		}else if (lastFeedback != feedback) {
			decreaseDelta();
		}else{
			increaseDelta();
		}
    }
       
    public void adjustValue(int feedback)
    {    	
    	// 1 - Updates the delta value
		updateDelta(feedback);

		// 2 - Adjust the current value
		if (feedback != FEEDBACK_GOOD) {
			value = value.add((int)(delta*(feedback == FEEDBACK_GREATER ? 1 : -1)));
		}
		
		lastFeedback = feedback;
    }
}
