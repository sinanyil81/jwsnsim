package application.appSelf;

/**
 * A tool to calculate the mean of a data series. Data are continuously fed and
 * their average are calculated.
 * 
 * @author Önder Gürcan
 * 
 */
public class Averager {

	private double average = 0.0;

	private double elementCount = 0.0;

	public double getAverage() {
		return this.average;
	}

	public void update(double value) {
		average = ((average * elementCount) + value) / ++elementCount;
	}

	public double getElementCount() {
		return elementCount;
	}

	public void update(Averager averager) {
		if (averager.getElementCount() > 0) {
			double newElementCount = elementCount + averager.getElementCount();
			average = ((average * elementCount) + (averager.getAverage() * averager
					.getElementCount())) / (newElementCount);
			this.elementCount = newElementCount;
		}
	}

}
