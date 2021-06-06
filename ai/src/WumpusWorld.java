
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Random;

class WumpusWorld {
	static int n = 4;
	static int shootdirection;
	static boolean flag = true;
	static Pair cur = new Pair(0, 0); // (1,1)			//현재 위치한 격자 위치
	static Pair prev = new Pair(-1, -1);				//이전에 위치했던 격자 위치


	static Block_A[][] maze_a;
	static Block[][] maze;
	static Agent robot = new Agent();
	static int[] prior = new int[4]; // direction 우선순위

	static void makeWorld() {

		Random rand = new Random();

		int num = 0;
		maze = new Block[n][n];
		maze_a = new Block_A[n][n];
		for (int i = 0; i < n; i++) {
			maze[i] = new Block[n];
			maze_a[i] = new Block_A[n];

			for (int j = 0; j < n; j++) {
				maze[i][j] = new Block();
				maze_a[i][j] = new Block_A();
			}
		}

		int x, y;

		maze_a[0][0].setSafe();
		maze_a[1][0].setSafe();
		maze_a[0][1].setSafe();
//		safeStatusSet(0, 0);
//		safeStatusSet(1, 0);
//		safeStatusSet(0, 1);

//		maze_a[0][0].Safe = true;
//		maze_a[1][0].Safe = true;
//		maze_a[0][1].Safe = true;
		maze[0][0].Safe = true; // [1,1]
		maze[1][0].Safe = true; // [1,2]
		maze[0][1].Safe = true; // [2,1]

		x = rand.nextInt(4);
		y = rand.nextInt(4);

		// setBump
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == 0) {
					maze[i][j].setBump(Direction.UP, true);
				}
				if (j == 0) {
					maze[i][j].setBump(Direction.LEFT, true);
				}
				if (i == n - 1) {
					maze[i][j].setBump(Direction.DOWN, true);
				}
				if (j == n - 1) {
					maze[i][j].setBump(Direction.RIGHT, true);
				}
			}
		}

		// gold
		while ((x == 0 && y == 0) || (x == 0 && y == 1) || (x == 1 && y == 0)) {
			x = rand.nextInt(4);
			y = rand.nextInt(4);
		}
		addGold(x, y);

		// wumpus
		while (num == 0) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (maze[i][j].Safe || maze[i][j].hasGold)
						continue;

					if (rand.nextInt(15) == 0) {
						addWumpus(i, j);
						num++;
					}
				}
			}
		}

		// pitch
		num = 0;
		while (num == 0) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (maze[i][j].Safe || maze[i][j].hasGold || maze[i][j].hasWumpus)
						continue;

					if (rand.nextInt(15) == 0) {
						addPit(i, j);
						num++;
					}

				}
			}
		}

	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		makeWorld();	//1)

		int moves = 0;
		int r = cur.r;
		int c = cur.c;
		int rPrev = prev.r;
		int cPrev = prev.c;
		System.out.println("\nInitial state:");

		while (!maze[cur.r][cur.c].Glitter) {	//2)
//			r = cur.r;
//			c = cur.c;
//			rPrev = prev.r;
//			cPrev = prev.c;

			printMaze();	//1.
			// 초기

			if (!maze_a[r][c].visited) {	//2.
				for (int i = 0; i < 4; i++) {
					maze_a[r][c].setBump(i, maze[r][c].checkBump(i));
				}
			}

			maze_a[r][c].visited = true;

			//3.
			// pit or wumpus
			if (maze[r][c].hasPit) {
				System.out.println("[" + cur.r + ", " + cur.c + "] has a pit..\n");
				maze_a[r][c].pitStatus = Block_A.PRESENT;
				maze_a[r][c].wumpusStatus = Block_A.NOT_PRESENT; // 겹침 xx
				cur.r = 0;
				cur.c = 0;
				prev.r = -1;
				prev.c = -1;
				r = cur.r;
				c = cur.c;
				rPrev = prev.r;
				cPrev = prev.c;

				continue;
			} else if (maze[r][c].hasWumpus) {
				System.out.println("[" + cur.r + ", " + cur.c + "] has a wumpus..\n");
				maze_a[r][c].wumpusStatus = Block_A.PRESENT;
				maze_a[r][c].pitStatus = Block_A.NOT_PRESENT;
				cur.r = 0;
				cur.c = 0;
				prev.r = -1;
				prev.c = -1;
				r = cur.r;
				c = cur.c;
				rPrev = prev.r;
				cPrev = prev.c;
				continue;
			}

			//4.
			NowumpusStatusSet(r, c);
			NopitStatusSet(r, c);
			maze_a[r][c].updateSafe();

			
			//5.
			// breeze
//			if (!maze[r][c].Breeze && !maze_a[r][c].Safe) {
			if (maze[r][c].Breeze) {
				pitStatusSet(r, c);
			}

			// stench
			if (maze[r][c].Stench) {
				wumpusStatusSet(r, c);
			}

			 // 주변 격자들에 대한 safe 처리
			if (!maze[r][c].Breeze && !maze[r][c].Stench) {
				safeStatusSet(r, c);
			}


			// set direction
			if (flag) {				//(6-1)
				int i = selectDirection(r, c, robot.d.direction); // right direction check

				// turn
				if (robot.d.direction < i) { // turnR
					while (robot.d.direction != i) {
						robot.turnRight();
					}
				} else { // turnL
					while (robot.d.direction != i) {
						robot.turnLeft();
					}
				}

				robot.goForward();
				
			} else {
				// shoot			//(6-2)
				robot.shoot(maze, r, c, shootdirection);
				flag = true;
				continue;			//action을 취한 후 격자들에 대한 정보를 다시 업데이트 후, 이동 방향 정함
			}

			moves++;

			System.out.println("\n\nMove " + moves + ":");

			r = cur.r;
			c = cur.c;
			rPrev = prev.r;
			cPrev = prev.c;

//			if (moves > n * n) {
//				System.out.println("\nNo solution found!");
//				break;
//			}
		}

		//3)
		// grab
		robot.grab();
		printMaze();
		System.out.println();

		GoHome();

		sc.close();
	}

	static void addPit(int r, int c) {
		maze[r][c].hasPit = true;

		setBreeze(r, c);
	}

	static void setBreeze(int r, int c) {

		if (!(maze[r][c].checkBump(Direction.UP)))
			maze[r - 1][c].Breeze = true;
		if (!(maze[r][c].checkBump(Direction.DOWN)))
			maze[r + 1][c].Breeze = true;
		if (!(maze[r][c].checkBump(Direction.LEFT)))
			maze[r][c - 1].Breeze = true;
		if (!(maze[r][c].checkBump(Direction.RIGHT)))
			maze[r][c + 1].Breeze = true;

	}

	static void addWumpus(int r, int c) {
		maze[r][c].hasWumpus = true;

		setStench(r, c);
	}

	static void removeWumpus(int r, int c) {
		maze[r][c].hasWumpus = false;
		refreshStench();
	}

	static void setStench(int r, int c) {

		if (!(maze[r][c].checkBump(Direction.UP)))
			maze[r - 1][c].Stench = true;
		if (!(maze[r][c].checkBump(Direction.DOWN)))
			maze[r + 1][c].Stench = true;
		if (!(maze[r][c].checkBump(Direction.LEFT)))
			maze[r][c - 1].Stench = true;
		if (!(maze[r][c].checkBump(Direction.RIGHT)))
			maze[r][c + 1].Stench = true;
	}

	static void refreshStench() { // refresh maze
		// remove stench
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				maze[i][j].Stench = false;
			}
		}

		// reset
		int a = 0, b = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (maze[i][j].hasWumpus) {
					setStench(i, j);
				}
			}
		}
	}

	static void addGold(int r, int c) {
		maze[r][c].hasGold = true;
		maze[r][c].Glitter = true;
	}

	static void grabGold() {
		maze[cur.r][cur.c].hasGold = false;
		maze[cur.r][cur.c].Glitter = false;
	}

	static void pitStatusSet(int r, int c) {
		int a, b;

		// UP
		if (!(maze_a[r][c].checkBump(Direction.UP)) && maze_a[r - 1][c].pitStatus != Block_A.NOT_PRESENT) { // !(safe처리
																											// or bump)
			a = r - 1;
			b = c;

			if (maze_a[a][b].pitStatus == Block_A.DEFAULT) { // default(-1)
				maze_a[a][b].pitStatus = Block_A.UNSURE;
			} else if (maze_a[a][b].pitStatus < Block_A.PRESENT) { // 4> >=1
				(maze_a[a][b].pitStatus)++;
			}
		}

		// DOWN
		if (!(maze_a[r][c].checkBump(Direction.DOWN)) && maze_a[r + 1][c].pitStatus != Block_A.NOT_PRESENT) {
			a = r + 1;
			b = c;

			if (maze_a[a][b].pitStatus == Block_A.DEFAULT) { // default(-1)
				maze_a[a][b].pitStatus = Block_A.UNSURE;
			} else if (maze_a[a][b].pitStatus < Block_A.PRESENT) {
				(maze_a[a][b].pitStatus)++;
			}
		}

		// LEFT
		if (!(maze_a[r][c].checkBump(Direction.LEFT)) && maze_a[r][c - 1].pitStatus != Block_A.NOT_PRESENT) {
			a = r;
			b = c - 1;

			if (maze_a[a][b].pitStatus == Block_A.DEFAULT) { // default(-1)
				maze_a[a][b].pitStatus = Block_A.UNSURE;
			} else if (maze_a[a][b].pitStatus < Block_A.PRESENT) {
				(maze_a[a][b].pitStatus)++;
			}
		}

		// RIGHT
		if (!(maze_a[r][c].checkBump(Direction.RIGHT)) && maze_a[r][c + 1].pitStatus != Block_A.NOT_PRESENT) {
			a = r;
			b = c + 1;

			if (maze_a[a][b].pitStatus == Block_A.DEFAULT) { // default(-1)
				maze_a[a][b].pitStatus = Block_A.UNSURE;
			} else if (maze_a[a][b].pitStatus < Block_A.PRESENT) {
				(maze_a[a][b].pitStatus)++;
			}
		}
	}

	static void wumpusStatusSet(int r, int c) {
		int a, b;

		// UP r >= 1
		if (!(maze_a[r][c].checkBump(Direction.UP)) && maze_a[r - 1][c].wumpusStatus != Block_A.NOT_PRESENT) { // !(safe처리
																												// or
																												// bump)
			a = r - 1;
			b = c;

			if (maze_a[a][b].wumpusStatus == Block_A.DEFAULT) {
				maze_a[a][b].wumpusStatus = Block_A.UNSURE;
			} else { // shoot
				(maze_a[a][b].wumpusStatus)++;

				if (maze_a[a][b].wumpusStatus >= 2) {
					// turn
					int tempD = Direction.UP;

					// shoot
					flag = false;
					shootdirection = tempD;

					return;
//				robot.shoot(maze, r, c, tempD); // r,c

				}
			}
		}

		// DOWN r < n - 2
		if (!(maze_a[r][c].checkBump(Direction.DOWN)) && maze_a[r + 1][c].wumpusStatus != Block_A.NOT_PRESENT) {
			a = r + 1;
			b = c;

			if (maze_a[a][b].wumpusStatus == Block_A.DEFAULT) {
				maze_a[a][b].wumpusStatus = Block_A.UNSURE;
			} else {

				(maze_a[a][b].wumpusStatus)++;
				if (maze_a[a][b].wumpusStatus >= 2) {
					int tempD = Direction.DOWN;

					// shoot
					flag = false;
					shootdirection = tempD;

					return;
				}
			}
		}

		// RIGHT c < n - 2
		if (!(maze_a[r][c].checkBump(Direction.RIGHT)) && maze_a[r][c + 1].wumpusStatus != Block_A.NOT_PRESENT) {
			a = r;
			b = c + 1;

			if (maze_a[a][b].wumpusStatus == Block_A.DEFAULT) {
				maze_a[a][b].wumpusStatus = Block_A.UNSURE;
			} else {

				(maze_a[a][b].wumpusStatus)++;
				if (maze_a[a][b].wumpusStatus >= 2) {
					int tempD = Direction.RIGHT;

					// shoot
					flag = false;
					shootdirection = tempD;

					return;
				}
			}
		}

		// LEFT c >= 1
		if (!(maze_a[r][c].checkBump(Direction.LEFT)) && maze_a[r][c - 1].wumpusStatus != Block_A.NOT_PRESENT) {
			a = r;
			b = c - 1;

			if (maze_a[a][b].wumpusStatus == Block_A.DEFAULT) {
				maze_a[a][b].wumpusStatus = Block_A.UNSURE;
			} else { // shoot
				(maze_a[a][b].wumpusStatus)++;

				if (maze_a[a][b].wumpusStatus >= 2) {
					int tempD = Direction.LEFT;
					flag = false;
					shootdirection = tempD;

					return;
				}
			}
		}
	}

	static boolean checkScream(int r, int c) {

		maze_a[r][c].wumpusStatus = Block_A.NOT_PRESENT;
		if (maze[r][c].hasWumpus) {
			return true;
		}
		return false;
	}

	static void safeStatusSet(int r, int c) {
		int a, b;

		// UP
		if (!(maze_a[r][c].checkBump(Direction.UP))) { // !(bump)
			a = r - 1;
			b = c;

			NowumpusStatusSet(a, b);
			NopitStatusSet(a, b);
			maze_a[a][b].updateSafe();

		}

		// DOWN
		if (!(maze_a[r][c].checkBump(Direction.DOWN))) {
			a = r + 1;
			b = c;

			NowumpusStatusSet(a, b);
			NopitStatusSet(a, b);
			maze_a[a][b].updateSafe();
		}

		// RIGHT
		if (!(maze_a[r][c].checkBump(Direction.RIGHT))) {
			a = r;
			b = c + 1;

			NowumpusStatusSet(a, b);
			NopitStatusSet(a, b);
			maze_a[a][b].updateSafe();
		}

		// LEFT
//		if (c >= 1 && !maze_a[r][c - 1].Safe) {
		if (!(maze_a[r][c].checkBump(Direction.LEFT))) {
			a = r;
			b = c - 1;

			NowumpusStatusSet(a, b);
			NopitStatusSet(a, b);
			maze_a[a][b].updateSafe();
		}

	}

	static void NowumpusStatusSet(int r, int c) {
		maze_a[r][c].wumpusStatus = Block_A.NOT_PRESENT;
	}

	static void NopitStatusSet(int r, int c) {
		maze_a[r][c].pitStatus = Block_A.NOT_PRESENT;
	}

	static int selectDirection(int r, int c, int direct) {
		int[] prior = new int[4]; // right, down, left, up (index -> direction)
		int[] status = new int[4];

		Arrays.fill(prior, -1);

		// set priority
		int a = 0, b = 0;
		for (int i = 0; i < 4; i++) {

			if (maze_a[r][c].checkBump(i))
				continue; /// bump

			switch (i) {
			case 0:
				a = r;
				b = c + 1;
				break;
			case 1:
				a = r + 1;
				b = c;
				break;
			case 2:
				a = r;
				b = c - 1;
				break;
			case 3:
				a = r - 1;
				b = c;
				break;
			}

			//(우선순위2)
			if (maze_a[a][b].Safe && !maze_a[a][b].visited) {
				prior[i] = 2;
			}

			//(우선순위1)
			else if (!maze_a[a][b].visited) {
				int pits = maze_a[a][b].pitStatus;
				int wums = maze_a[a][b].wumpusStatus;

				prior[i] = 1;
				if(pits<wums) {
					status[i] = wums;
				}else {
					if (pits >= Block_A.PRESENT) {	//(우선순위 -1) : 고려x
						prior[i]=-1;
					} else {
						status[i] = pits;
					}
				}
				
				
//				if (pits < wums && wums != Block_A.NOT_PRESENT) {	//wums
////				if (pits > wums && wums != Block_A.NOT_PRESENT) {	//wums
//					status[i] = wums;
//				} else {											//pits
//					if (pits >= Block_A.PRESENT) {
//						prior[i]=-1;
//					} else {
//						status[i] = pits;
//					}
//				}

			}

			//(우선순위3)
			else { // (maze_a[r][c].visited)
				prior[i] = 0;
			}
		}

		
		//우선순위 방식 선택
		int value = -1000;
		int index = -1;
		for (int i = 0; i < 4; i++) {
			if (value < prior[i] && prior[i] != -1) {
//			if (value < prior[i] && prior[i] != -1) {
				value = prior[i];
				index = i;
			}
		}

		
		
		//(우선순위1) : status 값 낮은 순
		if (value == 1) { // status check

			// pair array
			Pair[] p = new Pair[4];
			for (int i = 0; i < 4; i++) {
				if (prior[i] != 1) {
					p[i] = new Pair(-1, -1);
					
				} else if (status[i] == Block_A.PRESENT) {
					p[i] = new Pair(-1, i);
				} else {
					//(statusvalue,index)
					p[i] = new Pair(status[i], i);
				}
			}

			// statusvalue 낮은 direction 선택
			value = 1000;
			for (int i = 0; i < 4; i++) {
				if (p[i].r < value && p[i].r != -1) {
//				if (p[i].r > value && p[i].r != -1) {
					index = i;
				}
			}
		}
		return index;
	}

	static void printNow() { // [x,y]
		int buffr = cur.r - 1;
		int buffc = cur.c - 1;

		int x = 1 + buffc;
		int y = 1 + buffr;
		System.out.println("[" + x + ", " + y + "]\n");
	}

	static void GoHome() {

		if (robot.gold == 1) {

			while (!(cur.r == 0 && cur.c == 0)) { //

				if (!maze_a[cur.r][cur.c].visited) {
					for (int i = 0; i < 4; i++) {
						maze_a[cur.r][cur.c].setBump(i, maze[cur.r][cur.c].checkBump(i));
					}
				}

				if (!(maze_a[cur.r][cur.c].checkBump(Direction.UP)) && maze_a[cur.r - 1][cur.c].Safe) { // 상 cur.r >= 1
					prev.r = cur.r;
					cur.r--;
					printMaze();
					printNow();
//					System.out.println("[" + cur.r + ", " + cur.c + "]\n");
					continue;
				} else if (!(maze_a[cur.r][cur.c].checkBump(Direction.LEFT)) && maze_a[cur.r][cur.c - 1].Safe) { // 좌
																													// cur.c
																													// >=
																													// 1
					prev.c = cur.c;
					cur.c--;
					printMaze();
					printNow();
					continue;
				} else {
					if (!(maze_a[cur.r][cur.c].checkBump(Direction.DOWN)) && maze_a[cur.r + 1][cur.c].Safe) {// 하 cur.r
																												// <= (n
																												// - 2)
						prev.r = cur.r;
						cur.r++;
						printMaze();
						printNow();
						continue;

					} else if (!(maze_a[cur.r][cur.c].checkBump(Direction.RIGHT)) && maze_a[cur.r][cur.c + 1].Safe) {// 우
																														// cur.c
																														// <=
																														// (n
																														// -
																														// 2)
						prev.c = cur.c;
						cur.c++;
						printMaze();
						printNow();

					}
				}
			}

		} else {
			System.out.println("아직 Gold를 획득하지 못했습니다.");
		}

		robot.climb();
	}

	static void printMaze() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				char charToPrint = '-';
				if (cur.r == i && cur.c == j) // agent
					charToPrint = '*';
				else if (maze[i][j].hasPit)
					charToPrint = 'O';
				else if (maze[i][j].hasWumpus)
					charToPrint = 'X';
				else if (maze[i][j].hasGold)
					charToPrint = '$';

				System.out.print(charToPrint + "\t");
			}
			System.out.println();
			System.out.println();
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
		}catch (Exception e) {
			
		}
		
		
	}
}
