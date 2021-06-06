
public class Direction {
	public static final int RIGHT=0;
	public static final int DOWN=1;
	public static final int LEFT=2;
	public static final int UP=3;
	
	public int direction=RIGHT;	//default
	
	public void turnLeft() {
		direction=(direction-1)%4;
	}
	
	public void turnRight() {
		direction=(direction+1)%4;
	}
	
	public void setDirection(int d) {
		direction=d;
	}
}
