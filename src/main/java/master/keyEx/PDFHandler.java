package master.keyEx;

import java.io.IOException;
import java.util.ArrayList;


import com.cybozu.labs.langdetect.LangDetectException;


/**
 * Hello world!
 * 
 */
public class PDFHandler {
	public PDFHandler() {

	}

	public static void main(String[] args) {
		// BasicConfigurator.configure();

		try {
			parsePDFtoKey();
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void parsePDFtoKey() throws LangDetectException, IOException {
		PDFExtractor app = new PDFExtractor();
		String parsedText = app.parsePdftoString();

		LangDetect lang = new LangDetect();
		app.setLang(lang.detect(parsedText));
		System.out.println(app.getLang());
		// sentence detector -> tokenizer
		String[] tokens = app.getToken(parsedText);

		ArrayList<String> keywords = app.getKeywordsfromPDF(tokens);
		String[] filter = app.posttags(tokens);
		// ArrayList<Integer> keys = app.filterNounVerb(filter);
		ArrayList<String> keys = app.filterNoun(filter, tokens);

		ArrayList<Words> words = app.generateWords(filter,tokens); 


		System.out.println("normal:" + tokens.length + ", optimiertNouns:"
				+ keys.size() );
		// go go stemming

		if (keywords.isEmpty()) {

			// empty - could not directly extract keywords
		} else {
			// use extracted keywords as ref. elements
		}
		// app.token();

	}


}
