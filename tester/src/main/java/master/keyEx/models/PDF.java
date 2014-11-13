package master.keyEx.models;

import java.util.ArrayList;

public class PDF {
	private ArrayList<WordOcc> wordOcc;
	private String language;
	private ArrayList<String> genericKeywords;

	public PDF() {
		// TODO Auto-generated constructor stub
	}

	public PDF(ArrayList<WordOcc> words, String language) {
		this.wordOcc = words;
		this.language = language;
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

}
