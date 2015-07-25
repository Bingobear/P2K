package master.keyEx.models;

/**
 * Keyword model class
 * 
 * @author Simon Bruns
 *
 */

public class Keyword {
	private int score;
	private String word;
	private Category cat;
	private PDF pdf;
	
	public PDF getPdf() {
		return pdf;
	}

	public void setPdf(PDF pdf) {
		this.pdf = pdf;
	}

	public Category getCat() {
		return cat;
	}

	public void setCat(Category cat) {
		this.cat = cat;
	}

	public Keyword() {

	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}	
}
