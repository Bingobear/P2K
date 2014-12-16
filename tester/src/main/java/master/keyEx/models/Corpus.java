package master.keyEx.models;

import java.util.ArrayList;

public class Corpus {

	private int docN = 0;
	private ArrayList<PDF> pdfList = new ArrayList<PDF>();
	// Merge Both
	private ArrayList<Category> globalCategory = new ArrayList<Category>();
	private ArrayList<CategoryCatalog> globalCategoryCatalog = new ArrayList<CategoryCatalog>();

	public void calculateIdf() {
		ArrayList<WordOcc> words = null;
		Boolean found = false;
		for (PDF doc : pdfList) {
			words = doc.getWordOccList();
			for (WordOcc word : words) {
				for (PDF currdoc : pdfList) {
					words = currdoc.getWordOccList();
					for (int ii = 0; ii < words.size(); ii++) {
						if (words.get(ii).getWord().getWord()
								.contains(word.getWord().getWord())) {
							word.incKeyinPDF();
							break;
						}
					}
				}
			}
		}
		for (PDF doc : pdfList) {
			words = doc.getWordOccList();
			for (WordOcc word : words) {
				word.setIdf(TFIDF.calcIDF((double) docN,
						(double) word.getKeyinPDF()));
			}
		}
		// this.idf = Math.log10(docN/pdfList);
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

	public ArrayList<PDF> filterPDFTDIDF(ArrayList<PDF> pdfList2, double level) {
		for (int ii = 0; ii < pdfList.size(); ii++) {
			ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			for (int jj = 0; jj < words.size(); jj++) {
				ArrayList<WordOcc> filtwords = new ArrayList<WordOcc>();
				if (words.get(jj).getTfidf() > level) {
					filtwords.add(words.get(jj));
				}
				System.out.println(filtwords.get(jj));
				pdfList.get(ii).setWordOcc(filtwords);
			}
		}
		return pdfList;
	}

	// TODO CALCULATE CATEGORY SCORING
	// Possibly use only words with specific tdif
	public void associateWordswithCategory(PDF pdf) {
		boolean found = false;
		for (Category cat : pdf.getGenericKeywords()) {
			for (int counter = 0; counter < this.globalCategoryCatalog.size(); counter++) {
				if (cat.getTitle().equals(
						this.globalCategoryCatalog.get(counter).getCategory()
								.getTitle())) {
					found=true;
					addCategoryWords(counter, pdf.getWordOccList());
					break;
				}
			}
			if (!found) {
				this.globalCategoryCatalog.add(new CategoryCatalog(cat, pdf
						.getWordOccList()));
			} else {
				found = false;
			}
		}
	}

	private void addCategoryWords(int position, ArrayList<WordOcc> wordOccList) {
		ArrayList<WordOcc> keys = this.globalCategoryCatalog.get(position)
				.getKeywordList();
		boolean found = false;
		for (WordOcc word : wordOccList) {
			for (WordOcc gkey : keys) {
				if (word.getWord().getWord().equals(gkey.getWord().getWord())) {
					found = true;
					//gkey.setOcc(gkey.getOcc() + word.getOcc());
					//needs to be relative to word number
					gkey.setOcc(gkey.getOcc() + word.getOcc());
					System.out.println("Good WORD");
					break;
				}
			}
			if (!found) {
				this.globalCategoryCatalog.get(position).getKeywordList()
						.add(word);
			} else {
				found = false;
			}
		}

	}

	public void addGlobalCat(ArrayList<Category> keywords) {
		boolean found = false;
		for (int ii = 0; ii < keywords.size(); ii++) {
			for (int jj = 0; jj < getGlobalCategory().size(); jj++) {
				Category current = this.globalCategory.get(jj);
				if (keywords.get(ii).getTitle().equals(current.getTitle())) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.globalCategory.add(keywords.get(ii));
			} else {
				found = false;
			}
		}
	}

	public ArrayList<Category> getGlobalCategory() {
		return globalCategory;
	}

	public void setGlobalCategory(ArrayList<Category> globalCategory) {
		this.globalCategory = globalCategory;
	}

	public void calculateCatRele() {
		for (int ii = 0;ii<this.pdfList.size();ii++) {
			PDF current = this.pdfList.get(ii);
			for (int counter = 0; counter < current.getGenericKeywords().size(); counter++) {
				for (CategoryCatalog catcat : this.globalCategoryCatalog) {
					if (catcat
							.getCategory()
							.getTitle()
							.equals(current.getGenericKeywords().get(counter)
									.getTitle())) {
						current = calculateRelPDF(current, counter, catcat);
					}
				}
			}
			this.pdfList.set(ii, current);
		}

	}

	//TODO consider occurence when rating - and norm value
	private PDF calculateRelPDF(PDF current, int counter, CategoryCatalog catcat) {
		for (WordOcc pdfword : current.getWordOccList()) {
			for (WordOcc word : catcat.getKeywordList()) {
				if(pdfword.getWord().getWord().equals(word.getWord().getWord())){
					current.getGenericKeywords().get(counter).incRelevance(word.getOcc());
					break;
				}
			}
		}
		return current;
	}
}
