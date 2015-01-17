package master.keyEx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

import master.keyEx.models.*;
import Database.DBInterface;
import Database.Database;

import com.cybozu.labs.langdetect.LangDetectException;

public class PDFHandler {
	static boolean debug_extractor = true;
	static boolean debug_db = true;
	static boolean debug_img = false;
	static String title = "";


	public PDFHandler() {

	}

	public static void main(String[] args) {
		// BasicConfigurator.configure();
		Corpus corpus = null;
		PDFHandler app = new PDFHandler();
		if (debug_extractor) {
			try {
				corpus = app.parsePDFtoKey();
			} catch (LangDetectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (debug_db) {

			if (corpus != null) {
				try {
					DBInterface.fillDB(corpus);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				System.out.println("SOMETHING WENT WRONG WITH THE EXPORT");
			}
		}
	}

	private Corpus parsePDFtoKey() throws LangDetectException, IOException {

		// File hack = new File(".");
		// String home = hack.getAbsolutePath();
		// String importData ="c:/RWTH/Data/Publikationen Cluster/test/";
		String importData = "c:/RWTH/Data/HCI/";
		// String importData = url.getPath();
		File folder = new File(importData);
		Corpus corpus = new Corpus();
		ArrayList<PDF> pdfList = new ArrayList<PDF>();
		boolean first = true;
		corpus = createCorpus(folder, corpus, pdfList, first);
		corpus.calculateIdf();
		// First calculate Keyword TFIDF (Relevance)
		corpus.setPdfList(corpus.calculateTD_IDF(corpus.getPdfList()));
		// SAVE FILTER LEVEL
		// Filter pdf keylist with min level 0.0001
		corpus.setPdfList(corpus.filterPDFTDIDF(corpus.getPdfList(), 0.0001));
		// Calculate CatTFIDF with filtered keywords
		corpus.calculateCatTFIDF();
		corpus.filterCatTFIDF(0.00001);
		corpus.calculateRel();
//		System.out.println("PDFHandler");
//		for (int ii = 0; ii < corpus.getPdfList().size(); ii++) {
//			ArrayList<WordOcc> words = corpus.getPdfList().get(ii)
//					.getWordOccList();
//			System.out
//			.println(corpus.getPdfList().get(ii).getTitle()
//					+ "______________________________________________________________");
//			for (int jj = 0; jj < words.size(); jj++) {
//				System.out.println(words.get(jj).getWord().getWord()
//						+ " -  TF: " + words.get(jj).getTf() + " IDF: "
//						+ words.get(jj).getIdf() + "TFIDF: "
//						+ words.get(jj).getTfidf() + " wordocc: "
//						+ words.get(jj).getOcc());
//			}
//
//		}
//		System.out
//				.println("______________________________________________________________");
//		System.out.println("PDFHandler");
//		for (int ii = 0; ii < corpus.getPdfList().size(); ii++) {
//			System.out
//					.println("----------------------------------------------------------------");
//			System.out.println(corpus.getPdfList().get(ii).getTitle());
//			for (int jj = 0; jj < corpus.getPdfList().get(ii)
//					.getGenericKeywords().size(); jj++) {
//				System.out.println(corpus.getPdfList().get(ii)
//						.getGenericKeywords().get(jj).getTitle()
//						+ " has Relevance: "
//						+ corpus.getPdfList().get(ii).getGenericKeywords()
//								.get(jj).getRelevance());
//			}
//		}
		return corpus;

	}

	private Corpus createCorpus(File folder, Corpus corpus,
			ArrayList<PDF> pdfList, boolean first) throws LangDetectException {
		File hack = new File(".");
		String home = hack.getAbsolutePath();
		String img = home + "/export/gen_img/";
		String key = home + "/export/gen_key/";
		String export = home + "/export/gen_svg/";
		PDFExtractor extractor = new PDFExtractor();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {

				System.out.println("File= " + folder.getAbsolutePath() + "\\"
						+ fileEntry.getName());

				ArrayList<Words> words = new ArrayList<Words>();
				try {
					words = extractor.parsePDFtoKey(fileEntry, first);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("File corrupted");
				}
				if (first) {
					first = false;
				}
				if (words.size() == 0) {
					int test = 0;
				} else {

					ArrayList<WordOcc> occ = extractor.keyOcc(words);

					// TODO add publication id!
					PDF pdf = new PDF(occ, extractor.getLang(),
							extractor.getWordcount(), extractor.getTitlePage());
					pdf.setGenericKeywords(extractor.getKeywords());

					pdf.setCatnumb(extractor.getCatnumb());
					// VERY RUDEMENTARY TITLE EXTRACTION VIA FILE
					pdf.setTitle(getFileN(fileEntry));

					// No keywords tough love
					if (!pdf.getGenericKeywords().isEmpty()) {
						pdfList.add(pdf);
						String language = pdf.getLanguage();
						corpus.incDocN(language);
						corpus.setPdfList(pdfList);
						corpus.associateWordswithCategory(pdf);
						if (debug_img) {
							System.out.println("File= "
									+ folder.getAbsolutePath() + "\\"
									+ fileEntry.getName());
							createImgText(fileEntry, occ, key, img, export,
									home);
						}

					}
				}
			} else if (fileEntry.isDirectory()) {
				System.out.println("RECURSION!");
				createCorpus(fileEntry, corpus, pdfList, first);
			}
		}
		return corpus;
	}

	private void createImgText(File fileEntry, ArrayList<WordOcc> occ,
			String key, String img, String export, String home) {

		title = getFileN(fileEntry);

		Text2Image image = new Text2Image();
		image.generateImage(title, img);
		createTextExport(occ, key, title);
		if ((title.substring(title.lastIndexOf('.') + 1, title.length())
				.toLowerCase()).equals("txt"))
			;

		WordCramGen wcg = new WordCramGen();

		wcg.generate(home + "/export/", export, title);

	}

	private String getFileN(File fileEntry) {
		String title = fileEntry.getName();
		int pos = title.lastIndexOf(".");
		if (pos >= 0) {
			title = title.substring(0, pos);
		}
		return title;
	}

	private static void createTextExport(ArrayList<WordOcc> keyOcc,
			String path, String title) {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path + title + ".txt"), "utf-8"));
			for (int ii = 0; ii < keyOcc.size(); ii++) {
				WordOcc current = keyOcc.get(ii);

				writer.write(current.getWord().getWord() + ";"
						+ current.getOcc() + ";");

			}
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}

}
