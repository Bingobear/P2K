package test.tester;

import java.io.IOException;
import java.util.ArrayList;

import org.tartarus.snowball.ext.englishStemmer;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.InvalidFormatException;

/**
 * Hello world!
 * 
 */
public class App {
	public App() {

	}

	public static void main(String[] args) {
		// BasicConfigurator.configure();
		PDFExtractor app = new PDFExtractor();

		try {

			String parsedText = app.parsePdftoString();

			ArrayList<String> tokenheaven =app.getToken(parsedText);
			String[] tokenTest = new String[tokenheaven.size()];
			for (int ii=0;ii<tokenheaven.size();ii++){
				tokenTest[ii] = tokenheaven.get(ii);
			}
			String[] tokens = app.generalToken(parsedText);
			ArrayList<String> keywords = app.getKeywordsfromPDF(tokens);
			app.posttags(tokenTest);
			// does not work like this
			Stemmer stem = new Stemmer();
			String[] stemtokens = stem.stem(tokens);

			System.out.println("normal:" + tokens.length + ", stemmed"
					+ stemtokens.length);
			// go go stemming
			if (keywords.isEmpty()) {

				// empty - could not directly extract keywords
			} else {
				// use extracted keywords as ref. elements
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			app.token();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Hello World!");
	}

}
