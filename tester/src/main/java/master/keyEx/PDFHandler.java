package master.keyEx;

import java.io.IOException;
import java.util.ArrayList;




import master.keyEx.models.WordOcc;
import master.keyEx.models.Words;

import com.cybozu.labs.langdetect.LangDetectException;

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
		PDFExtractor extractor = new PDFExtractor();
		ArrayList<Words> words = extractor.parsePDFtoKey();

		ArrayList<WordOcc> occ = extractor.keyOcc(words);

		// go go stemming


		// app.token();

	}


}
