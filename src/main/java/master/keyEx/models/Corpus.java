package master.keyEx.models;

import java.util.ArrayList;

public class Corpus {
	

	private int docN=0;
	private ArrayList<PDF> pdfList = new ArrayList<PDF>();

	public void calculateIdf() {
		ArrayList<WordOcc> words = null;
		Boolean found = false;
	    for(PDF doc : pdfList){
	    	words = doc.getWordOccList();
	    	for(WordOcc word:words){
	    		for(PDF currdoc : pdfList){
	    			words=currdoc.getWordOccList();
	    			for(int ii=0;ii<words.size();ii++){
	    				if(words.get(ii).getWord().getWord().contains(word.getWord().getWord())){
	    					word.incKeyinPDF();
	    					break;
	    				}
	    			}
	    		}
	    	}
	    }
	    for(PDF doc : pdfList){
	    	words = doc.getWordOccList();
	    	for(WordOcc word:words){
	    		word.setIdf(TFIDF.calcIDF((double)docN,(double)word.getKeyinPDF()));
	    	}
	    }
		//this.idf = Math.log10(docN/pdfList);
	}

	public int getDocN() {
		return docN;
	}
	public void setDocN(int docN) {
		this.docN = docN;
	}
	
	public void incDocN() {
		this.docN++;
	}
	public Corpus() {
		// TODO Auto-generated constructor stub
	}
	public ArrayList<PDF> getPdfList() {
		return pdfList;
	}
	public void setPdfList(ArrayList<PDF> pdfList) {
		this.pdfList = pdfList;
	}

	public ArrayList<PDF> calculateTD_IDF(ArrayList<PDF> pdfList) {
		for (int ii = 0; ii < pdfList.size(); ii++) {
			pdfList.get(ii).calculateTF_IDF();
			System.out.println(ii);
			ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			for (int jj = 0; jj < words.size(); jj++) {
				System.out.println(words.get(jj).getTfidf() + ":"
						+ words.get(jj).getWord().getWord());
			}
			System.out
					.println("______________________________________________________________");
		}
		return pdfList;
		
	}

	public ArrayList<PDF> filterPDFTDIDF(ArrayList<PDF> pdfList2,double level) {
		for (int ii = 0; ii < pdfList.size(); ii++) {
			ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			for (int jj = 0; jj < words.size(); jj++) {
				ArrayList<WordOcc> filtwords = new ArrayList<WordOcc>();
				if(words.get(jj).getTfidf()>level){
					filtwords.add(words.get(jj));
				}
				System.out.println(filtwords.get(jj));
				pdfList.get(ii).setWordOcc(filtwords);
			}
		}
		return pdfList;
	}
}
