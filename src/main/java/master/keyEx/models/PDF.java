package master.keyEx.models;

import java.util.ArrayList;

public class PDF {
	private ArrayList<WordOcc> wordOcc;
	private String language;
	private int wordcount;
	
	public int getWordcount() {
		return wordcount;
	}

	public void setWordcount(int wordcount) {
		this.wordcount = wordcount;
	}

	private ArrayList<String> genericKeywords;
	private String firstPage;
	private int publicationID;


	
	public void calculateTF_IDF(){
		WordOcc word=null;
		for(int ii=0;ii<wordOcc.size();ii++){
			word=wordOcc.get(ii);
			double tf = (double)word.getOcc()/(double)wordcount;
			word.setTf(tf);
			double tfidf=(double)tf*(double)word.getIdf();
			word.setTfidf(tfidf);
		}
	}

	public PDF() {
		// TODO Auto-generated constructor stub
	}

	public PDF(ArrayList<WordOcc> words, String language, int wordcount) {
		this.wordOcc = words;
		this.wordcount = wordcount;
		this.language = language;
	}

	public PDF(ArrayList<WordOcc> words, String lang, int wordcount,
			String titlePage) {
		this.wordOcc = words;
		this.wordcount = wordcount;
		this.language = lang;
		this.firstPage = titlePage;
	}

	public ArrayList<WordOcc> getWordOccList() {
		return wordOcc;
	}

	public void addWordOcc(WordOcc word) {
		this.addWordOcc(word);
	}

	public WordOcc getWordOcc(String word) {
		for (int ii = 0; ii < this.wordOcc.size(); ii++) {
			if (wordOcc.get(ii).getWord().getWord().contains(word)) {
				return wordOcc.get(ii);
			}
		}
		return null;
	}

	public void setWordOcc(ArrayList<WordOcc> wordocc) {
		this.wordOcc = wordocc;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArrayList<String> getGenericKeywords() {
		return genericKeywords;
	}

	public void setGenericKeywords(ArrayList<String> genericKeywords) {
		this.genericKeywords = genericKeywords;
	}

	public String getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	public int getPublicationID() {
		return publicationID;
	}

	public void setPublicationID(int publicationID) {
		this.publicationID = publicationID;
	}

}
