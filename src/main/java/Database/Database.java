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

		if (corpus != null) {
			Statement stmt = connect.createStatement();
			String sqlT = "SELECT id FROM corpus.corpus";
			ResultSet rsT = stmt.executeQuery(sqlT);
			if (rsT.next()) {
				corpID = rsT.getLong(1);
			} else {
				preparedStatement = connect.prepareStatement(
						"insert into  CORPUS.CORPUS values (?, ?)"
								+ " ON DUPLICATE KEY update id=?",
						Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setInt(1, 1);
				preparedStatement.setInt(2, corpus.getDocN());
				try {
					preparedStatement.executeUpdate();
				} catch (Exception e) {
					System.out.println(corpus.getDocN());
					e.printStackTrace();
				}
				ResultSet rs = null;

				try {
					rs = preparedStatement.getGeneratedKeys();
					if (rs.next()) {
						corpID = rs.getLong(1);
					}
				} finally {
					// ... cleanup that will execute whether or not an error
					// occurred ...
				}
			}
			// if (corpID == -1) {
			// String sql = "SELECT idAuthor,name FROM corpus.Author";
			// ResultSet rst = preparedStatement.executeQuery(sql);
			// if (rs.next()) {
			// corpID = rs.getLong(1);
			// }
			// }
		}
		if (pdf != null) {

			preparedStatement = connect.prepareStatement(
					"insert into  CORPUS.PDF values (default, ?,?, ?,?,?)"
							+ " ON DUPLICATE KEY update wordcount=?",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) corpID);
			preparedStatement.setInt(2, (int) idPub);
			preparedStatement
					.setString(2, pdf.getFirstPage().substring(0, 200));
			preparedStatement.setInt(3, pdf.getWordcount());
			preparedStatement.setString(4, pdf.getLanguage());
			preparedStatement.executeUpdate();

			ResultSet rs = null;

			try {
				rs = preparedStatement.getGeneratedKeys();
				if (rs.next()) {
					pdfID = rs.getLong(1);
				}
			} finally {
				// ... cleanup that will execute whether or not an error
				// occurred ...
			}

			if (!pdf.getWordOccList().isEmpty()) {
				// ArrayList<Long> keyIDs = new ArrayList<Long>();
				ArrayList<Integer> defKeys = new ArrayList<Integer>();
				ArrayList<Integer> genKeys = new ArrayList<Integer>();
				if (!pdf.getGenericKeywords().isEmpty()) {
					for (int count = 0; count < pdf.getGenericKeywords().size(); count++) {
						preparedStatement = connect.prepareStatement(
								"insert into  CORPUS.CATEGORY values (default,?)"
										+ " ON DUPLICATE KEY update name=?",
								Statement.RETURN_GENERATED_KEYS);
						preparedStatement.setString(1, pdf.getGenericKeywords()
								.get(count));

						try {
							preparedStatement.executeUpdate();

						} catch (Exception e) {
							System.out.println(pdfID);
							e.printStackTrace();
						}
						rs = null;
						try {
							rs = preparedStatement.getGeneratedKeys();
							if (rs.next()) {
								defKeys.add((int) rs.getLong(1));
							}
						} finally {
							// ... cleanup that will execute whether or not an
							// error
							// occurred ...
						}
					}
				}
				ArrayList<WordOcc> words = pdf.getWordOccList();
				for (int ii = 0; ii < words.size(); ii++) {
					preparedStatement = connect
							.prepareStatement(
									"insert into  CORPUS.Keyword values (default,?, ?,?,?,?,?,?)",
									Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setInt(1, (int) corpID);
					preparedStatement.setInt(2, (int) pdfID);

					preparedStatement.setString(3, words.get(ii).getWord()
							.getWord());
					preparedStatement.setInt(4, words.get(ii).getOcc());
					preparedStatement.setDouble(5, words.get(ii).getTfidf());
					preparedStatement.setDouble(6, words.get(ii).getIdf());
					preparedStatement.setDouble(7, words.get(ii).getTf());
					preparedStatement.executeUpdate();

					rs = null;
					try {
						rs = preparedStatement.getGeneratedKeys();
						if (rs.next()) {
							genKeys.add((int) rs.getLong(1));
						}
					} finally {
						// ... cleanup that will execute whether or not an error
						// occurred ...
					}
				}

				for (int ii = 0; ii < defKeys.size(); ii++) {
					for (int jj = 0; jj < genKeys.size(); jj++) {
						preparedStatement = connect
								.prepareStatement(
										"insert into  CORPUS.KEYWORDS_has_Category values (?, ?)",
										Statement.RETURN_GENERATED_KEYS);
						preparedStatement.setInt(1, defKeys.get(ii));
						preparedStatement.setInt(2, genKeys.get(jj));
						preparedStatement.executeUpdate();
						try {
							preparedStatement.executeUpdate();

						} catch (Exception e) {
							System.out.println(pdfID);
							e.printStackTrace();
						}
					}
				}
			}
		}
		// if (!pdf.getWordOccList().isEmpty()) {
		// ArrayList<Long> keyIDs = new ArrayList<Long>();
		// ArrayList<WordOcc> words = pdf.getWordOccList();
		// for (int ii = 0; ii < words.size(); ii++) {
		// preparedStatement = connect
		// .prepareStatement("insert into  CORPUS.Keyword values (default,?, ?,?,?,?,?,?,?)");
		// preparedStatement.setInt(1, (int) corpID);
		// preparedStatement.setInt(2, (int) pdfID);
		// preparedStatement.setNull(3, java.sql.Types.INTEGER);
		// preparedStatement.setString(4, words.get(ii).getWord()
		// .getWord());
		// preparedStatement.setInt(5, words.get(ii).getOcc());
		// preparedStatement.setDouble(6, words.get(ii).getTfidf());
		// preparedStatement.setDouble(7, words.get(ii).getIdf());
		// preparedStatement.setDouble(8, words.get(ii).getTf());
		// preparedStatement.executeUpdate();
		//
		// ResultSet rs = null;
		//
		// try {
		// rs = preparedStatement.getGeneratedKeys();
		// if (rs.next()) {
		// keyIDs.add(rs.getLong(1));
		// }
		// } finally {
		// // ... cleanup that will execute whether or not an error
		// // occurred ...
		// }
		// }
		// }

	}

	private void fillADB(PDF pdf, ArrayList<Integer> authors, Corpus corpus)
			throws SQLException {
		long pdfID = 0;
		long corpID = 0;

		if (corpus != null) {

			Statement stmt = connect.createStatement();
			String sqlT = "SELECT id FROM corpus.corpus";
			ResultSet rsT = stmt.executeQuery(sqlT);
			if (rsT.next()) {
				corpID = rsT.getLong(1);
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
						corpID = rs.getLong(1);
					}
				} catch (Exception e) {
					System.out.println(corpus.getDocN());
					e.printStackTrace();
				}
			}
		}
		if (pdf != null) {
			preparedStatement = connect.prepareStatement(
					"insert into  CORPUS.PDF values (default,?, ?, ?,?,?)"
							+ " ON DUPLICATE KEY update wordcount=?",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, (int) corpID);
			// TODO setNULL
			preparedStatement.setNull(2, java.sql.Types.INTEGER);
			preparedStatement
					.setString(3, pdf.getFirstPage().substring(0, 100));
			preparedStatement.setInt(4, pdf.getWordcount());
			preparedStatement.setString(5, pdf.getLanguage());
			preparedStatement.setInt(6, pdf.getWordcount());
			try {
				preparedStatement.executeUpdate();
				ResultSet rs = null;
				rs = preparedStatement.getGeneratedKeys();
				if (rs.next()) {
					pdfID = rs.getLong(1);
				}

			} catch (Exception e) {

				e.printStackTrace();
			}

			// TODO Author duplicates care
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
			if (!pdf.getWordOccList().isEmpty()) {
				// TODO CHANGE UNIQUE CATEGORIES
				// ArrayList<Long> keyIDs = new ArrayList<Long>();
				ArrayList<Integer> defKeys = new ArrayList<Integer>();
				ArrayList<Integer> genKeys = new ArrayList<Integer>();
				if (pdf.getGenericKeywords() != null) {
					if (!pdf.getGenericKeywords().isEmpty()) {
						for (int count = 0; count < pdf.getGenericKeywords()
								.size(); count++) {
							preparedStatement = connect
									.prepareStatement(
											"insert into  CORPUS.CATEGORY values (default,?)"
													+ " ON DUPLICATE KEY update name=?",
											Statement.RETURN_GENERATED_KEYS);
							preparedStatement.setString(1, pdf
									.getGenericKeywords().get(count));
							preparedStatement.setString(2, pdf
									.getGenericKeywords().get(count));

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
							} finally {
								// ... cleanup that will execute whether or not
								// an
								// error
								// occurred ...
							}
						}
					}
				}
				ArrayList<WordOcc> words = pdf.getWordOccList();
				for (int ii = 0; ii < words.size(); ii++) {
					preparedStatement = connect
							.prepareStatement(
									"insert into  CORPUS.Keyword values (default,?, ?,?,?,?,?,?)",
									Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setInt(1, (int) corpID);
					preparedStatement.setInt(2, (int) pdfID);

					preparedStatement.setString(3, words.get(ii).getWord()
							.getWord());
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
					} finally {
						// ... cleanup that will execute whether or not an error
						// occurred ...
					}
				}

				for (int ii = 0; ii < defKeys.size(); ii++) {
					for (int jj = 0; jj < genKeys.size(); jj++) {
						preparedStatement = connect
								.prepareStatement(
										"insert into  CORPUS.KEYWORDS_has_Category values (?, ?)",
										Statement.RETURN_GENERATED_KEYS);
						preparedStatement.setInt(1, defKeys.get(ii));
						preparedStatement.setInt(2, genKeys.get(jj));
						preparedStatement.executeUpdate();
						try {
							preparedStatement.executeUpdate();

						} catch (Exception e) {
							System.out.println(pdfID);
							e.printStackTrace();
						}
					}
				}
			}
		}

	}
}
