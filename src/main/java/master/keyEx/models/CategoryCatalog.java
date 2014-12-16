package master.keyEx.models;

import java.util.ArrayList;

public class CategoryCatalog {
private Category category;
private ArrayList<WordOcc> keywordList = new ArrayList<WordOcc>();
	public CategoryCatalog(Category cat, ArrayList<WordOcc> keys) {
		this.category = cat;
		this.setKeywordList(keys);
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

}
