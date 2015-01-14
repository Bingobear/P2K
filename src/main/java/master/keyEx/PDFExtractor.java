package master.keyEx;

import java.io.File;
import java.io.FileInputStream;
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

public class PDFExtractor {
	// TODO: get Title via extracting names then creating offset to them via
	// TODO:Change language to PDF object
	// first words
	/**
	 * PDF Extractor
	 * 
	 * @throws IOException
	 */
	private String titlePage;
	private int catnumb;

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

	public String parsePdftoString(PDFTextStripper pdfStripper,
			PDDocument pdDoc, int start, int end) throws IOException {

		pdfStripper.setStartPage(start);
		pdfStripper.setEndPage(end);
		String parsedText = pdfStripper.getText(pdDoc);
		System.out.println("pages: " + start + "-" + end + " parsed");
		return parsedText;
	}

	// TODO versuch größere menge zu bekommen
	public ArrayList<Category> getKeywordsfromPDF(String[] text) {
		ArrayList<Category> keywords = new ArrayList<Category>();
		ArrayList<String> textPDF = new ArrayList<String>(Arrays.asList(text));
		int counter = 0;

		if (textPDF.contains("Keywords")) {
			counter = textPDF.indexOf("Keywords");
			System.out.println("Keyword found");

		} else if (textPDF.contains("key")) {
			counter = textPDF.indexOf("key");

		} else if (textPDF.contains("word")) {
			counter = textPDF.indexOf("word");
		} else if (textPDF.contains("Index")) {
			// does not work i think
			counter = textPDF.indexOf("Index");
			System.out.println("Index found");
			if (textPDF.get(counter + 1).equals("Terms")) {
				counter = counter + 1;
			} else {
				counter = 0;
			}
		}
		counter++;
		int offset = 0;

		while (counter != 0) {
			String intro = textPDF.get(counter).toUpperCase();
			if (((textPDF.get(counter).equals(",") || (textPDF.get(counter)
					.equals(";") || (textPDF.get(counter).equals(".") ^ ((textPDF
					.get(counter).matches("\\d+")))))
					&& (offset != 0)))) {
				String currkey = "";
				//TODO KEYWORD EXTRACTION HAS TO LOOK BETTER
				for (int ii = counter - offset; ii < counter; ii++) {
					currkey = currkey + textPDF.get(ii);
					if (!(textPDF.get(ii + 1).equals(",") || (textPDF.get(
							counter).equals(";") || (textPDF.get(ii + 1)
							.equals(".") ^ (textPDF.get(ii + 1).matches("\\d+")))))) {
						currkey = currkey.replaceAll("\\W", "") + " ";
						
					}
				}
				if (!currkey.matches("\\d+")) {
					Category category = new Category(currkey);
					keywords.add(category);
				}
				counter++;
				offset = 0;
			} else if ((offset > 4)) {
				counter = 0;
			} else if (intro.equals("INTRODUCTION")||intro.equals("ABSTRACT")) {
				counter = 0;
			} else {
				counter++;
				offset++;
			}
		}
		// System.out.print(counter);
		setCatnumb(keywords.size());
		return keywords;

	}

	/**
	 * @param parsedText
	 * @return
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
	 * Hello world OpenNLP!
	 * 
	 * @throws IOException
	 * @throws InvalidFormatException
	 * 
	 */

	public ArrayList<String> NameFinder(String[] sentences)
			throws InvalidFormatException, IOException {
		// TEST

		// Load the model file downloaded from OpenNLP
		// http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
		InputStream modelIn = null;
		if (this.getLang().equals("en")) {
			modelIn = getClass().getResourceAsStream("/eng/en-ner-person.bin");
		} else {
			modelIn = getClass().getResourceAsStream("/ger/de-ner-person.bin");
		}
		TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

		// Create a NameFinder using the model
		NameFinderME finder = new NameFinderME(model);

		Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
		ArrayList<String> result = new ArrayList<String>();
		for (String sentence : sentences) {

			// Split the sentence into tokens
			String[] tokens = tokenizer.tokenize(sentence);

			// Find the names in the tokens and return Span objects
			Span[] nameSpans = finder.find(tokens);

			// Print the names extracted from the tokens using the Span data
			String[] helper = null;
			helper = Span.spansToStrings(nameSpans, tokens);
			for (int ii = 0; ii < helper.length; ii++) {
				result.add(helper[ii]);
			}

		}

		return result;
	}

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

	// Optimize extraction
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
					// System.out.println(tokenSen[jj]);
					// tokenSen[jj].replaceAll("\\W", "")
					// TODO Improve word recognition
					// TODO Filter line break
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

	// /**
	// * - change to index return
	// *
	// * @param filter
	// * @param tokens
	// * @return
	// */
	// public ArrayList<String> filterNounVerb(String[] filter, String[] tokens)
	// {
	// ArrayList<Integer> result = new ArrayList<Integer>();
	// for (int ii = 0; ii < filter.length; ii++) {
	// if ((filter[ii].contains("NN")) || (filter[ii].contains("VB"))) {
	// result.add(ii);
	// }
	// }
	// ArrayList<String> words = new ArrayList<String>();
	// for (int ii = 0; ii < result.size(); ii++) {
	// words.add(tokens[result.get(ii)]);
	// }
	// return words;
	//
	// }
	//
	// // Delete references from PDF
	//
	// public ArrayList<String> filterNoun(String[] filter, String[] tokens) {
	// ArrayList<Integer> result = new ArrayList<Integer>();
	// for (int ii = 0; ii < filter.length; ii++) {
	// if ((filter[ii].contains("NN"))) {
	// result.add(ii);
	// }
	// }
	// ArrayList<String> words = new ArrayList<String>();
	// for (int ii = 0; ii < result.size(); ii++) {
	// words.add(tokens[result.get(ii)]);
	// }
	// return words;
	// }

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

