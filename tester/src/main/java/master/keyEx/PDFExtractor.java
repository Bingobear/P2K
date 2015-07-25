package master.keyEx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import master.keyEx.models.*;

import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Main Text Mining Class
 * 
 * @author Simon Bruns
 *
 */
public class PDFExtractor {

	/**
	 * PDF Extractor
	 * 
	 * @throws IOException
	 */
	private String titlePage;
	private int catnumb;
	private int pagenumber;
	private int endK = 0;
	private String language;
	private ArrayList<Category> keywords = new ArrayList<Category>();

	public ArrayList<Category> getKeywords() {
		return keywords;
	}

	public void setKeywords(ArrayList<Category> keywords) {
		this.keywords = keywords;
	}

	private int wordcount = 0;

	public int getWordcount() {
		return wordcount;
	}

	PDFExtractor() {

	}

	PDFExtractor(String lang) {
		this.language = lang;
	}

	public void setLang(String lang) {
		this.language = lang;
	}

	public String getLang() {
		return this.language;
	}

	/**
	 * Converts x pages (end-start) pdf to String
	 * 
	 * @param pdfStripper
	 * @param pdDoc
	 * @param start
	 *            (starting page)
	 * @param end
	 *            (ending page)
	 * @return
	 * @throws IOException
	 */
	public String parsePdftoString(PDFTextStripper pdfStripper,
			PDDocument pdDoc, int start, int end) throws IOException {

		pdfStripper.setStartPage(start);
		pdfStripper.setEndPage(end);
		String parsedText = pdfStripper.getText(pdDoc);
		System.out.println("pages: " + start + "-" + end + " parsed");
		return parsedText;
	}

	/**
	 * Tokenizes given String
	 * 
	 * @param parsedText
	 * @return string[]
	 * 
	 */
	public String[] generalToken(String parsedText) {
		Tokenizer _tokenizer = null;

		InputStream modelIn = null;
		String[] tokens = null;
		try {
			// Loading tokenizer model
			if (this.getLang().equals("en")) {
				modelIn = getClass().getResourceAsStream("/eng/en-token.bin");
			} else {
				modelIn = getClass().getResourceAsStream("/ger/de-token.bin");
			}
			final TokenizerModel tokenModel = new TokenizerModel(modelIn);
			modelIn.close();

			_tokenizer = new TokenizerME(tokenModel);
			tokens = _tokenizer.tokenize(parsedText);
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}
		return tokens;
	}

	/**
	 * Creates the SentenceDetector object (utilizes gloabal language to refer
	 * to resource lang)
	 * 
	 * @return
	 */
	public SentenceDetector sentencedetect() {

		SentenceDetector _sentenceDetector = null;

		InputStream modelIn = null;
		try {
			// Loading sentence detection model
			if (this.getLang().equals("en")) {
				modelIn = getClass().getResourceAsStream("/eng/en-sent.bin");
			} else {
				modelIn = getClass().getResourceAsStream("/ger/de-sent.bin");
			}

			final SentenceModel sentenceModel = new SentenceModel(modelIn);
			modelIn.close();

			_sentenceDetector = new SentenceDetectorME(sentenceModel);

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}
		return _sentenceDetector;
	}

	/**
	 * Converts text string to token array string[] 
	 * 
	 * @param parsedText
	 * @return
	 */
	public String[] getTokenPM(String parsedText) {
		SentenceDetector sentdetector = sentencedetect();
		String[] sentence = sentdetector.sentDetect(parsedText);
		ArrayList<String> tokensA = new ArrayList<String>();
		for (int ii = 0; ii < sentence.length; ii++) {
			String[] tokenSen = generalToken(sentence[ii]);
			for (int jj = 0; jj < tokenSen.length; jj++) {
				tokensA.add(tokenSen[jj]);
			}
		}
		String[] tokens = new String[tokensA.size()];
		for (int ii = 0; ii < tokensA.size(); ii++) {
			tokens[ii] = tokensA.get(ii);

		}
		return tokens;
	}

