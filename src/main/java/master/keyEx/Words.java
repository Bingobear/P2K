package master.keyEx;

public class Words {
	String word;
	String stem;
	String type;
	
	public Words() {
		// TODO Auto-generated constructor stub
	}
	
	Words(String word, String stem, String type) {
		this.word = word;
		this.stem = stem;
		this.type = type;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
