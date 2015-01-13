package master.keyEx.models;

import java.util.ArrayList;

public class Corpus {

	private int docNEng = 0;
	private int docNGer = 0;
	private ArrayList<PDF> pdfList = new ArrayList<PDF>();
	// Merge Both
	// private ArrayList<Category> globalCategory = new ArrayList<Category>();
	private ArrayList<CategoryCatalog> globalCategoryCatalog = new ArrayList<CategoryCatalog>();

	public ArrayList<CategoryCatalog> getGlobalCategoryCatalog() {
		return globalCategoryCatalog;
	}

	public void setGlobalCategoryCatalog(
			ArrayList<CategoryCatalog> globalCategoryCatalog) {
		this.globalCategoryCatalog = globalCategoryCatalog;
	}

	// NOT SURE IF ITS RIGHT
	public void calculateIdf() {
		ArrayList<WordOcc> words = null;
		// new
		ArrayList<WordOcc> wordes = null;
		String language = null;
		for (PDF doc : pdfList) {
			words = doc.getWordOccList();
			language = doc.getLanguage();
			for (WordOcc word : words) {
				// so words are not considered multiple times
				if (word.getKeyinPDF() == 0) {
					for (PDF currdoc : pdfList) {
						// words overwrite?
						if (word.getWord().getWord().equals("future")) {
							String test = "0";
						}
						if (currdoc.getLanguage().equals(language)) {
							wordes = currdoc.getWordOccList();
							for (int ii = 0; ii < wordes.size(); ii++) {
								if (wordes.get(ii).getWord().getWord()
										.contains(word.getWord().getWord())) {
									word.incKeyinPDF();
									break;
								}
							}
						}
					}
				}
			}
		}
		for (PDF doc : pdfList) {
			words = doc.getWordOccList();
			for (WordOcc word : words) {
				String pdfLanguage = doc.getLanguage();
				word.setIdf(TFIDF.calcIDF((double) getDocN(pdfLanguage),
						(double) word.getKeyinPDF()));
			}
		}
		// this.idf = Math.log10(docN/pdfList);
	}

	// TODO DONE LANGUAGE ADDING
	public int getDocN(String language) {
		if (language.equals("de")) {
			return docNGer;
		} else if (language.equals("en")) {
			return docNEng;
		}
		return docNEng;

	}

	public void setDocN(int docN, String language) {
		if (language.equals("de")) {
			this.docNGer = docN;
		} else if (language.equals("en")) {
			this.docNEng = docN;
		}
	}