	/**
	 * Extracts tokens from a given text - sums sup sentence detection and
	 * tokenization
	 * 
	 * @param parsedText
	 * @return string[]
	 */
	public String[] getToken(String parsedText) {
		SentenceDetector sentdetector = sentencedetect();
		String[] sentence = sentdetector.sentDetect(parsedText);
		ArrayList<String> tokensA = new ArrayList<String>();
		String help = "";
		for (int ii = 0; ii < sentence.length; ii++) {
			String[] tokenSen = generalToken(sentence[ii]);
			for (int jj = 0; jj < tokenSen.length; jj++) {
				help = tokenSen[jj].replaceAll("\\W", "");

				if ((!help.isEmpty()) && (help.length() > 2)) {
					tokensA.add(tokenSen[jj]);
				} else if ((help.equals("-")) && (jj + 1 < tokenSen.length)) {
					System.out.println(tokenSen[jj]);
					String tokencomb = tokensA.get(tokensA.size() - 1) + "-"
							+ tokenSen[jj + 1];
					jj++;
					tokensA.add(tokencomb);
					// System.out.println("NEW TOKEN"+tokensA.get(tokensA.size()-1));

				}

			}
		}
		String[] tokens = new String[tokensA.size()];
		for (int ii = 0; ii < tokensA.size(); ii++) {
			tokens[ii] = tokensA.get(ii);

		}
		return tokens;
	}

	/**
	 * Creates posttag array for a given text
	 * 
	 * @param text
	 * @return string[]
	 */
	public String[] posttags(String[] text) {
		POSTaggerME posttagger = createposttagger();
		String[] result = posttagger.tag(text);
		return result;

	}

	private POSTaggerME createposttagger() {

		InputStream modelIn = null;
		POSTaggerME _posTagger = null;
		try {
			// Loading tokenizer model
			if (this.getLang().equals("en")) {
				modelIn = getClass().getResourceAsStream(
						"/eng/en-pos-maxent.bin");
			} else {
				modelIn = getClass().getResourceAsStream(
						"/ger/de-pos-maxent.bin");
			}

			final POSModel posModel = new POSModel(modelIn);
			modelIn.close();

			_posTagger = new POSTaggerME(posModel);

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}
		return _posTagger;

	}

