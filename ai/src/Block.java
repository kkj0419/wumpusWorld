
public class Block {

	public boolean hasGold;
	public boolean hasPit, hasWumpus;

	public boolean Stench,Breeze,Glitter;
    
	public boolean Safe=false;
	
	//[right,down,left,up]
	public boolean Bump[]= {false,false,false,false};
	
	public void setBump(int i,boolean value) {
		Bump[i]=value;
	}
	
    public boolean checkBump(int direction) {
    	return Bump[direction];
    }
    
}
