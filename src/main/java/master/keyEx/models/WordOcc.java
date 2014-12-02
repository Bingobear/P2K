package master.keyEx.models;

import master.keyEx.models.Words;

public class WordOcc {
	private Words word;
	private int occ;
	private double tf=0;
	private double tfidf = 0;
	private double idf=0;
	private int keyinPDF=0;
	
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

	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf;
	}

	public double getTfidf() {
		return tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}

	public int getKeyinPDF() {
		return keyinPDF;
	}

	public void incKeyinPDF() {
		this.keyinPDF++;
	}
	
	public void setKeyinPDF(int keyinPDF) {
		this.keyinPDF = keyinPDF;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}
}
