package master.keyEx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import master.keyEx.models.*;
import Database.DBInterface;
import Database.Database;

import com.cybozu.labs.langdetect.LangDetectException;

/** Main Interface to initiate Textmining (pdf extractor)
 * @author Simon Bruns
 *
 */
public class PDFHandler {
	//debug modes
	static boolean debug_extractor = true;
	static boolean debug_db = false;
	static boolean debug_img = false;
	static String title = "";

	public PDFHandler() {

	}

	/**Initiates corpus text mining - ranking
	 * @param args
	 */
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
		corpus = PubMapper.enrichCorpus(corpus);
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

	/** Main text mining method return parsed/calculated corpus (containing all pdfs) 
	 * @return corpus
	 * @throws LangDetectException
	 * @throws IOException
	 */
	private Corpus parsePDFtoKey() throws LangDetectException, IOException {

		String importData = "c:/RWTH/Data/test/";
		File folder = new File(importData);
		Corpus corpus = new Corpus();
		ArrayList<PDF> pdfList = new ArrayList<PDF>();
		boolean first = true;
		corpus = createCorpus(folder, corpus, pdfList, first);
		corpus.calculateIdf();
		corpus.setPdfList(corpus.calculateTD_IDF(corpus.getPdfList()));

		corpus.initializeTFICFCalc();
		corpus.filterTFICF(0.00001);
		corpus.calculateAllPDFCatRel();
		return corpus;

	}

	/** Creates basic corpus -> text mining (word extraction,keyword,pdfs)
	 * @param folder
	 * @param corpus
	 * @param pdfList
	 * @param first
	 * @return corpus
	 * @throws LangDetectException
	 */
	private Corpus createCorpus(File folder, Corpus corpus,
			ArrayList<PDF> pdfList, boolean first) throws LangDetectException {
		File hack = new File(".");
		String home = hack.getAbsolutePath();
		String img = home + "/export/gen_img/";
		String key = home + "/export/gen_key/";
		String export = home + "/export/gen_svg/";
		PDFExtractor extractor = new PDFExtractor();
		String importtitle = "c:/RWTH/Data/titletable/pdftitleo.csv";
		ArrayList<String> titles = readCSVTitle(importtitle);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {

				System.out.println("File= " + folder.getAbsolutePath() + "\\"
						+ fileEntry.getName());

				ArrayList<Words> words = new ArrayList<Words>();
				try {
					words = extractor.parsePDFtoKey(fileEntry, first,
							corpus.getPdfList());
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

					PDF pdf = new PDF(occ, extractor.getLang(),
							extractor.getWordcount(), extractor.getTitlePage());
					pdf.setGenericKeywords(extractor.getKeywords());

					pdf.setCatnumb(extractor.getCatnumb());
					// RUDEMENTARY TITLE EXTRACTION VIA FILE
					pdf.setTitle(getTitle(fileEntry.getName(), titles));

					// No keywords -> not valid pdf
					if (!pdf.getGenericKeywords().isEmpty()) {
						pdf.setFilename(fileEntry.getName());
						pdfList.add(pdf);
						String language = pdf.getLanguage();
						pdf.setPagecount(extractor.getPagenumber());
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

	private String getTitle(String fileName, ArrayList<String> titles) {
		for (int ii = 0; ii < titles.size(); ii = ii + 2) {
			if (titles.get(ii).equals(fileName)) {
				System.out.println("FOUND:" + titles.get(ii + 1));
				String titleNorm = Normalizer.normalize(titles.get(ii + 1),
						Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
				return titleNorm;
			}
		}
		return fileName;

	}

	/**retrieve titles from external csv title file
	 * @param importtitle
	 * @return
	 */
	private ArrayList<String> readCSVTitle(String importtitle) {
		String csvFile = importtitle;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		ArrayList<String> titles = new ArrayList<String>();
		String[] helper = null;

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator

				helper = line.split(cvsSplitBy);
				for (int counter = 0; counter < helper.length; counter++) {
					titles.add(helper[counter]);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
		return titles;
	}

	/**Creates a wordcloud out of a given text
	 * @param fileEntry -> pdf
	 * @param occ -> word occurences and word itself
	 * @param key 
	 * @param img
	 * @param export
	 * @param home
	 */
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
