package com.leav.worldtree;


public class Pos {
	public final int x, y, z;
	
	public Pos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pos)) return false;
        Pos key = (Pos) o;
        return x == key.x && y == key.y && z == key.z;
    }

    @Override
    public int hashCode() {
    	return (31 * x + y) * 31 + z;
    }
	
	public int euclideanDist(Pos dest) {
		return Math.abs(x - dest.x) + Math.abs(y - dest.y) + Math.abs(z - dest.z);
	}
	
	public double dist(Pos dest) {
		return Math.sqrt((x - dest.x) * (x - dest.x) + (y - dest.y) * (y - dest.y) + (z - dest.z) * (z - dest.z));
	}
	
	public double distSquared(int x, int y, int z) {
		return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) + (z - this.z) * (z - this.z);
	}
	
	
	// directions
	
	public Pos dir(int dir) {
		switch(dir) {
		case 0:
			return down();
		case 1:
			return up();
		case 2:
			return north();
		case 3:
			return south();
		case 4:
			return west();
		case 5:
			return east();
		case 6:
			return downNorth();
		case 7:
			return downSouth();
		case 8:
			return downWest();
		case 9:
			return downEast();
		case 10:
			return upNorth();
		case 11:
			return upSouth();
		case 12:
			return upWest();
		case 13:
			return upEast();
		case 14:
			return northWest();
		case 15:
			return northEast();
		case 16:
			return southWest();
		case 17:
			return southEast();
		case 18:
			return downNorthWest();
		case 19:
			return downNorthEast();
		case 20:
			return downSouthWest();
		case 21:
			return downSouthEast();
		case 22:
			return upNorthWest();
		case 23:
			return upNorthEast();
		case 24:
			return upSouthWest();
		case 25:
			return upSouthEast();
		}
		return this;
	}
	
	public static int dirFaceBegin() {
		return 0;
	}
	
	public static int dirFaceEnd() {
		return 5;
	}
	
	public static int dirEdgeBegin() {
		return 6;
	}
	
	public static int dirEdgeEnd() {
		return 17;
	}
	
	public static int dirCornerBegin() {
		return 18;
	}
	
	public static int dirCornerEnd() {
		return 25;
	}
	
	public Pos down() {
		return new Pos(x, y - 1, z);
	}
	
	public Pos up() {
		return new Pos(x, y + 1, z);
	}
	
	public Pos north() {
		return new Pos(x, y, z - 1);
	}
	
	public Pos south() {
		return new Pos(x, y, z + 1);
	}
	
	public Pos west() {
		return new Pos(x - 1, y, z);
	}
	
	public Pos east() {
		return new Pos(x + 1, y, z);
	}
	
	public Pos downNorth() {
		return new Pos(x, y - 1, z - 1);
	}
	
	public Pos downSouth() {
		return new Pos(x, y - 1, z + 1);
	}
	
	public Pos downWest() {
		return new Pos(x - 1, y - 1, z);
	}
	
	public Pos downEast() {
		return new Pos(x + 1, y - 1, z);
	}
	
	public Pos upNorth() {
		return new Pos(x, y + 1, z - 1);
	}
	
	public Pos upSouth() {
		return new Pos(x, y + 1, z + 1);
	}
	
	public Pos upWest() {
		return new Pos(x - 1, y + 1, z);
	}
	
	public Pos upEast() {
		return new Pos(x + 1, y + 1, z);
	}
	
	public Pos northWest() {
		return new Pos(x - 1, y, z - 1);
	}
	
	public Pos northEast() {
		return new Pos(x + 1, y, z - 1);
	}
	
	public Pos southWest() {
		return new Pos(x - 1, y, z + 1);
	}
	
	public Pos southEast() {
		return new Pos(x + 1, y, z + 1);
	}
	
	public Pos downNorthWest() {
		return new Pos(x - 1, y - 1, z - 1);
	}
	
	public Pos downNorthEast() {
		return new Pos(x + 1, y - 1, z - 1);
	}
	
	public Pos downSouthWest() {
		return new Pos(x - 1, y - 1, z + 1);
	}
	
	public Pos downSouthEast() {
		return new Pos(x + 1, y - 1, z + 1);
	}
	
	public Pos upNorthWest() {
		return new Pos(x - 1, y + 1, z - 1);
	}
	
	public Pos upNorthEast() {
		return new Pos(x + 1, y + 1, z - 1);
	}
	
	public Pos upSouthWest() {
		return new Pos(x - 1, y + 1, z + 1);
	}
	
	public Pos upSouthEast() {
		return new Pos(x + 1, y + 1, z + 1);
	}
}