				// TODO:Question compare words or only stem with type
				// TODO: some words have fallouts - not accounted duplicates
				// Lower Border
				// IMPROVED FILTER ALGO

				if (compare.getWord().equals(current.getWord())
						|| ((compare.getStem().equals(current.getStem())) && ((compare
								.getType().contains(current.getType()) || (current
								.getType().contains(compare.getType())))))) {
					if (compare.getWord().equals("cloud")) {
						String test = "";
					}
					keywords.remove(ii);
					count++;
					arraySize--;
				}
				counter = ii;
				size = keywords.size();
				// UPPER BORDER
				// if ((compare.getWord().contains(current.getWord()))
				// && (compare.getStem().equals(current.getStem()))
				// && (compare.getType().equals(current.getType()))) {
				// keywords.remove(ii);
				// count++;
				// arraySize--;
				// }
			}

			result.add(new WordOcc(current, count));
		}
		return result;
	}

	/**
	 * Generate Word ArrayList
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
						Words word = new Words(
								tokens[ii].replaceAll("\\W", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					} else {
						// MAYBE SOLVES PROBLEM?TODO
						Words word = new Words(tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					}
					// TODO OLD VERSION BETTER ?
					// Words word = new Words(tokens[ii], stemmedW[ii],
					// filter[ii],this.keywords);
					// result.add(word);
				}
			}
		} else if (mode == 1) {
			for (int ii = 0; ii < filter.length; ii++) {
				if ((filter[ii].contains("NN")) || (filter[ii].contains("VB"))) {
					if (!this.language.equals("de")) {
						// System.out.println(tokens[ii]);
						Words word = new Words(
								tokens[ii].replaceAll("\\W", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					} else {
						// MAYBE SOLVES PROBLEM?TODO
						Words word = new Words(tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					}
				}
			}
		} else if (mode == 2) {
			for (int ii = 0; ii < filter.length; ii++) {
				if ((filter[ii].contains("NN")) || (filter[ii].contains("JJ"))) {
					if (!this.language.equals("de")) {
						// System.out.println(tokens[ii]);
						Words word = new Words(
								tokens[ii].replaceAll("\\W", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					} else {
						// MAYBE SOLVES PROBLEM?TODO
						Words word = new Words(tokens[ii].replaceAll(
								"[^\\p{L}\\p{Nd}]+", ""), stemmedW[ii],
								filter[ii], this.keywords);
						result.add(word);
					}
				}
			}
		}

		return result;
	}



	
	/**
	 * TODO: GET TITLE FROM FIRST SENTENCE - idea: use namefinder
	 * 
	 * @param fileEntry
	 * @param first
	 * @param url2
	 * 
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	public ArrayList<Words> parsePDFtoKey(File fileEntry, boolean first)
			throws LangDetectException, IOException {
		ArrayList<Words> result = new ArrayList<Words>();

		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		setTitlePage(fileEntry.getName());
		// TODO:Move to input
		// antrag big, test small
		// URL url = getClass().getResource("/text/test.pdf");
		// File file = new File(url.getPath());

		PDFParser parser = new PDFParser(new FileInputStream(fileEntry));
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();

		pdDoc = new PDDocument(cosDoc);

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
							counter, counter + 1)); // TODO:MOVE KEYWORDS TO PDF
													// OBJECT
					String[] tokens = getTokenPM(parsedText);
					// TODO create regEx for identifying keyword - area
					ArrayList<Category> keywords = getKeywordsfromPDF(tokens);

					// No keywords you are out
					if (keywords.isEmpty()) {
//						File dest = new File("c:/RWTH/Data/noKeywords/");
//						System.out
//								.println("PDFExtractor: No Keywords in pdf -> ignore");
//						FileUtils.copyFileToDirectory(fileEntry, dest);
						// empty - could not directly extract keywords
						break;
					} else {
//						File dest = new File("c:/RWTH/Data/hasKeywords/");
//						FileUtils.copyFileToDirectory(fileEntry, dest);
						this.setKeywords(keywords);
					}

				}

				parsedText = parsedText.toLowerCase();

				// sentence detector -> tokenizer
				String[] tokens = getToken(parsedText);
				String[] filter = posttags(tokens);
				// TODO move sonderzeichen behandlung zu occurence function
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
		System.out.println("FINAL RESULT:optimiertNouns:" + result.size());
		return result;
	}

	@SuppressWarnings("unused")
	private String extractTitle(String parsedText) {
		SentenceDetector sentdetector = sentencedetect();
		String[] sentence = sentdetector.sentDetect(parsedText);
		try {
			NameFinder(sentence);
		} catch (InvalidFormatException e) {
			//
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
		return null;
	}

	public String getTitlePage() {
		return titlePage;
	}

	public void setTitlePage(String titlePage) {
		this.titlePage = titlePage;
	}

	public int getCatnumb() {
		return catnumb;
	}

	public void setCatnumb(int catnumb) {
		this.catnumb = catnumb;
	}

}