package test.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.apache.lucene.document.Document;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.lucene.LucenePDFDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

/**
 * Hello world!
 * 
 */
public class App {
	public App() {

	}

	public static void main(String[] args) {
	//	BasicConfigurator.configure();
		App app = new App();
		app.sentencedetect();
		try {
			app.testPdf();
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
/**
 * PDF Extractor
 * @throws IOException
 */
	private void testPdf() throws IOException {
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		URL url = getClass()
				.getResource("/test.pdf");
		File file = new File(url.getPath());

		PDFParser parser = new PDFParser(new FileInputStream(file));
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();
		pdDoc = new PDDocument(cosDoc);
		pdfStripper.setStartPage(1);
		pdfStripper.setEndPage(5);
		String parsedText = pdfStripper.getText(pdDoc);
		//System.out.println(parsedText);	
		generalToken(parsedText);
	}

	/**
	 * @param parsedText 
 * 
 */
	public void generalToken(String parsedText) {
		Tokenizer _tokenizer = null;

		InputStream modelIn = null;
		try {
			// Loading tokenizer model
			modelIn = getClass().getResourceAsStream("/en-token.bin");
			final TokenizerModel tokenModel = new TokenizerModel(modelIn);
			modelIn.close();

			_tokenizer = new TokenizerME(tokenModel);
			String [] tokens = _tokenizer.tokenize(parsedText);
			System.out.println(tokens.length);

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
	}

	/**
	 * Hello world OpenNLP!
	 * 
	 * @throws IOException
	 * @throws InvalidFormatException
	 * 
	 */

	public void token() throws InvalidFormatException, IOException {

		String[] sentences = {
				"If President John F. Kennedy, after visiting France in 1961 with his immensely popular wife,"
						+ " famously described himself as 'the man who had accompanied Jacqueline Kennedy to Paris,'"
						+ " Mr. Hollande has been most conspicuous on this state visit for traveling alone.",
				"Mr. Draghi spoke on the first day of an economic policy conference here organized by"
						+ " the E.C.B. as a sort of counterpart to the annual symposium held in Jackson"
						+ " Hole, Wyo., by the Federal Reserve Bank of Kansas City. " };

		// Load the model file downloaded from OpenNLP
		// http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
		InputStream modelIn = null;
		modelIn = getClass().getResourceAsStream("/en-ner-person.bin");
		TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

		// Create a NameFinder using the model
		NameFinderME finder = new NameFinderME(model);

		Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

		for (String sentence : sentences) {

			// Split the sentence into tokens
			String[] tokens = tokenizer.tokenize(sentence);

			// Find the names in the tokens and return Span objects
			Span[] nameSpans = finder.find(tokens);

			// Print the names extracted from the tokens using the Span data
			System.out.println(Arrays.toString(Span.spansToStrings(nameSpans,
					tokens)));
		}
	}

	private SentenceDetector sentencedetect() {
		// TODO Auto-generated method stub
		SentenceDetector _sentenceDetector = null;

		InputStream modelIn = null;
		try {
			// Loading sentence detection model
			modelIn = getClass().getResourceAsStream("/en-sent.bin");
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

}
