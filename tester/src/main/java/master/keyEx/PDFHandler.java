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
import Database.Database;

import com.cybozu.labs.langdetect.LangDetectException;

public class PDFHandler {
	static boolean debug_extractor = true;
	static boolean debug_db = false;
	static boolean debug_img = false;
	static String title = "";

	public PDFHandler() {

	}

	public static void main(String[] args) {
		// BasicConfigurator.configure();
		if (debug_db) {
			Database test = new Database();
			try {
				test.readDataBase();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		PDFHandler app = new PDFHandler();
		if (debug_extractor) {
			try {
				app.parsePDFtoKey();
			} catch (LangDetectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void parsePDFtoKey() throws LangDetectException, IOException {
		PDFExtractor extractor = new PDFExtractor();
		File hack = new File(".");
		String home = hack.getAbsolutePath();
		String img = home + "/export/gen_img/";
		String key = home + "/export/gen_key/";
		String export = home + "/export/gen_svg/";
		URL url = getClass().getResource("/data/pdf/");
		File folder = new File(url.getPath());
		boolean first = true;
		ArrayList<PDF> pdfList = new ArrayList<PDF>();
		Corpus corpus = new Corpus();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {

				System.out.println("File= " + folder.getAbsolutePath() + "\\"
						+ fileEntry.getName());
				title = fileEntry.getName();
				int pos = title.lastIndexOf(".");
				if (pos >= 0) {
					title = title.substring(0, pos);
				}
				Text2Image image = new Text2Image();
				image.generateImage(title, img);

				ArrayList<Words> words = extractor.parsePDFtoKey(fileEntry,
						first);
				if (first) {
					first = false;
				}
				ArrayList<WordOcc> occ = extractor.keyOcc(words);
				// createTextExport(occ);
				// TODO add publication id!
				PDF pdf = new PDF(occ, extractor.getLang(),
						extractor.getWordcount());
				pdfList.add(pdf);
				corpus.incDocN();
				if (debug_img) {
					createTextExport(occ, key, title);
					if ((title.substring(title.lastIndexOf('.') + 1,
							title.length()).toLowerCase()).equals("txt"))
						System.out.println("File= " + folder.getAbsolutePath()
								+ "\\" + fileEntry.getName());
					WordCramGen wcg = new WordCramGen();

					wcg.generate(home + "/export/", export, title);
				}
			}
		}
		corpus.setPdfList(pdfList);
		corpus.calculateIdf();
		for (int ii = 0; ii < pdfList.size(); ii++) {
			pdfList.get(ii).calculateTF_IDF();
			System.out.println(ii);
			ArrayList<WordOcc> words = pdfList.get(ii).getWordOccList();
			for (int jj = 0; jj < words.size(); jj++) {
				System.out.println(words.get(jj).getTfidf() + ":"
						+ words.get(jj).getWord().getWord());
			}
			System.out
					.println("______________________________________________________________");
		}
		// PDFExtractor extractor = new PDFExtractor();
		// ArrayList<Words> words = extractor.parsePDFtoKey();
		//
		// ArrayList<WordOcc> occ = extractor.keyOcc(words);
		// //createTextExport(occ);
		//

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
	// private static void createTextExport(ArrayList<WordOcc> keyOcc) {
	// Writer writer = null;
	//
	// try {
	// writer = new BufferedWriter(new OutputStreamWriter(
	// new FileOutputStream("test.txt"), "utf-8"));
	// for (int ii = 0; ii < keyOcc.size(); ii++) {
	// WordOcc current = keyOcc.get(ii);
	//
	// writer.write(current.getWord().getWord() + ";"
	// + current.getOcc() + ";");
	//
	// }
	// } catch (IOException ex) {
	// // report
	// } finally {
	// try {
	// writer.close();
	// } catch (Exception ex) {
	// }
	// }
	//
	// }

}
