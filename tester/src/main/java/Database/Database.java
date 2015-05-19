package Database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.AutoCloseable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import Database.model.*;
import master.keyEx.models.Corpus;
import master.keyEx.models.PDF;
import master.keyEx.models.Publication;
import master.keyEx.models.WordOcc;

//TODO HCICORPUS ->CORPUS (later)
public class Database {
	private Connection connect = null;
	private String dbName = "corpus";// hcicorpus
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	String fileNC;

	// you need to close all three to make sure
	private void close() {
		close(resultSet);
		close(statement);
		close(connect);
	}

	public ArrayList<Publication> getAllPub() throws ClassNotFoundException,
			SQLException {
		ArrayList<Publication> publications = new ArrayList<Publication>();

		Class.forName("com.mysql.jdbc.Driver");
		// setup the connection with the DB.
		connect = DriverManager.getConnection("jdbc:mysql://localhost/"
				+ dbName + "?" + "user=test&password=test");
		Statement stmt = connect.createStatement();
		String sqlT = "SELECT idPublication,title FROM " + dbName
				+ ".Publication";
		ResultSet rsT = stmt.executeQuery(sqlT);
		while (rsT.next()) {
			Publication pub = new Publication(rsT.getInt("idPublication"),
					rsT.getString("title"));
			publications.add(pub);
		}
		rsT.close();
		for (int ii = 0; ii < publications.size(); ii++) {
			ArrayList<Author> authors = new ArrayList<Author>();
			int idPub = publications.get(ii).getPubID();
			String sql = "SELECT Author_idAuthor FROM "
					+ dbName
					+ ".Publication_has_Author WHERE Publication_idPublication="
					+ idPub;
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				authors.add(new Author(rs.getInt("Author_idAuthor")));
			}
			rs.close();
			for (int jj = 0; jj < authors.size(); jj++) {
				ResultSet resultSetCategory = stmt
						.executeQuery("SELECT * FROM  " + dbName
								+ ".author Where idAuthor="
								+ authors.get(jj).getAuthorID());
				while (resultSetCategory.next()) {
					authors.get(jj)
							.setName(resultSetCategory.getString("name"));
				}
				resultSetCategory.close();

			}
			publications.get(ii).setAuthors(authors);
		}
		stmt.close();
		return publications;

	}

	private void close(AutoCloseable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// don't throw now as it might leave following closables in
			// undefined state
		}
	}

	public void fillDB(PDF pdf, Corpus corpus) throws SQLException,
			ClassNotFoundException {
		// this will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
		// setup the connection with the DB.
		connect = DriverManager.getConnection("jdbc:mysql://localhost/"
				+ dbName + "?" + "user=test&password=test");

		// fileNC = pdf.getFilename();
		// int idPub = -1;
		// String sqlT = "SELECT idPublication,title FROM " + dbName
		// + ".Publication";
		// ResultSet rsT = stmt.executeQuery(sqlT);
		// String titlepage = "";
		// String authorFake = "";
		// if (pdf.getTitle().isEmpty()) {
		// titlepage = pdf.getFirstPage().toLowerCase();
		// } else {
		// authorFake = pdf.getTitle();
		// titlepage = pdf.getTitle().toLowerCase();
		// }
		//
		// ArrayList<String> titles = new ArrayList<String>();
		// ArrayList<Integer> titleCand = new ArrayList<Integer>();
		// while (rsT.next()) {
		// int id = rsT.getInt("idPublication");
		// String original = rsT.getString("title");
		// String title = original.toLowerCase();
		// int partitionSize = 0;
		// int length = title.length();
		// if (length < 10) {
		// } else if (length < 20) {
		// partitionSize = 6;
		// } else if (length < 30) {
		// partitionSize = 10;
		// } else {
		// partitionSize = 16;
		// }
		// if (partitionSize == 0) {
		// if (titlepage.contains(title)) {
		// System.out.println("found shortTitle:" + title);
		// idPub = id;
		// break;
		// }
		// } else {
		// int dividor = length / partitionSize;
		// ArrayList<String> subs = new ArrayList<String>();
		// // ka geht das ?
		// for (int ii = 0; ii < length - partitionSize; ii = ii
		// + partitionSize) {
		// subs.add(title.substring(ii, ii + partitionSize));
		// }
		// if (length - dividor * partitionSize > partitionSize / 2) {
		// subs.add(title.substring(dividor * partitionSize, length));
		// }
		// for (int ii = 0; ii < subs.size(); ii++) {
		//
		// if (titlepage.contains(subs.get(ii))) {
		// // delete when bth optimized
		//
		// titles.add(title);
		// titleCand.add(id);
		// System.out
		// .println("found title:" + subs.get(ii) + " = "
		// + original + " = " + subs + " ///" + id);
		//
		// }
		// }
		// }
		// /*
		// * if (titlepage.contains("approach to understand human factors")) {
		// * if (title.contains(
		// *
		// "a game-based approach to understand human factors in supply chains and quality management"
		// * )) { int test = 0; } } if (titlepage.contains(title)) { idPub =
		// * id; System.out.println("FOUND Title - " + title); break; }
		// */
		// }
		// rsT.close();
		//
		// if (titleCand.size() > 1) {
		// int[] occ = new int[titleCand.size()];
		// Arrays.fill(occ, 1);
		// int max = 1;
		// int maxCan = -1;
		// ArrayList<Integer> open = new ArrayList<Integer>();
		// boolean unique = false;
		// for (int ii = 0; ii < titleCand.size(); ii++) {
		// for (int jj = ii + 1; jj < titleCand.size(); jj++) {
		// // ==
		// if (titleCand.get(ii).equals(titleCand.get(jj))) {
		// occ[ii] = occ[ii] + 1;
		// // missing scenario max occ by more than one
		// if (occ[ii] > max) {
		// max = occ[ii];
		// idPub = titleCand.get(ii);
		// maxCan = titleCand.get(ii);
		// open.add(ii);
		// unique = true;
		// } else if ((occ[ii] == max) && (occ[ii] > 1)
		// && (titleCand.get(ii) != maxCan)) {
		// unique = false;
		// }
		// }
		// }
		// }
		// System.out.println(idPub + " - occ: " + max);
		// if (!unique) {
		// idPub = -1;
		// }
		// System.out.println(idPub + " - occ: " + max);
		// }
		//
		// ArrayList<Integer> authors = new ArrayList<Integer>();
		// if (idPub < 0) {
		// // not in BTH database
		// String sql = "SELECT idAuthor,name FROM " + dbName + ".Author";
		// ResultSet rs = stmt.executeQuery(sql);
		// // STEP 5: Extract data from result set
		// ArrayList<String> author = new ArrayList<String>();
		// ArrayList<Integer> positions = new ArrayList<Integer>();
		// ArrayList<Integer> distance = new ArrayList<Integer>();
		//
		// ArrayList<Author> authorsall = createAllAuthors();
		//
		// for (Author auth : authorsall) {
		// // while (rs.next()) {
		// // // Retrieve by column name
		// // int id = rs.getInt("idAuthor");
		// // String name = rs.getString("name");
		// ArrayList<String> nameparts = new ArrayList<String>();
		// for (String retval : auth.getName().split(",")) {
		// nameparts.add(retval);
		// }
		// // if(name.equals("Thombansen, Ulrich")){
		// // String stop = "kacke";
		// // // name="Thombansen";
		// // }
		// // String eval = pdf.getFirstPage();
		// // //\\p{L}
		// // eval = eval.replaceAll("[^\\p{L}]"," ");
		// //
		// // for (int count = 0; count < nameparts.size()-1; count++) {
		// // if(eval.matches(".*\\b"+nameparts.get(count)+"\\b.*")){
		// // // if (pdf.getFirstPage().contains(nameparts.get(count))) {
		// // authors.add(auth.getAuthorID());
		// // System.out.println(nameparts.get(count));
		// // // System.out.println("FOUND Author - " + name
		// // // + pdf.getFirstPage().substring(0, 10));
		// // }
		// // }
		// // if(authors.isEmpty()){
		//
		// for (int count = 0; count < nameparts.size() - 1; count++) {
		//
		// if (pdf.getFirstPage().contains(nameparts.get(count))) {
		// if (nameparts.get(count).equals("Li")) {
		// int test = 0;
		// }
		// if (authorFake.contains(nameparts.get(count))) {
		// continue;
		// }
		// int pos = pdf.getFirstPage().indexOf(
		// nameparts.get(count));
		//
		// if (!positions.isEmpty()) {
		// int length = positions.size();
		// for (int kk = 0; kk < length; kk++) {
		//
		// if ((positions.get(kk) > pos)) {
		// positions.add(kk, pos);
		// authors.add(kk, auth.getAuthorID());
		// author.add(kk, nameparts.get(count));
		// // letztes element
		// } else if (kk == length - 1) {
		// positions.add(pos);
		// authors.add(auth.getAuthorID());
		// author.add(nameparts.get(count));
		// }
		// }
		//
		// } else {
		// authors.add(auth.getAuthorID());
		// positions.add(pos);
		// System.out
		// .println(nameparts.get(count) + ":" + pos);
		// author.add(nameparts.get(count));
		// }
		//
		// /*
		// * if (pos < min) { min = pos; } if (pos > max) { max =
		// * pos; }
		// */
		//
		// // System.out.println("FOUND Author - " + name
		// // + pdf.getFirstPage().substring(0, 10));
		// }
		// }
		// // }
		//
		// }
		//
		// // Create subfunction overlapping names
		// HashSet<Integer> uniqueValues = new HashSet<Integer>(positions);
		// if (uniqueValues.size() < positions.size()) {
		// for (int ii = 0; ii < positions.size(); ii++) {
		// for (int jj = ii + 1; jj < positions.size(); jj++) {
		// if (positions.get(jj).equals(positions.get(ii))) {
		//
		// if (author.get(jj).length() > author.get(ii)
		// .length()) {
		// author.remove(ii);
		// authors.remove(ii);
		// positions.remove(ii);
		// ii--;
		// } else {
		// author.remove(jj);
		// authors.remove(jj);
		// positions.remove(jj);
		// jj--;
		// }
		// }
		// }
		// }
		// }
		// int max = positions.get(positions.size() - 1);
		// int min = positions.get(0);
		// distance.add(0);
		// for (int ii = 1; ii < positions.size(); ii++) {
		// // normieren
		// int pos = positions.get(ii) - min;
		// int nextpos = positions.get(ii - 1) - min;
		// distance.add(Math.abs(pos - nextpos));
		// }
		// // some name fragments - use average to find
		// // faktor 100 ?
		// int factor = 100;
		// if (author.size() >= 3) {
		// factor = 50;
		// }
		// System.out.println(min + " to " + max + " - " + positions.size()
		// + " : " + positions.size() * factor);
		// if ((max - min) > positions.size() * factor) {
		// int range = 0;
		// for (int ii = 0; ii < distance.size(); ii++) {
		// range = range + distance.get(ii);
		// }
		// range = range / distance.size();
		// // System.out.println(range);
		// // thesis upper/lower bound enough
		// for (int ii = 0; ii < positions.size(); ii++) {
		// if (distance.get(ii) > range) {
		// if (ii < positions.size() - 1) {
		// if (distance.get(ii + 1) < range) {
		// author.remove(ii - 1);
		// authors.remove(ii - 1);
		// positions.remove(ii - 1);
		// distance.remove(ii - 1);
		// continue;
		// }
		// }
		// author.remove(ii);
		// authors.remove(ii);
		// positions.remove(ii);
		// distance.remove(ii);
		// }
		// }
		// }
		//
		// rs.close();
		// System.out.println("Found Authors: " + author + " | " + authors
		// + " in pdf:" + pdf.getFilename());
		// fillTDB(pdf,idPub , corpus,authors);
		// } else {
		// // found title in database
		// String sql = "SELECT title FROM " + dbName
		// + ".Publication WHERE idPublication=" + idPub;
		// ResultSet rs = stmt.executeQuery(sql);
		// while (rs.next()) {
		// pdf.setTitle(rs.getString("title"));
		// }
		// rs.close();
		// sql = "SELECT Author_idAuthor FROM "
		// + dbName
		// + ".Publication_has_Author WHERE Publication_idPublication="
		// + idPub;
		// rs = stmt.executeQuery(sql);
		// while (rs.next()) {
		// authors.add(rs.getInt("Author_idAuthor"));
		// }
		// rs.close();
		//
		// System.out.println("Found Paper: " + idPub + " in pdf:"
		// + pdf.getFilename() + " written by:" + authors);
		// fillTDB(pdf,idPub , corpus,authors);
		// }
		ArrayList<Integer> authors = new ArrayList<Integer>();
		for (int ii = 0; ii < pdf.getAuthors().size(); ii++) {
			authors.add(pdf.getAuthors().get(ii).getAuthorID());
		}
		fillTDB(pdf, pdf.getPublicationID(), corpus, authors);
	}

	public ArrayList<Author> createAllAuthors() throws SQLException {

		ArrayList<Author> authors = new ArrayList<Author>();
		connect = DriverManager.getConnection("jdbc:mysql://localhost/"
				+ dbName + "?" + "user=test&password=test");
		Statement state = connect.createStatement();

		// state.setFetchSize(100);
		ResultSet resultSetCategory = state.executeQuery("SELECT * FROM  "
				+ dbName + ".author");
		while (resultSetCategory.next()) {

			int id = resultSetCategory.getInt("idAuthor");
			// System.out.println(id);
			String name = resultSetCategory.getString("name");
			// String nameNorm = Normalizer.normalize(name,
			// Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
			Author aut = new Author(name, id);
			// System.out.println(pdf.getPublicationID());
			authors.add(aut);
		}
		resultSetCategory.close();
		state.close();
		return authors;

	}

	private void fillTDB(PDF pdf, int idPub, Corpus corpus,
			ArrayList<Integer> authors) throws SQLException {
		long pdfID = -1;
		long corpID = -1;
		corpID = addCorpus(corpus);

		if (pdf != null) {
			if (idPub > 0) {
				pdfID = addTPDF(corpID, idPub, pdf);
			} else {
				pdfID = addPDF(corpID, pdf);
			}

			if (pdfID > 0) {
				if (!authors.isEmpty()) {
					addPDFhasAuth(authors, pdfID);
				}
				if (!pdf.getWordOccList().isEmpty()) {
					ArrayList<Integer> defKeys = addCategory(pdf, pdfID);

					ArrayList<WordOcc> words = pdf.getWordOccList();
					ArrayList<Integer> genKeys = addKeywords(words, corpID,
							pdfID);

					addCathasKeys(defKeys, genKeys, pdfID);
				}

			}
		}

	}

	private void fillADB(PDF pdf, ArrayList<Integer> authors, Corpus corpus)
			throws SQLException {
		long pdfID = 0;
		long corpID = 0;
		corpID = addCorpus(corpus);

		if (pdf != null) {
			pdfID = addPDF(corpID, pdf);

			if (pdfID > 0) {
				// TODO Author duplicates care
				addPDFhasAuth(authors, pdfID);

				if (!pdf.getWordOccList().isEmpty()) {
					// TODO CHANGE UNIQUE CATEGORIES
					// ArrayList<Long> keyIDs = new ArrayList<Long>();
					ArrayList<Integer> defKeys = addCategory(pdf, pdfID);

					ArrayList<WordOcc> words = pdf.getWordOccList();
					ArrayList<Integer> genKeys = addKeywords(words, corpID,
							pdfID);

					addCathasKeys(defKeys, genKeys, pdfID);

				}
			}
		}

	}

	// TODO for some reason not all cats are added
	// TODO different solution to duplicates ! testing if connection already
	// exists
	private void addCathasKeys(ArrayList<Integer> defKeys,
			ArrayList<Integer> genKeys, long pdfID) throws SQLException {
		for (int ii = 0; ii < defKeys.size(); ii++) {
			for (int jj = 0; jj < genKeys.size(); jj++) {
				// TODO duplicate!!!
				preparedStatement = connect.prepareStatement("insert into  "
						+ dbName + ".KEYWORD_has_Category values (?, ?)"
						+ " ON DUPLICATE KEY UPDATE Category_idCategory=?",
						Statement.RETURN_GENERATED_KEYS);

				if (defKeys.isEmpty()) {
					preparedStatement.setInt(1, genKeys.get(jj));
					preparedStatement.setNull(2, java.sql.Types.INTEGER);
					preparedStatement.setInt(3, defKeys.get(ii));

				} else {
					preparedStatement.setInt(1, genKeys.get(jj));
					preparedStatement.setInt(2, defKeys.get(ii));
					preparedStatement.setInt(3, defKeys.get(ii));

				}
				try {
					preparedStatement.executeUpdate();

				} catch (Exception e) {
					// System.out.println(pdfID);
					e.printStackTrace();
				}
			}
		}

	}

	// ALSO GIVE CAT IDF AND SO ON
	private ArrayList<Integer> addKeywords(ArrayList<WordOcc> words,
			long corpID, long pdfID) throws SQLException {
		ArrayList<Integer> genKeys = new ArrayList<Integer>();
		for (int ii = 0; ii < words.size(); ii++) {
			preparedStatement = connect.prepareStatement("insert into  "
					+ dbName + ".Keyword values (default,?, ?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) corpID);
			preparedStatement.setInt(2, (int) pdfID);

			preparedStatement.setString(3, words.get(ii).getWord().getWord());
			preparedStatement.setInt(4, words.get(ii).getOcc());
			preparedStatement.setDouble(5, words.get(ii).getTfidf());
			preparedStatement.setDouble(6, words.get(ii).getIdf());
			preparedStatement.setDouble(7, words.get(ii).getTf());
			preparedStatement.executeUpdate();

			ResultSet rs = null;
			try {
				rs = preparedStatement.getGeneratedKeys();
				if (rs.next()) {
					genKeys.add((int) rs.getLong(1));
				}
				rs.close();
			} finally {
				// ... cleanup that will execute whether or not an
				// error
				// occurred ...
			}

		}

		return genKeys;
	}

	// TODO EXTEND CATEGORY AND RELEVANCE in main code and DB
	private ArrayList<Integer> addCategory(PDF pdf, long pdfID)
			throws SQLException {
		ArrayList<Integer> defKeys = new ArrayList<Integer>();
		if (!pdf.getGenericKeywords().isEmpty()) {
			for (int count = 0; count < pdf.getGenericKeywords().size(); count++) {
				int idDef = -1;
				String sqlT = "SELECT idCategory,name,normtitle FROM " + dbName
						+ ".Category";
				Statement stmt = connect.createStatement();
				ResultSet rsT = stmt.executeQuery(sqlT);
				while (rsT.next()) {
					int id = rsT.getInt("idCategory");
					String title = rsT.getString("name");
					String normtitle = rsT.getString("normtitle");

					// first case not nec is in second
					if (pdf.getGenericKeywords().get(count).getNormtitle()
							.equals(normtitle)
							|| (pdf.getGenericKeywords().get(count)
									.getAssociatedGCAT().equals(normtitle))) {
						idDef = id;
						// System.out.println("FOUND Category - " + title);
						break;
					}
				}
				rsT.close();
				//test here what happens -> categories has to have duplicates
				if (idDef < 0) {
					preparedStatement = connect.prepareStatement(
							"insert into  " + dbName
									+ ".CATEGORY values (default,?,?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, pdf.getGenericKeywords()
							.get(count).getTitle());
					preparedStatement.setDouble(2, pdf.getGenericKeywords()
							.get(count).getRelevance());
					preparedStatement.setString(3, pdf.getGenericKeywords()
							.get(count).getNormtitle());
					preparedStatement.setString(4, pdf.getGenericKeywords()
							.get(count).getAssociatedGCAT());

					try {
						preparedStatement.executeUpdate();

					} catch (Exception e) {
						// System.out.println(pdfID);
						e.printStackTrace();
					}
					ResultSet rs = null;
					try {
						rs = preparedStatement.getGeneratedKeys();
						if (rs.next()) {
							defKeys.add((int) rs.getLong(1));
						}
						rs.close();
					} finally {
						// ... cleanup that will execute whether or
						// not
						// an
						// error
						// occurred ...
					}
				} else {
					defKeys.add(idDef);
				}
			}
		}
		addPDFCat(defKeys, pdfID);
		return defKeys;
	}

	private void addPDFCat(ArrayList<Integer> defKeys, long pdfID)
			throws SQLException {
		for (int jj = 0; jj < defKeys.size(); jj++) {
			// on Duplicate ?
			preparedStatement = connect.prepareStatement("insert into  "
					+ dbName + ".PDF_has_Category values (?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) pdfID);
			preparedStatement.setInt(2, defKeys.get(jj));
			if ((pdfID == 4) || (pdfID == 5) || (pdfID == 19) || (pdfID == 2)
					|| (pdfID == 31) || (pdfID == 49) || (pdfID == 82)) {
				String timeLog = "DB_conflict_knownError";
				File logFile = new File(timeLog);

				// This will output the full path where the file will be written
				// to...
				try {
					System.out.println(logFile.getCanonicalPath());
					BufferedWriter writer;
					writer = new BufferedWriter(new FileWriter(logFile, true));
					writer.write("WAGASGASG - PDFid: " + pdfID + "; von file"
							+ fileNC + " with cats:" + defKeys.get(jj));
					writer.newLine();
					writer.close();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			try {
				preparedStatement.executeUpdate();

			} catch (Exception e) {
				// System.out.println(pdfID);
				String timeLog = "DB_conflict";
				File logFile = new File(timeLog);

				// This will output the full path where the file will be written
				// to...
				try {
					System.out.println(logFile.getCanonicalPath());
					BufferedWriter writer;
					writer = new BufferedWriter(new FileWriter(logFile, true));
					writer.write("File: " + pdfID + "; von file" + fileNC
							+ " with cats:" + defKeys.get(jj));
					writer.newLine();
					writer.close();
					e.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		}

	}

	// here on duplicate ignore and mark file
	private long addTPDF(long corpID, int idPub, PDF pdf) throws SQLException {
		int pdfID = -1;
		preparedStatement = connect.prepareStatement("insert into  " + dbName
				+ ".PDF values (default,?, ?, ?,?,?,?,?)"
				+ " ON DUPLICATE KEY update wordcount=?",
				Statement.RETURN_GENERATED_KEYS);

		preparedStatement.setInt(1, (int) corpID);
		preparedStatement.setInt(2, (int) idPub);
		preparedStatement.setString(3, pdf.getTitle());
		preparedStatement.setInt(4, pdf.getWordcount());
		preparedStatement.setString(5, pdf.getLanguage());
		preparedStatement.setInt(6, pdf.getPagecount());
		// TODO COLUMN COUNT DOESNT MATCH VALUE COUNT AT ROW 1
		preparedStatement.setString(7, pdf.getFilename());
		preparedStatement.setInt(8, pdf.getWordcount());
		/*
		 * preparedStatement.setString(2, pdf.getFirstPage().substring(0, 200));
		 * preparedStatement.setInt(3, pdf.getWordcount());
		 * preparedStatement.setString(4, pdf.getLanguage());
		 * preparedStatement.setInt(5, pdf.getWordcount());
		 */
		preparedStatement.executeUpdate();

		ResultSet rs = null;

		try {
			rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				pdfID = (int) rs.getLong(1);
			}
			rs.close();
		} finally {
			// ... cleanup that will execute whether or not an error
			// occurred ...
		}
		return pdfID;
	}

	private void addPDFhasAuth(ArrayList<Integer> authors, long pdfID)
			throws SQLException {

		for (int jj = 0; jj < authors.size(); jj++) {
			// on Duplicate ?
			preparedStatement = connect.prepareStatement("insert into  "
					+ dbName + ".PDF_has_Author values (?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) pdfID);
			preparedStatement.setInt(2, authors.get(jj));

			try {
				preparedStatement.executeUpdate();

			} catch (Exception e) {
				// System.out.println(pdfID);
				e.printStackTrace();
			}

		}

	}

	// add here test so no duplicates
	private long addPDF(long corpID, PDF pdf) throws SQLException {
		int pdfID = -1;
		preparedStatement = connect.prepareStatement("insert into " + dbName
				+ ".PDF values (default,?, ?, ?,?,?,?,?)"
				+ " ON DUPLICATE KEY update wordcount=?",
				Statement.RETURN_GENERATED_KEYS);
		preparedStatement.setInt(1, (int) corpID);
		// TODO setNULL
		preparedStatement.setNull(2, java.sql.Types.INTEGER);
		// pdf.getFirstPage().substring(0, 100)
		preparedStatement.setString(3, pdf.getTitle());
		preparedStatement.setInt(4, pdf.getWordcount());
		preparedStatement.setString(5, pdf.getLanguage());
		preparedStatement.setInt(6, pdf.getPagecount());
		// TODO COLUMN COUNT DOESNT MATCH VALUE COUNT AT ROW 1
		preparedStatement.setString(7, pdf.getFilename());
		preparedStatement.setInt(8, pdf.getWordcount());
		try {
			preparedStatement.executeUpdate();
			ResultSet rs = null;
			rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				pdfID = (int) rs.getLong(1);
			}

		} catch (Exception e) {
			System.out.println(pdf.getTitle() + " ID: " + pdfID);
			e.printStackTrace();
		}
		return pdfID;
	}

	private long addCorpus(Corpus corpus) throws SQLException {
		int corpID = -1;
		if (corpus != null) {

			Statement stmt = connect.createStatement();
			String sqlT = "SELECT id FROM " + dbName + ".corpus";
			ResultSet rsT = stmt.executeQuery(sqlT);

			if (rsT.next()) {
				corpID = (int) rsT.getLong(1);
			} else {
				preparedStatement = connect.prepareStatement("insert into  "
						+ dbName + ".CORPUS values (default,?, ?,?)"
						+ " ON DUPLICATE KEY update uniqueRow=?",
						Statement.RETURN_GENERATED_KEYS);
				// TODO include both formats of language DOC N !!!
				preparedStatement.setInt(1, corpus.getDocN("de"));
				preparedStatement.setInt(2, corpus.getDocN("en"));
				preparedStatement.setString(3, "yes");
				preparedStatement.setString(4, "yes");

				try {
					preparedStatement.executeUpdate();
					ResultSet rs = preparedStatement.getGeneratedKeys();
					if (rs.next()) {
						corpID = (int) rs.getLong(1);
					}
				} catch (Exception e) {
					// System.out.println(corpus.getDocN());
					e.printStackTrace();
				}
				// onDuplicate increase occurence
				addGlobalCategory(corpus, corpID);
			}
		}

		return corpID;
	}

	// TODO SAVE GLOBAL RELEVANCE ?
	// TODO DUPLICATE PDF
	private void addGlobalCategory(Corpus corpus, int corpID)
			throws SQLException {
		// NOT NECESSARY
		ArrayList<Integer> gCids = new ArrayList<Integer>();
		Statement stmt = connect.createStatement();
		String sqlT = "SELECT idGlobalCategory, title,normtitle FROM " + dbName
				+ ".GlobalCategory";
		ResultSet rsT = stmt.executeQuery(sqlT);
		int id = -1;

		boolean found = false;
		for (int ii = 0; ii < corpus.getGlobalCategoryCatalog().size(); ii++) {
			if (rsT.next()) {
				id = rsT.getInt("idGlobalCategory");
			}
			if (id != -1) {
				while (rsT.next()) {
					String normtitle = rsT.getString("normtitle");

					if (corpus.getGlobalCategoryCatalog().get(ii).getCategory()
							.getNormtitle().contains(normtitle)) {
						gCids.add(id);

						addCatKeywords(id, corpus.getGlobalCategoryCatalog()
								.get(ii).getKeywordList());
						// System.out.println("FOUND Category - " + title);
						found = true;
						break;
					}

				}
			}
			if (!found) {

				preparedStatement = connect.prepareStatement("insert into  "
						+ dbName + ".GlobalCategory values (default,?, ?,?)",
						Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setInt(1, corpID);
				preparedStatement.setString(2, corpus
						.getGlobalCategoryCatalog().get(ii).getCategory()
						.getTitle());
				preparedStatement.setString(3, corpus
						.getGlobalCategoryCatalog().get(ii).getCategory()
						.getNormtitle());
				try {
					preparedStatement.executeUpdate();
					ResultSet rs = preparedStatement.getGeneratedKeys();
					if (rs.next()) {
						id = (int) rs.getLong(1);
						gCids.add(id);
					}
				} catch (Exception e) {
					// System.out.println(corpus.getDocN());
					e.printStackTrace();
				}

				addCatKeywords(id, corpus.getGlobalCategoryCatalog().get(ii)
						.getKeywordList());
			} else {
				found = false;
			}
		}

	}

	// addCatKeywords()
	// TODO Auto-generated method stub

	// geht das so ?=
	// ALSO GIVE IDF TF AND SO ON ?
	private void addCatKeywords(int id, ArrayList<WordOcc> keywordList)
			throws SQLException {

		for (int ii = 0; ii < keywordList.size(); ii++) {
			preparedStatement = connect.prepareStatement("insert into "
					+ dbName + ".Cat_Keyw values (default,?, ?,?,?)"
					+ " ON DUPLICATE KEY update occ=occ+"
					+ keywordList.get(ii).getOcc(),
					Statement.RETURN_GENERATED_KEYS);

			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, keywordList.get(ii).getWord()
					.getWord());
			preparedStatement.setInt(3, keywordList.get(ii).getOcc());
			preparedStatement.setDouble(4, keywordList.get(ii).getCatTFIDF());
			try {
				preparedStatement.executeUpdate();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
