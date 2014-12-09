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

	private Corpus parsePDFtoKey() throws LangDetectException,
			IOException {
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

				ArrayList<Words> words = extractor.parsePDFtoKey(fileEntry,
						first);
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
					pdfList.add(pdf);
					corpus.incDocN();

					if (debug_img) {
						System.out.println("File= " + folder.getAbsolutePath()
								+ "\\" + fileEntry.getName());
						createImgText(fileEntry, occ, key, img, export, home);

					}
				}
			}
		}
		corpus.setPdfList(pdfList);
		corpus.calculateIdf();
		pdfList = corpus.calculateTD_IDF(pdfList);
		//SAVE FILTER LEVEL
		//pdfList = corpus.filterPDFTDIDF(pdfList,0.0001);
		corpus.setPdfList(pdfList);
		return corpus;

	}


	private void createImgText(File fileEntry, ArrayList<WordOcc> occ,
			String key, String img, String export, String home) {
		title = fileEntry.getName();
		int pos = title.lastIndexOf(".");
		if (pos >= 0) {
			title = title.substring(0, pos);
		}
		Text2Image image = new Text2Image();
		image.generateImage(title, img);
		createTextExport(occ, key, title);
		if ((title.substring(title.lastIndexOf('.') + 1, title.length())
				.toLowerCase()).equals("txt"))
			;

		WordCramGen wcg = new WordCramGen();

		wcg.generate(home + "/export/", export, title);

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
