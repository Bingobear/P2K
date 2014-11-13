package master.keyEx.models;

import master.keyEx.models.Words;

public class WordOcc {
	private Words word;
	private int occ;
	
	WordOcc() {

	}

	public WordOcc(Words word, int occ) {
		this.word = word;
		this.occ = occ;
	}
	
	public Words getWord() {
		return word;
	}

	public void setWord(Words word) {
		this.word = word;
	}

	public int getOcc() {
		return occ;
	}

	public void setOcc(int occ) {
		this.occ = occ;
	}
}