	public void incDocN(String language) {
		if (language.equals("de")) {
			this.docNGer++;
		} else if (language.equals("en")) {
			this.docNEng++;
		}
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
			// System.out.println(ii);
			// ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			// for (int jj = 0; jj < words.size(); jj++) {
			// System.out.println(words.get(jj).getWord().getWord()
			// + "- TFIDF: " + words.get(jj).getTfidf() + " IDF: "
			// + words.get(jj).getIdf() + " TF: "
			// + words.get(jj).getTf() + " wordocc: "
			// + words.get(jj).getOcc());
			// }
			// System.out
			// .println("______________________________________________________________");
		}
		return pdfList;

	}

	public ArrayList<PDF> filterPDFTDIDF(ArrayList<PDF> pdfList2, double level) {
		for (int ii = 0; ii < pdfList.size(); ii++) {
			ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			ArrayList<WordOcc> test = new ArrayList();

			for (int jj = 0; jj < words.size(); jj++) {

				if (words.get(jj).getTfidf() > level) {
					test.add(words.get(jj));
				}

			}
			pdfList.get(ii).setWordOcc(test);
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
					found = true;
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

	// TODO CONSIDER DUPLICATE PAPER
	private void addCategoryWords(int position, ArrayList<WordOcc> wordOccList) {
		ArrayList<WordOcc> keys = this.globalCategoryCatalog.get(position)
				.getKeywordList();
		boolean found = false;
		int catocc = 0;
		for (WordOcc word : wordOccList) {
			for (WordOcc gkey : keys) {
				if (word.getWord().getWord().equals(gkey.getWord().getWord())) {
					found = true;
					// gkey.setOcc(gkey.getOcc() + word.getOcc());
					// needs to be relative to word number
					gkey.setOcc(gkey.getOcc() + word.getOcc());
					catocc = catocc + gkey.getOcc();
					// System.out.println("Good WORD");
					break;
				}
			}
			if (!found) {
				if (word == null) {
					int test = 0;
				}
				// WordOcc words = new WordOcc(word);
				this.globalCategoryCatalog.get(position).getKeywordList()
						.add(word);
				catocc = catocc + word.getOcc();
			} else {
				found = false;
			}
		}
		this.globalCategoryCatalog.get(position).incTotalW(catocc);

	}

	public void calculateCatTFIDF() {
		for (int ii = 0; ii < this.pdfList.size(); ii++) {
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
		calculateCatIdf();
		calculateCatTDIF();
	}

	private void calculateCatTDIF() {
		for (int ii = 0; ii < this.globalCategoryCatalog.size(); ii++) {
			this.globalCategoryCatalog.get(ii).calculateTF_IDF();
			// System.out.println(ii);
			// ArrayList<WordOcc> words = this.globalCategoryCatalog.get(ii)
			// .getKeywordList();
			// for (int jj = 0; jj < words.size(); jj++) {
			// if (words.get(jj).getCatTFIDF() > 0) {
			// System.out.println("CATEGORY:"
			// + this.globalCategoryCatalog.get(ii).getCategory()
			// .getTitle() + " "
			// + words.get(jj).getCatTFIDF() + ":"
			// + words.get(jj).getWord().getWord());
			// }
			// }
			// System.out
			// .println("______________________________________________________________");
		}
	}

	// TODO consider occurence when rating - and norm value
	private PDF calculateRelPDF(PDF current, int counter, CategoryCatalog catcat) {
		for (WordOcc pdfword : current.getWordOccList()) {
			for (WordOcc word : catcat.getKeywordList()) {
				if (pdfword.getWord().getWord()
						.equals(word.getWord().getWord())) {
					current.getGenericKeywords().get(counter)
							.incwOcc(word.getOcc());
					break;
				}
			}
		}
		return current;
	}

	public void calculateCatIdf() {
		ArrayList<WordOcc> words = null;
		ArrayList<WordOcc> wordes = null;
		for (CategoryCatalog doc : this.globalCategoryCatalog) {
			words = doc.getKeywordList();
			for (WordOcc word : words) {
				// NO NEGATIVE VALUES
				if (word.getKeyinCat() == 0) {
					for (CategoryCatalog currdoc : this.globalCategoryCatalog) {
						wordes = currdoc.getKeywordList();
						for (int ii = 0; ii < wordes.size(); ii++) {
							if ((wordes.get(ii).getWord().getWord().equals(word
									.getWord().getWord()))
									&& (!word.isCatRet())) {

								word.incKeyinCat();

								// System.out.println("Corpus:"+currdoc.getCategory().getTitle()
								// + "->" + word.getWord().getWord());

								break;
							}
						}
					}
					// TODO SOLVE IN NORMAL FASCHION
					word.setCatRet(true);
				}
			}
		}
		for (CategoryCatalog doc : this.globalCategoryCatalog) {
			words = doc.getKeywordList();
			for (WordOcc word : words) {
				word.setCatIDF(TFIDF.calcIDF(
						// have to consider also occurence not only size
						(double) this.globalCategoryCatalog.size(),
						(double) word.getKeyinCat()));
			}
		}
		// this.idf = Math.log10(docN/pdfList);
	}

	// preparation to calculate cat -> normalized result
	public void calculateRel() {
		for (int ii = 0; ii < this.pdfList.size(); ii++) {
			ArrayList<Category> pdfcat = this.pdfList.get(ii)
					.getGenericKeywords();
			for (WordOcc word : pdfList.get(ii).getWordOccList()) {
				for (int counter = 0; counter < pdfcat.size(); counter++) {
					for (Category current : word.getWord().getCategory()) {
						if (current.getTitle().equals(
								pdfcat.get(counter).getTitle())) {
							this.pdfList.get(ii).getGenericKeywords()
									.get(counter)
									.incRelevance(word.getCatTFIDF());
							;
						}
					}
				}
			}
		}

	}

	public void filterCatTFIDF(double level) {
		for (int ii = 0; ii < globalCategoryCatalog.size(); ii++) {
			ArrayList<WordOcc> words = globalCategoryCatalog.get(ii)
					.getKeywordList();
			ArrayList<WordOcc> test = new ArrayList();

			for (int jj = 0; jj < words.size(); jj++) {

				if (words.get(jj).getTfidf() > level) {
					test.add(words.get(jj));
				}

			}
			globalCategoryCatalog.get(ii).setKeywordList(test);
		}

	}
}
