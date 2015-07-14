package master.keyEx.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Corpus class Interface to perform ranking algorithm (tfidf) and (tficf)
 * 
 * @author Simon
 *
 */
public class Corpus {

	private int docNEng = 0;
	private int docNGer = 0;
	private ArrayList<PDF> pdfList = new ArrayList<PDF>();
	private ArrayList<CategoryCatalog> globalCategoryCatalog = new ArrayList<CategoryCatalog>();

	public ArrayList<CategoryCatalog> getGlobalCategoryCatalog() {
		return globalCategoryCatalog;
	}

	public void setGlobalCategoryCatalog(
			ArrayList<CategoryCatalog> globalCategoryCatalog) {
		this.globalCategoryCatalog = globalCategoryCatalog;
	}

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
				word.setIdf(AlgorithmUtil.calcIDF(
						(double) getDocN(pdfLanguage),
						(double) word.getKeyinPDF()));
			}
		}
	}

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
			/*
			 * ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList(); for
			 * (int jj = 0; jj < words.size(); jj++) {
			 * System.out.println(words.get(jj).getWord().getWord() +
			 * "- TFIDF: " + words.get(jj).getTfidf() + " IDF: " +
			 * words.get(jj).getIdf() + " TF: " + words.get(jj).getTf() +
			 * " wordocc: " + words.get(jj).getOcc()); } System.out .println(
			 * "______________________________________________________________"
			 * );
			 */}
		return pdfList;

	}

	/**
	 * Removes words from each pdf which tfidf is not above the defined level
	 * 
	 * @param pdfList
	 * @param level
	 * @return
	 */
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

	/**
	 * Associates pdfwords with category (preparation for tficf)
	 * 
	 * @param pdf
	 */
	public void associateWordswithCategory(PDF pdf) {
		boolean found = false;
		for (Category cat : pdf.getGenericKeywords()) {
			for (int counter = 0; counter < this.globalCategoryCatalog.size(); counter++) {
				String wordCat = cat.getNormtitle();
				String wordGlobal = this.globalCategoryCatalog.get(counter)
						.getCategory().getNormtitle();
				int ld = AlgorithmUtil.LevenshteinDistance(wordCat, wordGlobal);
				double sim = 0;
				if (wordCat.length() > wordGlobal.length()) {
					sim = AlgorithmUtil.calculateWordSim(wordCat, ld);
				} else {
					sim = AlgorithmUtil.calculateWordSim(wordGlobal, ld);
				}
				if (sim <= 0.2) {
					found = true;
					try {
						writelog(wordCat, wordGlobal, pdf.getTitle());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cat.setAssociatedGCAT(this.getGlobalCategoryCatalog()
							.get(counter).getCategory().getNormtitle());
					addCategoryWords(counter, pdf.getWordOccList());
					break;
				}
			}
			if (!found) {
				cat.setAssociatedGCAT(cat.getNormtitle());
				this.globalCategoryCatalog.add(new CategoryCatalog(cat, pdf
						.getWordOccList()));
			} else {
				found = false;
			}
		}
	}

	private void writelog(String wordCat, String wordGlobal, String string)
			throws IOException {
		String timeLog = "MatchesLogFile";
		File logFile = new File(timeLog);

		// This will output the full path where the file will be written to...
		System.out.println(logFile.getCanonicalPath());

		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(logFile, true));
		writer.write(string + " has matches " + wordCat + " with gCat"
				+ wordGlobal);
		writer.newLine();
		writer.write("_________________________________________________________");
		writer.newLine();
		writer.close();

	}

	/**
	 * Calculates occurences of a word within each category (preparation TFICF)
	 * 
	 * @param position
	 * @param wordOccList
	 */
	private void addCategoryWords(int position, ArrayList<WordOcc> wordOccList) {
		ArrayList<WordOcc> keys = this.globalCategoryCatalog.get(position)
				.getKeywordList();
		boolean found = false;
		int catocc = 0;
		for (WordOcc word : wordOccList) {
			for (WordOcc gkey : keys) {
				if (word.getWord().getWord().equals(gkey.getWord().getWord())) {
					found = true;
					gkey.setOcc(gkey.getOcc() + word.getOcc());
					catocc = catocc + gkey.getOcc();
					break;
				}
			}
			if (!found) {
				if (word == null) {
					int test = 0;
				}
				this.globalCategoryCatalog.get(position).getKeywordList()
						.add(word);
				catocc = catocc + word.getOcc();
			} else {
				found = false;
			}
		}
		this.globalCategoryCatalog.get(position).incTotalW(catocc);

	}

	/**
	 * Calculates TFIDF for every words in each pdfs prepares its values (count
	 * occ of words in cat)
	 * 
	 */
	public void initializeTFICFCalc() {
		for (int ii = 0; ii < this.pdfList.size(); ii++) {
			PDF current = this.pdfList.get(ii);
			for (int counter = 0; counter < current.getGenericKeywords().size(); counter++) {
				for (CategoryCatalog catcat : this.globalCategoryCatalog) {
					if (catcat
							.getCategory()
							.getTitle()
							.equals(current.getGenericKeywords().get(counter)
									.getTitle())) {
						current = calculateCatTF(current, counter, catcat);
					}
				}
			}
			this.pdfList.set(ii, current);
		}
		calculateICF();
		calculateTFICF();
	}

	/**
	 * Calculates TFICF for every word for each category
	 * 
	 */
	private void calculateTFICF() {
		for (int ii = 0; ii < this.globalCategoryCatalog.size(); ii++) {
			this.globalCategoryCatalog.get(ii).calculateTF_IDF();
//			 System.out.println(ii);
//			 ArrayList<WordOcc> words = this.globalCategoryCatalog.get(ii)
//			 .getKeywordList();
//			 for (int jj = 0; jj < words.size(); jj++) {
//			 if (words.get(jj).getCatTFIDF() > 0) {
//			 System.out.println("CATEGORY:"
//			 + this.globalCategoryCatalog.get(ii).getCategory()
//			 .getTitle() + " "
//			 + words.get(jj).getCatTFIDF() + ":"
//			 + words.get(jj).getWord().getWord());
//			 }
//			 }
//			 System.out
//			 .println("______________________________________________________________");
		}
	}

	/**
	 * Calculates catTF for each word in the given pdf
	 * 
	 * @param current
	 * @param counter
	 * @param catcat
	 * @return
	 */
	private PDF calculateCatTF(PDF current, int counter, CategoryCatalog catcat) {
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

	/**
	 * Calculates the icf part of tf-icf
	 * 
	 */
	public void calculateICF() {
		ArrayList<WordOcc> words = null;
		ArrayList<WordOcc> wordes = null;
		for (CategoryCatalog doc : this.globalCategoryCatalog) {
			words = doc.getKeywordList();
			for (WordOcc word : words) {
				if (word.getKeyinCat() == 0) {
					for (CategoryCatalog currdoc : this.globalCategoryCatalog) {
						wordes = currdoc.getKeywordList();
						for (int ii = 0; ii < wordes.size(); ii++) {
							if ((wordes.get(ii).getWord().getWord().equals(word
									.getWord().getWord()))
									&& (!word.isCatRet())) {

								word.incKeyinCat();

								break;
							}
						}
					}
					word.setCatRet(true);
				}
			}
		}
		for (CategoryCatalog doc : this.globalCategoryCatalog) {
			words = doc.getKeywordList();
			for (WordOcc word : words) {
				word.setCatIDF(AlgorithmUtil.calcIDF(
						// have to consider also occurence not only size
						(double) this.globalCategoryCatalog.size(),
						(double) word.getKeyinCat()));
			}
		}
	}

	/**
	 * Calculate keyword relevance for every PDF of the corpus (adding TFICF
	 * values of matching keywords from associated categories)
	 * 
	 */
	public void calculateAllPDFCatRel() {
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
							// try to normalize -> avoid problem
							this.pdfList.get(ii).getGenericKeywords()
									.get(counter).incNormAdd();
						}
					}
					this.pdfList.get(ii).getGenericKeywords().get(counter)
							.getRelevance();
				}
			}
			for (int counter = 0; counter < pdfcat.size(); counter++) {
				if (pdfcat.get(counter).getRelevance() == 0) {
					pdfcat.get(counter).setRelevance(0.00001);
				}
			}
		}

	}

	/**
	 * All TFICF values have to be above the defined leve, else remove word
	 * 
	 * @param level
	 */
	public void filterTFICF(double level) {
		for (int ii = 0; ii < globalCategoryCatalog.size(); ii++) {
			ArrayList<WordOcc> words = globalCategoryCatalog.get(ii)
					.getKeywordList();
			ArrayList<WordOcc> relevantWords = new ArrayList();

			for (int jj = 0; jj < words.size(); jj++) {

				if (words.get(jj).getTfidf() > level) {
					relevantWords.add(words.get(jj));
				}

			}
			globalCategoryCatalog.get(ii).setKeywordList(relevantWords);
		}

	}
}
