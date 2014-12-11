package master.keyEx.models;

public class Category {
private String title;
private int relevance;
	public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
	public Category() {
		// TODO Auto-generated constructor stub
	}
	public Category(String name) {
		this.setTitle(name);
		this.relevance =1;
	}
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	public void incRelevance() {
		this.relevance++;
	}

}
