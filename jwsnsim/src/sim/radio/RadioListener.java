package sim.radio;

public interface RadioListener {
	public void radioTransmissionBegin();
	public void radioTransmissionEnd();
	public void radioReceptionBegin();
	public void radioReceptionEnd();
}

