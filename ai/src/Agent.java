
//	public static final int RIGHT=0;
//	public static final int DOWN=1;
//	public static final int LEFT=2;
//	public static final int UP=3;
//	시계방향

public class Agent {
	public int arrow = 3;
	public int gold = 0;

	public Direction d = new Direction();

	public String[] arr= {"→","↓","←","↑"};
//	public Pair cur;
//	public Pair prev;
//	
//	public Agent(Pair cur,Pair prev) {
//		
//	}

	public void goForward() {
		int direct = d.direction;
		WumpusWorld.prev.r = WumpusWorld.cur.r;
		WumpusWorld.prev.c = WumpusWorld.cur.c;

		switch (direct) {
		case 0:
			(WumpusWorld.cur.c)++;
			break;
		case 1:
			(WumpusWorld.cur.r)++;
			break;
		case 2:
			(WumpusWorld.cur.c)--;
			break;
		case 3:
			(WumpusWorld.cur.r)--;
			break;
		}
	}

	public void turnLeft() {
		d.turnLeft();
	}

	public void turnRight() {
		d.turnRight();
	}

	// grab the gold
	public void grab() {
		gold++;
		WumpusWorld.grabGold();
		System.out.println("gold 획득!!\n");

	}

	// shoot an arrow
	public boolean shoot(Block[][] maze, int r, int c, int direction) {
		if (arrow > 0) {
			int a = r, b = c;
			while (true) {
				if (maze[r][c].checkBump(direction)) { // bump
					System.out.println("wrong direction");
					return false;
				}
				arrow--;
				switch (direction) {
				case Direction.UP:
					a--;
					break;
				case Direction.DOWN:
					a++;
					break;
				case Direction.RIGHT:
					b++;
					break;
				case Direction.LEFT:
					b--;
					break;
				}
				
				System.out.println("shoot !! [ "+arr[direction]+" ]");
				if (WumpusWorld.checkScream(a, b)) {		//hear scream
					System.out.println("scream!!\n");
					if (maze[a][b].hasWumpus) {
						WumpusWorld.removeWumpus(a, b);
						break;
					}
				} else {	//no scream
				}
			}

			return true;
		} else{ // no arrow
			System.out.println("no arrow!!");
			return false;
		}
	}

	// climb
	public void climb() {
		System.out.println("탈출 성공!!");
	}

}
