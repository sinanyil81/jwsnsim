package sim.node;

public class Position {

	public int xCoord;
	public int yCoord;
	public int zCoord;

	public Position() {
		xCoord = 0;
		yCoord = 0;
		zCoord = 0;
	}
	
	public Position(double x, double y, double z) {
		xCoord = (int)x;
		yCoord = (int)y;
		zCoord = (int)z;
	}

	public Position(int x, int y, int z) {
		xCoord = x;
		yCoord = y;
		zCoord = z;
	}

	public void set(Position p) {
		xCoord = p.xCoord;
		yCoord = p.yCoord;
		zCoord = p.zCoord;
	}

	public void set(int x, int y, int z) {
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
}
