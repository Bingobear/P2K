package master.keyEx.models;

import java.util.ArrayList;

public class Words {
	private String word;
	private String stem;
	private String type;
	private ArrayList<Category> category;

	
	public Words() {
		// TODO Auto-generated constructor stub
	}
	
	public Words(String word, String stem, String type,ArrayList<Category> cat) {
		this.word = word;
		this.stem = stem;
		this.type = type;
		this.category = cat;
	}

	public Words(Words words) {
		this.word = words.word;
		this.stem = words.stem;
		this.type = words.type;
		this.category = words.category;
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

	public ArrayList<Category> getCategory() {
		return category;
	}

	public void setCategory(ArrayList<Category> category) {
		this.category = category;
	}


}
