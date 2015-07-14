package master.keyEx.models;

import java.util.ArrayList;

/** Category Catalog (consist of the category and all its associated words)
 * @author Simon
 *
 */
public class CategoryCatalog {
private Category category;
private ArrayList<WordOcc> keywordList = new ArrayList<WordOcc>();
//Move this to calculation rather than inremental (at the end calc totalW)
private int totalW=0;
	public CategoryCatalog(Category cat, ArrayList<WordOcc> keys) {
		this.category = cat;
		this.setKeywordList(keys);
		for(WordOcc key : keys){
			this.totalW = this.totalW+key.getOcc();
		}
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public ArrayList<WordOcc> getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(ArrayList<WordOcc> keywordList) {
		this.keywordList = keywordList;
	}
	public int getTotalW() {
		return totalW;
	}
	public void setTotalW(int totalW) {
		this.totalW = totalW;
	}
	
	public void incTotalW(int totalW) {
		this.totalW = this.totalW+totalW;
	}
	public void calculateTF_IDF() {
		WordOcc word = null;
		for (int ii = 0; ii < keywordList.size(); ii++) {
			word = keywordList.get(ii);
			double tf = AlgorithmUtil
					.calcTF((double) word.getOcc(), (double) this.totalW);
			word.setCatTF(tf);
			double tfidf = AlgorithmUtil.calcTFIDF(tf, (double) word.getCatIDF());
			word.setCatTFIDF(tfidf);
		}
		
	}

}
