package Database;
import java.util.ArrayList;

import master.keyEx.models.Corpus;
import master.keyEx.models.PDF;



/**Interface to fill Database with extracted corpus, publication, ... information
 * @author Simon Bruns
 *
 */
public class DBInterface {

	public DBInterface() {

	}



	// mods.getModses().size()
	public static void fillDB(Corpus corpus) {
		int test = 0;
		ArrayList<PDF> pdfList = corpus.getPdfList();
		for (int counter = 0; counter < pdfList.size(); counter++) {
			test = counter;
			try {
				Database dat = new Database();
				dat.fillDB(pdfList.get(counter),corpus);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}

}
