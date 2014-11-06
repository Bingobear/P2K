package test.tester;

import java.io.IOException;
import java.util.ArrayList;

import opennlp.tools.util.InvalidFormatException;


/**
 * Hello world!
 * 
 */
public class App {
	public App() {

	}

	public static void main(String[] args) {
	//	BasicConfigurator.configure();
		PDFExtractor app = new PDFExtractor();
		app.sentencedetect();
		try {
			String parsedText = app.parsePdftoString();
			String [] tokens = app.generalToken(parsedText);
			ArrayList<String> keywords = app.getKeywordsfromPDF(tokens);
			//englishStemmer stemmer = new englishStemmer();
			//go go stemming
			if (keywords.isEmpty()){
				
				//empty - could not directly extract keywords
			}else{
				//use extracted keywords as ref. elements
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
