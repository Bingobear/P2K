package master.keyEx.models;

import master.keyEx.models.Words;

/**
 * Wordocc model class
 * 
 * @author Simon Bruns
 *
 */

public class WordOcc {
	private Words word;
	private int occ;
	private double tf = 0;
	private double tfidf = 0;
	private double idf = 0;
	private int keyinPDF = 0;
	private int keyinCat = 0;
	//before 1
	private int catRel = 0;
	private double catIDF = 0;
	private double catTF = 0;
	private double catTFIDF = 0;
	private boolean catRet = false;

	WordOcc() {

	}

	public WordOcc(Words word, int occ) {
		this.word = word;
		this.occ = occ;
	}



	// TODO DELETE PDF STUFF
	public WordOcc(WordOcc another) {
		this.word = new Words(word);
		this.occ = another.occ;
		this.tf = another.tf;
		this.tfidf = another.tfidf;
		this.idf = another.idf;
		this.keyinPDF = another.keyinPDF;
		this.keyinCat = another.keyinCat;
		this.catRel = another.catRel;
		this.catIDF = another.catIDF;
		this.catTF = another.catTF;
		this.catTFIDF = another.catTFIDF;
		this.catRet = another.catRet;
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

	public int getCatRel() {
		return catRel;
	}

	public void setCatRel(int catRel) {
		this.catRel = catRel;
	}

	public double getCatIDF() {
		return catIDF;
	}

	public void setCatIDF(double d) {
		this.catIDF = d;
	}

	public double getCatTFIDF() {
		return catTFIDF;
	}

	public void setCatTFIDF(double tfidf2) {
		this.catTFIDF = tfidf2;
	}

	public int getKeyinCat() {
		return keyinCat;
	}

	public void setKeyinCat(int keyinCat) {
		this.keyinCat = keyinCat;
	}

	public void incKeyinCat() {
		this.keyinCat++;
	}

	public double getCatTF() {
		return catTF;
	}

	public void setCatTF(double catTF) {
		this.catTF = catTF;
	}

	public boolean isCatRet() {
		return catRet;
	}

	public void setCatRet(boolean catRet) {
		this.catRet = catRet;
	}
}
