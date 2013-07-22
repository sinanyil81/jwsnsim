package sim.jprowler;

public class Position {

	public double xCoord;
	public double yCoord;
	public double zCoord;

	public Position() {
		xCoord = 0;
		yCoord = 0;
		zCoord = 0;
	}

	public Position(double x, double y, double z) {
		xCoord = x;
		yCoord = y;
		zCoord = z;
	}

	public void set(Position p) {
		xCoord = p.xCoord;
		yCoord = p.yCoord;
		zCoord = p.zCoord;
	}

	public void set(double x, double y, double z) {
		xCoord = x;
		yCoord = y;
		zCoord = z;
	}

	public double distanceTo(Position pos) {
		return Math.sqrt(squareDistanceTo(pos));
	}

	public double squareDistanceTo(Position pos) {
		return ((xCoord - pos.xCoord) * (xCoord - pos.xCoord))
				+ ((yCoord - pos.yCoord) * (yCoord - pos.yCoord))
				+ ((zCoord - pos.zCoord) * (zCoord - pos.zCoord));
	}

	public boolean equals(Position p) {
		return p.xCoord == xCoord && p.yCoord == yCoord && p.zCoord == zCoord;
	}
	
	public String toString(){
		String s  ="";
		s += this.xCoord + " ";
		s += this.yCoord + " ";
		s += this.zCoord;
		
		return s;
	}
}
