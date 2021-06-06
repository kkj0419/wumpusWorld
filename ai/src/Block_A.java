//package aiproj;

public class Block_A {
    public static final int NOT_PRESENT = 0; 
    public static final int UNSURE = 1;
    public static final int DEFAULT=-1;
    public static final int PRESENT=10;
    
    
    public boolean visited=false;
//    public boolean Stench, Breeze, Glitter;
    
    public boolean[] Bump= {false,false,false,false};
//    public boolean[] Bump;
    
    public int pitStatus = DEFAULT;
    public int wumpusStatus = DEFAULT;
    
    public boolean Safe=false;
    
    public void updateSafe() {
    	if(pitStatus==NOT_PRESENT && wumpusStatus==NOT_PRESENT)
    		Safe=true;
    	else
    		Safe=false;
    }
    
	public void setBump(int i,boolean value) {
		Bump[i]=value;
	}
    
    public boolean checkBump(int direction) {
    	return Bump[direction];
    }
    
    public void setSafe() {
    	pitStatus=NOT_PRESENT;
    	wumpusStatus=NOT_PRESENT;
    }
//    public boolean isChecked() {
//    	if(Safe==true) {
//    		return 
//    	}
//    }
    
}



