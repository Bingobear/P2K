package Database;

import java.lang.AutoCloseable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import Database.model.*;
import master.keyEx.models.Corpus;
import master.keyEx.models.PDF;
import master.keyEx.models.WordOcc;

public class Database {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	@SuppressWarnings("deprecation")
	public void readDataBase() throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/feedback?"
							+ "user=test&password=test");

			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			resultSet = statement
					.executeQuery("select * from FEEDBACK.COMMENTS");
			writeResultSet(resultSet);

			// preparedStatements can use variables and are more efficient
			preparedStatement = connect
					.prepareStatement("insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)");
			// "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
			// parameters start with 1
			preparedStatement.setString(1, "Test");
			preparedStatement.setString(2, "TestEmail");
			preparedStatement.setString(3, "TestWebpage");
			preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
			preparedStatement.setString(5, "TestSummary");
			preparedStatement.setString(6, "TestComment");
			preparedStatement.executeUpdate();

			preparedStatement = connect
					.prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
			resultSet = preparedStatement.executeQuery();
			writeResultSet(resultSet);

			// remove again the insert comment
			preparedStatement = connect
					.prepareStatement("delete from FEEDBACK.COMMENTS where myuser= ? ; ");
			preparedStatement.setString(1, "Test");
			preparedStatement.executeUpdate();

			resultSet = statement
					.executeQuery("select * from FEEDBACK.COMMENTS");
			writeMetaData(resultSet);

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	private void writeMetaData(ResultSet resultSet) throws SQLException {
		// now get some metadata from the database
		System.out.println("The columns in the table are: ");
		System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			System.out.println("Column " + i + " "
					+ resultSet.getMetaData().getColumnName(i));
		}
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		// resultSet is initialised before the first data set
		while (resultSet.next()) {
			// it is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g., resultSet.getSTring(2);
			String user = resultSet.getString("myuser");
			String website = resultSet.getString("webpage");
			String summary = resultSet.getString("summary");
			Date date = resultSet.getDate("datum");
			String comment = resultSet.getString("comments");
			System.out.println("User: " + user);
			System.out.println("Website: " + website);
			System.out.println("Summary: " + summary);
			System.out.println("Date: " + date);
			System.out.println("Comment: " + comment);
		}
	}

	// you need to close all three to make sure
	private void close() {
		close(resultSet);
		close(statement);
		close(connect);
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
		connect = DriverManager.getConnection("jdbc:mysql://localhost/corpus?"
				+ "user=test&password=test");
		Statement stmt = connect.createStatement();
		int idPub = -1;
		String sqlT = "SELECT idPublication,title FROM corpus.Publication";
		ResultSet rsT = stmt.executeQuery(sqlT);
		while (rsT.next()) {
			int id = rsT.getInt("idPublication");
			String title = rsT.getString("title");
			if (pdf.getFirstPage().contains(title)) {
				idPub = id;
				System.out.println("FOUND Title - " + title);
				break;
			}
		}
		rsT.close();
		ArrayList<Integer> authors = new ArrayList<Integer>();
		if (idPub < 0) {
			// not in BTH database
			String sql = "SELECT idAuthor,name FROM corpus.Author";
			ResultSet rs = stmt.executeQuery(sql);
			// STEP 5: Extract data from result set

			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("idAuthor");
				String name = rs.getString("name");
				ArrayList<String> nameparts = new ArrayList<String>();
				for (String retval : name.split(",")) {
					nameparts.add(retval);
				}
				// if(name.equals("Thombansen, Ulrich")){
				// String stop = "kacke";
				// // name="Thombansen";
				// }
				for (int count = 0; count < nameparts.size() - 1; count++) {
					if (pdf.getFirstPage().contains(nameparts.get(count))) {
						authors.add(id);
						System.out.println("FOUND Author - " + name
								+ pdf.getFirstPage().substring(0, 10));
					}
				}

			}
			rs.close();
			fillADB(pdf, authors, corpus);
		} else {
			// found title in database
			fillTDB(pdf, idPub, corpus);
		}
	}

	private void fillTDB(PDF pdf, int idPub, Corpus corpus) throws SQLException {
		long pdfID = -1;
		long corpID = -1;
		corpID = addCorpus(corpus);

		if (pdf != null) {
			pdfID = addTPDF(corpID, idPub, pdf);

			if (pdfID > 0) {
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
//TODO for some reason not all cats are added
	private void addCathasKeys(ArrayList<Integer> defKeys,
			ArrayList<Integer> genKeys, long pdfID) throws SQLException {
		for (int ii = 0; ii < defKeys.size(); ii++) {
			for (int jj = 0; jj < genKeys.size(); jj++) {
				// TODO duplicate!!!
				preparedStatement = connect
						.prepareStatement(
								"insert into  CORPUS.KEYWORD_has_Category values (?, ?)",
								Statement.RETURN_GENERATED_KEYS);
				if (ii == 3) {
					System.out.println(jj);
				}
				if (defKeys.isEmpty()) {
					preparedStatement.setInt(1, genKeys.get(jj));
					preparedStatement.setNull(2, java.sql.Types.INTEGER);

				} else {
					preparedStatement.setInt(1, genKeys.get(jj));
					preparedStatement.setInt(2, defKeys.get(ii));

				}
				try {
					preparedStatement.executeUpdate();

				} catch (Exception e) {
					System.out.println(pdfID);
					e.printStackTrace();
				}
			}
		}

	}

	private ArrayList<Integer> addKeywords(ArrayList<WordOcc> words,
			long corpID, long pdfID) throws SQLException {
		ArrayList<Integer> genKeys = new ArrayList<Integer>();
		for (int ii = 0; ii < words.size(); ii++) {
			preparedStatement = connect
					.prepareStatement(
							"insert into  CORPUS.Keyword values (default,?, ?,?,?,?,?,?)",
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

	private ArrayList<Integer> addCategory(PDF pdf, long pdfID)
			throws SQLException {
		ArrayList<Integer> defKeys = new ArrayList<Integer>();
		if (!pdf.getGenericKeywords().isEmpty()) {
			for (int count = 0; count < pdf.getGenericKeywords().size(); count++) {
				int idDef = -1;
				String sqlT = "SELECT idCategory,name FROM corpus.Category";
				Statement stmt = connect.createStatement();
				ResultSet rsT = stmt.executeQuery(sqlT);
				while (rsT.next()) {
					int id = rsT.getInt("idCategory");
					String title = rsT.getString("name");
					if (pdf.getGenericKeywords().get(count).contains(title)) {
						idDef = id;
						System.out.println("FOUND Category - " + title);
						break;
					}
				}
				rsT.close();
				if (idDef < 0) {
					preparedStatement = connect.prepareStatement(
							"insert into  CORPUS.CATEGORY values (default,?)",
							Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, pdf.getGenericKeywords()
							.get(count));
					try {
						preparedStatement.executeUpdate();

					} catch (Exception e) {
						System.out.println(pdfID);
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
		return defKeys;
	}

	private long addTPDF(long corpID, int idPub, PDF pdf) throws SQLException {
		int pdfID = -1;
		preparedStatement = connect.prepareStatement(
				"insert into  CORPUS.PDF values (default, ?,?, ?,?,?)"
						+ " ON DUPLICATE KEY update wordcount=?",
				Statement.RETURN_GENERATED_KEYS);
		preparedStatement.setInt(1, (int) corpID);
		preparedStatement.setInt(2, (int) idPub);
		preparedStatement.setString(2, pdf.getFirstPage().substring(0, 200));
		preparedStatement.setInt(3, pdf.getWordcount());
		preparedStatement.setString(4, pdf.getLanguage());
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
			preparedStatement = connect.prepareStatement(
					"insert into  CORPUS.PDF_has_Author values (?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) pdfID);
			preparedStatement.setInt(2, authors.get(jj));

			try {
				preparedStatement.executeUpdate();

			} catch (Exception e) {
				System.out.println(pdfID);
				e.printStackTrace();
			}

		}

	}

	private long addPDF(long corpID, PDF pdf) throws SQLException {
		int pdfID = -1;
		preparedStatement = connect.prepareStatement(
				"insert into  CORPUS.PDF values (default,?, ?, ?,?,?)"
						+ " ON DUPLICATE KEY update wordcount=?",
				Statement.RETURN_GENERATED_KEYS);
		preparedStatement.setInt(1, (int) corpID);
		// TODO setNULL
		preparedStatement.setNull(2, java.sql.Types.INTEGER);
		preparedStatement.setString(3, pdf.getFirstPage().substring(0, 100));
		preparedStatement.setInt(4, pdf.getWordcount());
		preparedStatement.setString(5, pdf.getLanguage());
		preparedStatement.setInt(6, pdf.getWordcount());
		try {
			preparedStatement.executeUpdate();
			ResultSet rs = null;
			rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				pdfID = (int) rs.getLong(1);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return pdfID;
	}

	private long addCorpus(Corpus corpus) throws SQLException {
		int corpID = -1;
		if (corpus != null) {

			Statement stmt = connect.createStatement();
			String sqlT = "SELECT id FROM corpus.corpus";
			ResultSet rsT = stmt.executeQuery(sqlT);

			if (rsT.next()) {
				corpID = (int) rsT.getLong(1);
			} else {
				preparedStatement = connect.prepareStatement(
						"insert into  CORPUS.CORPUS values (default,?, ?)"
								+ " ON DUPLICATE KEY update uniqueRow=?",
						Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setInt(1, corpus.getDocN());
				preparedStatement.setString(2, "yes");
				preparedStatement.setString(3, "yes");

				try {
					preparedStatement.executeUpdate();
					ResultSet rs = preparedStatement.getGeneratedKeys();
					if (rs.next()) {
						corpID = (int) rs.getLong(1);
					}
				} catch (Exception e) {
					System.out.println(corpus.getDocN());
					e.printStackTrace();
				}
			}
		}
		return corpID;
	}
}