	/**
	 * Generates WordOcc array -> erasing duplicates and counting words
	 * 
	 * @param words
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<WordOcc> keyOcc(ArrayList<Words> words) {
		ArrayList<Words> keywords = new ArrayList<Words>();
		keywords = (ArrayList<Words>) words.clone();
		ArrayList<WordOcc> result = new ArrayList<WordOcc>();
		int arraySize = keywords.size();

		int counter = 0;
		int size = 0;
		while (arraySize > 0) {
			int count = 0;
			Words current = keywords.get(0);

			for (int ii = 0; ii < keywords.size(); ii++) {
				Words compare = keywords.get(ii);

				if (compare.getWord().equals(current.getWord())
						|| ((compare.getStem().equals(current.getStem())) && ((compare
								.getType().contains(current.getType()) || (current
								.getType().contains(compare.getType())))))) {
					keywords.remove(ii);
					count++;
					arraySize--;
				} else if (AlgorithmUtil.LevenshteinDistance(current.getWord(),
						compare.getWord()) < 0.2) {
					keywords.remove(ii);
					count++;
					arraySize--;
				}
				counter = ii;
				size = keywords.size();
			}
			result.add(new WordOcc(current, count));
		}
		return result;
	}

	/**
	 * Generate Word ArrayList -> filtering words type specific...
	 * 
	 * @param filter
	 * @param tokens
	 * @param modes
	 *            : 0-Noun, 1-Noun&Verb, 2-Noun&Adjective
	 * @return
	 */
	public ArrayList<Words> generateWords(String[] filter, String[] tokens,
			int mode) {
		// ArrayList<Integer> result = new ArrayList<Integer>();

		ArrayList<Words> result = new ArrayList<Words>();
		// for eng and german
		Stemmer stem = new Stemmer();
		String[] stemmedW = stem.stem(tokens, this.getLang());

		if (mode == 0) {
			for (int ii = 0; ii < filter.length; ii++) {
				if ((filter[ii].contains("NN"))) {
					if (!this.language.equals("de")) {
						// System.out.println(tokens[ii]);
						String text = tokens[ii].replaceAll("\\W", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					} else {
						// MAYBE SOLVES PROBLEM?TODO
						String text = tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					}
				}
			}
		} else if (mode == 1) {
			for (int ii = 0; ii < filter.length; ii++) {
				if ((filter[ii].contains("NN")) || (filter[ii].contains("VB"))) {
					if (!this.language.equals("de")) {
						// System.out.println(tokens[ii]);
						String text = tokens[ii].replaceAll("\\W", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					} else {
						// MAYBE SOLVES PROBLEM?TODO
						String text = tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					}
				}
			}
		} else if (mode == 2) {
			for (int ii = 0; ii < filter.length; ii++) {
				if ((filter[ii].contains("NN")) || (filter[ii].contains("JJ"))) {
					if (!this.language.equals("de")) {
						String text = tokens[ii].replaceAll("\\W", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					} else {
						String text = tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", "");
						if ((!text.isEmpty()) && (text.length() > 1)) {
							Words word = new Words(text, stemmedW[ii],
									filter[ii], this.keywords);
							result.add(word);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Parses PDFfile -> performs textmining: keyword-extraction,
	 * word-extraction
	 * 
	 * @param fileEntry
	 * @param first
	 * @param arrayList
	 * @param url2
	 * 
	 * @return ArrayList of words
	 * @throws LangDetectException
	 * @throws IOException
	 */
	public ArrayList<Words> parsePDFtoKey(File fileEntry, boolean first,
			ArrayList<PDF> arrayList) throws LangDetectException, IOException {
		ArrayList<Words> result = new ArrayList<Words>();

		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		setTitlePage(fileEntry.getName());

		PDFParser parser = new PDFParser(new FileInputStream(fileEntry));
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();

		pdDoc = new PDDocument(cosDoc);
		this.setPagenumber(pdDoc.getNumberOfPages());
		LangDetect lang = new LangDetect();

		for (int counter = 0; counter < pdDoc.getNumberOfPages(); counter += 5) {
			String parsedText = parsePdftoString(pdfStripper, pdDoc, counter,
					counter + 4);
			if (!((counter == 0) && (parsedText.length() < 50))) {
				setLang(lang.detect(parsedText, first));

				if (counter == 0) {
					System.out.println(getLang());
					if (first) {
						first = false;
					}

					this.setTitlePage(parsePdftoString(pdfStripper, pdDoc,
							counter, counter + 1));
					if (pdfExist(this.getTitlePage(), arrayList)) {
						break;
					}

					parsedText = parsedText.toLowerCase();
					String[] tokens = getTokenPM(parsedText);
					ArrayList<Category> keywords = getKeywordsFromPDF(tokens,
							fileEntry.getName());
					if (keywords.isEmpty()) {
						break;
					} else if ((keywords.size() < 4) || (keywords.size() > 8)) {
						if (this.titlePage.length() > endK) {
							this.titlePage = this.titlePage.substring(0,
									endK - 1);
						}
						this.setKeywords(keywords);
					}

					else {
						if (this.titlePage.length() > endK) {
							this.titlePage = this.titlePage.substring(0,
									endK - 1);
						}
						this.setKeywords(keywords);
					}
				}

				parsedText = parsedText.toLowerCase();

				// sentence detector -> tokenizer
				String[] tokens = getToken(parsedText);
				String[] filter = posttags(tokens);
				ArrayList<Words> words = generateWords(filter, tokens, 0);
				result.addAll(words);
				System.out.println("normal:" + tokens.length
						+ ", optimiertNouns:" + words.size());
				System.out.println("");
				wordcount = wordcount + tokens.length;
			} else {
				System.out.println("Bad Paper or Presentation");
				break;
			}
		}
		cosDoc.close();
		System.out.println("FINAL RESULT:optimiertNouns:" + result.size());
		return result;
	}

	/**
	 * Test if PDF is complete and not fragmented after parsing it
	 * 
	 * @param titlepage
	 * @param pdfList
	 * @return
	 */
	private boolean pdfExist(String titlepage, ArrayList<PDF> pdfList) {
		int sublength = 20;
		for (PDF compare : pdfList) {
			if (titlepage.length() < 20) {
				sublength = titlepage.length() - 1;
			}
			if (compare.getFirstPage().length() < sublength) {
				sublength = compare.getFirstPage().length() - 1;
			}
			if (compare.getFirstPage().substring(0, sublength)
					.equals(titlepage.substring(0, sublength))) {
				System.out.println("WORKS");
				return true;
			}
		}
		return false;
	}

	/**Extracts keywords from a given pdf (token string[]=tokenpm)
	 * @param tokens
	 * @param name
	 * @return
	 */
	private ArrayList<Category> getKeywordsFromPDF(String[] tokens, String name) {
		ArrayList<Category> keywords = new ArrayList<Category>();
		ArrayList<String> textPDF = new ArrayList<String>(Arrays.asList(tokens));
		int start = findKeyWStart(textPDF);
		endK = start;
		String seperator = "";
		if (start > 0) {

			if (textPDF.get(start).equals(":")) {
				start++;
			}
			if (textPDF.get(start).contains("keywords")) {
				String value = textPDF.get(start);
				textPDF.set(start, value.replaceAll("keywords", ""));
			}
			if (textPDF.get(start).contains("terms")) {
				String value = textPDF.get(start);
				textPDF.set(start, value.replaceAll("terms", ""));
			}
			textPDF = new ArrayList<String>(textPDF.subList(start,
					textPDF.size() - 1));
			int end = findKeyWEnd(textPDF);
			textPDF = new ArrayList<String>(textPDF.subList(0, end));
			seperator = findSep(textPDF);
			String akronom = "";
			String currKey = "";
			for (int ii = 0; ii < textPDF.size(); ii++) {
				if (textPDF.get(ii).equals(seperator)) {
					if (!akronom.isEmpty()) {
						currKey = currKey.replaceAll("(" + akronom + ")", "");
						currKey = currKey.replace(")", "");
					}
					currKey = currKey.replaceFirst("[^\\p{L}]+", "");
					currKey = currKey.trim();
					String normKey = currKey.replaceAll("[^\\p{L}]+", "");
					if ((!currKey.isEmpty()) && (!normKey.isEmpty())) {
						keywords.add(new Category(currKey, normKey, akronom));
					}
					akronom = "";
					currKey = "";

				} else if (textPDF.get(ii).contains("(")) {
					akronom = getAkronom(new ArrayList<String>(textPDF.subList(
							ii, textPDF.size())));
				} else {

					currKey = currKey + " " + textPDF.get(ii);

				}
			}
			if ((currKey.length() < 100) && (currKey.length() > 2)) {
				if ((currKey.charAt(currKey.length() - 1) == '1')
						&& (!currKey.isEmpty())) {
					currKey = currKey.replace("1", "");
				}
				if (!akronom.isEmpty()) {
					currKey = currKey.replaceAll("(" + akronom + ")", "");
					currKey = currKey.replace(")", "");
				}
				currKey = currKey.replaceFirst("[^\\p{L}]+", "");
				if (currKey.endsWith(".")) {
					currKey = currKey.substring(0, currKey.length() - 1);
				}
				currKey = currKey.trim();
				String normKey = currKey.replaceAll("[^\\p{L}]+", "");
				if ((!currKey.isEmpty()) && (!normKey.isEmpty())) {
					keywords.add(new Category(currKey, normKey, akronom));
				}
			}
		}

		setCatnumb(keywords.size());
		try {
			writelog(keywords, name, seperator, textPDF.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return keywords;
	}

	/**Extracts the akronom from a given string e.g. technology acceptance (ta) -> ta
	 * @param arrayList
	 * @return
	 */
	private String getAkronom(ArrayList<String> arrayList) {
		int end = getEndBracketPos(arrayList);
		String akro = "";
		int start = 0;
		if (arrayList.get(0).equals("(")) {
			start++;
		}
		for (int ii = start; ii < end; ii++) {
			akro = akro + arrayList.get(ii) + " ";
		}
		if (!(arrayList.get(end).equals(")"))) {
			akro = akro + arrayList.get(end);
		}

		akro = akro.replace("(", "");
		akro = akro.replace(")", "");
		akro.trim();
		return akro;
	}

	private int getEndBracketPos(ArrayList<String> arrayList) {
		for (int ii = 0; ii < arrayList.size(); ii++) {
			if (arrayList.get(ii).contains(")")) {
				return ii;
			}
		}
		return 0;
	}

	/** Writes a keyword log to protocol what keywords were extracted for which pdf
	 * @param keywords2
	 * @param name
	 * @param seperator
	 * @param size
	 * @throws IOException
	 */
	private void writelog(ArrayList<Category> keywords2, String name,
			String seperator, int size) throws IOException {
		String timeLog = "Keywords_log_new";
		File logFile = new File(timeLog);

		// This will output the full path where the file will be written to...
		System.out.println(logFile.getCanonicalPath());

		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(logFile, true));
		writer.write("Name: " + name + ", seperaotr: " + seperator
				+ ", Stringextract: " + size + ", lang:" + this.language);
		writer.newLine();
		for (int ii = 0; ii < keywords2.size(); ii++) {
			writer.write(keywords2.get(ii).getTitle() + " + "
					+ keywords2.get(ii).getNormtitle() + ", ");
			writer.newLine();
		}
		writer.write("_________________________________________________________");
		writer.newLine();
		writer.close();

	}

	/**extracts the separator (e.g. ",") from a given text (list)
	 * @param textPDF
	 * @return
	 */
	private String findSep(ArrayList<String> textPDF) {
		String[] seperatorC = { ",", ";", ".", "-" };
		int[] occ = new int[seperatorC.length];
		for (int ii = 0; ii < occ.length; ii++) {
			occ[ii] = 0;
		}
		int sep = 0;
		for (int ii = 0; ii < textPDF.size(); ii++) {
			for (int counter = 0; counter < occ.length; counter++) {
				if (textPDF.get(ii).equals(seperatorC[counter])) {
					occ[counter] = occ[counter] + 1;
					if (occ[sep] < occ[counter]) {
						sep = counter;
					}
				}
			}
		}
		return seperatorC[sep];
	}

	/**Identifies the probable start of the keyword enumeration
	 * @param textPDF
	 * @return
	 */
	private int findKeyWStart(ArrayList<String> textPDF) {
		int start = 0;
		if (textPDF.contains("keywords")) {
			start = textPDF.indexOf("keywords") + 1;

			System.out.println("Keyword found " + start);

		} else if (textPDF.contains("keyword")) {
			start = textPDF.indexOf("keyword") + 1;
		} else {
			start = getKeywPosition(textPDF);
			System.out.println("Keyword found within word" + start);
		}
		// TODO MAKE IT BEAUTIFUL
		if (textPDF.contains("index")) {
			// does not work i think
			int Istart = textPDF.indexOf("index");
			if (textPDF.get(start + 1).equals("terms")) {
				Istart = Istart + 2;
				if ((Istart < start) || (start == 0)) {
					start = Istart;
				}
				System.out.println("Index found " + start);
			} else if ((Istart < start) || (start == 0)) {
				start = findTermsposition(Istart + 1, new ArrayList<String>(
						textPDF.subList(Istart + 1, textPDF.size())));
			}
		}
		return start;
	}

	/**Identifies start of keywordenumeration (pdf uses as a term synonym)
	 * @param ostart
	 * @param arrayList
	 * @return
	 */
	private int findTermsposition(int ostart, ArrayList<String> arrayList) {
		for (int ii = 0; ii < arrayList.size(); ii++) {
			if ((arrayList.get(ii).contains("terms"))) {
				String word = arrayList.get(ii).replace("terms", "");
				if (word.length() > 1) {
					// word that contains dot -> is itself a keyword
					return ii + ostart;
				} else {
					// word is a fragment with no meaning
					return ii + 1 + ostart;
				}
			}
		}
		return 0;

	}

	/** identifies keyword start via "keyword" position
	 * @param textPDF
	 * @return
	 */
	private int getKeywPosition(ArrayList<String> textPDF) {
		for (int ii = 0; ii < textPDF.size(); ii++) {
			if ((textPDF.get(ii).contains("keywords"))) {
				String word = textPDF.get(ii).replace("keywords", "");
				if (word.length() > 1) {
					// word that contains dot -> is itself a keyword
					return ii;
				} else {
					// word is a fragment with no meaning
					return ii + 1;
				}
			}
		}
		return 0;
	}

	/** Identifies keyword end position
	 * @param textPDF
	 * @return
	 */
	private int findKeyWEnd(ArrayList<String> textPDF) {
		int end = textPDF.size() - 1;
		int endCandidate = 0;
		// ami2011 - towards 1s20 - ACM, WHEN- beul_et_al._ahfe.pdf, editorial -
		// iwc.iwt053.full.pdf
		String[] stops = { "introduction", "motivation", "abstract", ".",
				"acm", "towards", "when", "editorial", "*" };
		for (int ii = 0; ii < stops.length; ii++) {
			endCandidate = textPDF.indexOf(stops[ii]);
			if (textPDF.contains(stops[ii])) {
				if (stops[ii].equals(".")) {
					int dotcandidate = findDotPosition(textPDF);
					if (dotcandidate < endCandidate) {
						endCandidate = dotcandidate;
					}
				}
				if ((end > endCandidate) && (endCandidate > 4)) {
					end = endCandidate;
					System.out.println(stops[ii] + ": - " + end);
				}
			}
		}

		return end;

	}

	/**Identifies dot position to resolve fragments or end
	 * @param textPDF
	 * @return
	 */
	private int findDotPosition(ArrayList<String> textPDF) {
		for (int ii = 0; ii < textPDF.size(); ii++) {
			if ((textPDF.get(ii).contains("."))) {
				if (ii > 4) {
					String word = textPDF.get(ii).replace(".", "");
					if (word.length() > 1) {
						// word that contains dot -> is itself a keyword
						return ii + 1;
					} else {
						// word is a fragment with no meaning
						return ii;
					}
				} else
					break;
			}
		}
		return 50;
	}

	public String getTitlePage() {
		return titlePage;
	}

	public void setTitlePage(String titlePage) {
		this.titlePage = titlePage;
	}

	/**Retrieve number of categories for this pdf
	 * @return
	 */
	public int getCatnumb() {
		return catnumb;
	}

	public void setCatnumb(int catnumb) {
		this.catnumb = catnumb;
	}

	public int getPagenumber() {
		return pagenumber;
	}

	public void setPagenumber(int pagenumber) {
		this.pagenumber = pagenumber;
	}

}