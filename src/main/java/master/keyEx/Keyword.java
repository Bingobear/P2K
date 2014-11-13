package master.keyEx;

public class Keyword {
	int occ = 0;
	String word;
	Keyword() {

	}
	
	Keyword(int occ, String word) {
		this.occ = occ;
		this.word = word;
	}
	
	public void increaseOcc(){
		this.occ=+1;
	}
}
