package application.tools;

public class AvtSimple {
	final private float INCREASE_FACTOR = 2.0f;
	final private float DECREASE_FACTOR = 3.0f;
	
	final static public int FEEDBACK_GREATER = 0;
	final static public int FEEDBACK_LOWER = 1;
	final static public int FEEDBACK_GOOD = 2;
	final static public int FEEDBACK_NONE = 3;
	 
	float lowerBound = 0.0f;
	float upperBound = 0.0f;
	float value = 0.0f;
	
	float delta, deltaMin, deltaMax;
	
	int lastFeedback = FEEDBACK_NONE;
	
	public int NODE = 0;
			
    public AvtSimple(float lBound,float uBound,float val,float dMin, float dMax)
    {
		lowerBound = lBound;
		upperBound = uBound;
		value = val;
		
		deltaMin = dMin;
		deltaMax = dMax;
		delta = dMax;
    }
    
    public float getValue(){
    
    	return value;
    }
    
    public float getDelta(){
    	return delta;
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
    	if(lastFeedback == FEEDBACK_NONE)
    		return;
    	
    	if (lastFeedback == FEEDBACK_GOOD) {  		
			if (feedback == FEEDBACK_GOOD) {
				decreaseDelta();
			} else {
//				decreaseDelta();
//				increaseDelta();
			}
		}else if (lastFeedback != feedback) {
			decreaseDelta();
		}else{
			increaseDelta();
		}    	
    }
    
    float min(float a,float b){
    	if(a<b) 
    		return a;
    		
    	return b;
    }
    
    float max(float a,float b){
    	if(a>b) 
    		return a;
    		
    	return b;
    }
    
    public void adjustValue(int feedback)
    {    	
		updateDelta(feedback);

		if (feedback != FEEDBACK_GOOD) {
			value = min(upperBound,max(lowerBound,value + delta*(feedback == FEEDBACK_GREATER ? 1.0f : -1.0f)));
		}
		
		lastFeedback = feedback;
    }
}
