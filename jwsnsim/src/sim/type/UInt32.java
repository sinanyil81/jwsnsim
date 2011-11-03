package sim.type;

/**
 * 
 * @author K. Sinan YILDIRIM
 *
 */
public class UInt32 implements Comparable<UInt32>{
	
	/** Maximum possible value. */
	public static final long MAX_VALUE = 0xFFFFFFFFL;
	
	/** Minimum possible value. */
	public static final long MIN_VALUE = 0;

	private long value;

	public UInt32() {
		this.value = 0;
	}
	
	public UInt32(UInt32 value) {
		this.value = value.getValue();
	}
	
	public UInt32(long value) {
		this.value = value & MAX_VALUE;
	}
	
	public UInt32(int value) {
		/* get all bits other than the sign bit */		
		this.value = value & 0x7FFFFFFFL;
		
		if(value < 0){			
			this.value |= 0x80000000L;
		}
		else{
			this.value = value;
		}		
	}

	public long getValue(){
		return value;
	}
	
	public UInt32 add(UInt32 x){
		long result = value + x.getValue();
		
		if(result > MAX_VALUE){
			result -= MAX_VALUE;
			result--;
		}
					
		return new UInt32(result);
	}
	
	public UInt32 twosComplement(){
		long result = value;
		
		/* inverse of 2's complement */
		result = (~result) & MAX_VALUE;
		
		if(result == MAX_VALUE){
			result = 0;
		}
		else{
			result++;
		}
		
		return new UInt32(result);
	}
	
	public UInt32 subtract(UInt32 x){
		
		UInt32 twosComplementOfX = x.twosComplement();
					
		return add(twosComplementOfX);
	}
	
	public int toInteger(){
		
		return (int)value;
	}
	
	public double toDouble(){
		
		return (double)((int)value);
	}

	/** The value of this as a string. */
	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public int compareTo(UInt32 o) {
		return (int)(value - o.getValue());
	}

}
