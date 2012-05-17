package application.appTheoric;

public interface Clock {
	public void progress(double amount);
	public double getDrift();
}
