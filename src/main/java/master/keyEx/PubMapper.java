package master.keyEx;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import Database.model.Author;
import Database.Database;
import master.keyEx.models.Corpus;
import master.keyEx.models.PDF;
import master.keyEx.models.Publication;

public class PubMapper {
	private static ArrayList<Publication> pubs = new ArrayList<Publication>();

	public PubMapper() {
		// TODO Auto-generated constructor stub
	}

	public static Corpus enrichCorpus(Corpus corpus) {
		Database db = new Database();
		try {
			pubs = db.getAllPub();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<PDF> pdfL = corpus.getPdfList();
		for (int ii = 0; ii < pdfL.size(); ii++) {
			int pubID = getPub(pdfL.get(ii));
			if (pubID > 0) {
				Publication pub = null;
				for (int jj = 0; jj < pubs.size(); jj++) {
					if (pubs.get(jj).getPubID() == pubID) {
						pub = pubs.get(jj);
						break;
					}
				}
				pdfL.get(ii).setPublicationID(pub.getPubID());
				pdfL.get(ii).setAuthors(pub.getAuthors());
			} else {
				pdfL.get(ii).setPublicationID(pubID);
				pdfL.get(ii).setAuthors(getPDFAuthors(pdfL.get(ii)));

			}
		}
		corpus.setPdfList(pdfL);
		return corpus;
	}

	private static ArrayList<Author> getPDFAuthors(PDF pdf) {
		ArrayList<Author> authorRes = new ArrayList<Author>();
		String titlepage = "";
		String authorFake = "";
		ArrayList<Integer> authors = new ArrayList<Integer>();
		if (pdf.getTitle().isEmpty()) {
			titlepage = pdf.getFirstPage().toLowerCase();
		} else {
			authorFake = pdf.getTitle();
			titlepage = pdf.getTitle().toLowerCase();
		}

		// STEP 5: Extract data from result set
		ArrayList<String> author = new ArrayList<String>();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		ArrayList<Integer> distance = new ArrayList<Integer>();
		Database db = new Database();
		ArrayList<Author> authorsall = null;
		try {
			authorsall = db.createAllAuthors();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Author auth : authorsall) {

			// String name = rs.getString("name");
			ArrayList<String> nameparts = new ArrayList<String>();
			for (String retval : auth.getName().split(",")) {
				nameparts.add(retval);
			}

			for (int count = 0; count < nameparts.size() - 1; count++) {

				if (pdf.getFirstPage().contains(nameparts.get(count))) {
					if (nameparts.get(count).equals("Li")) {
						int test = 0;
					}
					if (authorFake.contains(nameparts.get(count))) {
						continue;
					}
					int pos = pdf.getFirstPage().indexOf(nameparts.get(count));

					if (!positions.isEmpty()) {
						int length = positions.size();
						for (int kk = 0; kk < length; kk++) {

							if ((positions.get(kk) > pos)) {
								positions.add(kk, pos);
								authors.add(kk, auth.getAuthorID());
								author.add(kk, nameparts.get(count));
								// letztes element
							} else if (kk == length - 1) {
								positions.add(pos);
								authors.add(auth.getAuthorID());
								author.add(nameparts.get(count));
							}
						}

					} else {
						authors.add(auth.getAuthorID());
						positions.add(pos);
						System.out.println(nameparts.get(count) + ":" + pos);
						author.add(nameparts.get(count));
					}

					/*
					 * if (pos < min) { min = pos; } if (pos > max) { max = pos;
					 * }
					 */

					// System.out.println("FOUND Author - " + name
					// + pdf.getFirstPage().substring(0, 10));
				}
			}
			// }

		}

		// Create subfunction overlapping names
		HashSet<Integer> uniqueValues = new HashSet<Integer>(positions);

		if (uniqueValues.size() < positions.size()) {
			for (int ii = 0; ii < positions.size(); ii++) {
				for (int jj = ii + 1; jj < positions.size(); jj++) {
					System.out.println(ii+" - "+jj+"<"+ positions.size());
					if (positions.get(jj).equals(positions.get(ii))) {

						if (author.get(jj).length() > author.get(ii).length()) {
							author.remove(ii);
							authors.remove(ii);
							positions.remove(ii);
							
							ii--;
							if(ii<0){
								ii=0;
							}
						} else {
							author.remove(jj);
							authors.remove(jj);
							positions.remove(jj);
							jj--;
							if(jj<0){
								jj=0;
							}
						}
					}
				}
			}
		}
		if (author.size() > 2) {
			int max = positions.get(positions.size() - 1);
			int min = positions.get(0);
			distance.add(0);
			for (int ii = 1; ii < positions.size(); ii++) {
				// normieren
				int pos = positions.get(ii) - min;
				int nextpos = positions.get(ii - 1) - min;
				distance.add(Math.abs(pos - nextpos));
			}
			// problem pdf format

			// some name fragments - use average to find
			// faktor 100 ?
			int factor = 100;
			if (author.size() >= 3) {
				factor = 50;
			}
			System.out.println(min + " to " + max + " - " + positions.size()
					+ " : " + positions.size() * factor);
			if ((max - min) > positions.size() * factor) {
				int range = 0;
				for (int ii = 0; ii < distance.size(); ii++) {
					range = range + distance.get(ii);
				}
				range = range / distance.size();
				// System.out.println(range);
				// thesis upper/lower bound enough
				for (int ii = 0; ii < positions.size(); ii++) {
					if (distance.get(ii) > range) {
						if (ii < positions.size() - 1) {
							if (distance.get(ii + 1) < range) {
								author.remove(ii - 1);
								authors.remove(ii - 1);
								positions.remove(ii - 1);
								distance.remove(ii - 1);
								continue;
							}
						}
						author.remove(ii);
						authors.remove(ii);
						positions.remove(ii);
						distance.remove(ii);
					}
				}
			}
		}

		System.out.println("Found Authors: " + author + " | " + authors
				+ " in pdf:" + pdf.getFilename());
		for (int ii = 0; ii < author.size(); ii++) {
			authorRes.add(new Author(author.get(ii), authors.get(ii)));
		}
		return authorRes;
	}

	private static int getPub(PDF pdf) {
		String fileNC = pdf.getFilename();
		// stupid name bug -> resolve with duplicate kick
		if (fileNC.equals("smarthealth_workshop_summary.pdf")) {
			return -1;
		}
		int idPub = -1;
		String titlepage = "";
		if (pdf.getTitle().isEmpty()) {
			titlepage = pdf.getFirstPage().toLowerCase();
		} else {
			titlepage = pdf.getTitle().toLowerCase();
		}

		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Integer> titleCand = new ArrayList<Integer>();
		for (int jj = 0; jj < pubs.size(); jj++) {
			int id = pubs.get(jj).getPubID();
			String original = pubs.get(jj).getTitle();
			String title = original.toLowerCase();
			int partitionSize = 0;
			int length = title.length();
			if(length<3){
				continue;
			}
			if (length < 10) {
			} else if (length < 20) {
				partitionSize = 6;
			} else if (length < 30) {
				partitionSize = 10;
			} else {
				partitionSize = 16;
			}
			if (partitionSize == 0) {
				if (titlepage.contains(title)) {
					System.out.println("found shortTitle:" + title);
					idPub = id;
					break;
				}
			} else {
				int dividor = length / partitionSize;
				ArrayList<String> subs = new ArrayList<String>();
				// ka geht das ?
				for (int ii = 0; ii < length - partitionSize; ii = ii
						+ partitionSize) {
					subs.add(title.substring(ii, ii + partitionSize));
				}
				if (length - dividor * partitionSize > partitionSize / 2) {
					subs.add(title.substring(dividor * partitionSize, length));
				}
				for (int ii = 0; ii < subs.size(); ii++) {

					if (titlepage.contains(subs.get(ii))) {
						// delete when bth optimized

						titles.add(title);
						titleCand.add(id);
						System.out
								.println("found title:" + subs.get(ii) + " = "
										+ original + " = " + subs + " ///" + id);

					}
				}
			}

		}

		if (titleCand.size() > 1) {
			int[] occ = new int[titleCand.size()];
			Arrays.fill(occ, 1);
			int max = 1;
			int maxCan = -1;
			ArrayList<Integer> open = new ArrayList<Integer>();
			boolean unique = false;
			for (int ii = 0; ii < titleCand.size(); ii++) {
				for (int jj = ii + 1; jj < titleCand.size(); jj++) {
					// ==
					if (titleCand.get(ii).equals(titleCand.get(jj))) {
						occ[ii] = occ[ii] + 1;
						// missing scenario max occ by more than one
						if (occ[ii] > max) {
							max = occ[ii];
							idPub = titleCand.get(ii);
							maxCan = titleCand.get(ii);
							open.add(ii);
							unique = true;
						} else if ((occ[ii] == max) && (occ[ii] > 1)
								&& (titleCand.get(ii) != maxCan)) {
							unique = false;
						}
					}
				}
			}
			System.out.println(idPub + " - occ: " + max);
			if (!unique) {
				idPub = -1;
			}
			System.out.println(idPub + " - occ: " + max + " name:" + fileNC
					+ " #pubs" + pubs.size());
		}
		return idPub;
	}

}
