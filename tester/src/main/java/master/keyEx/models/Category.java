package master.keyEx.models;

public class Category {
private String title;
private int relevance;
private int totalwords;
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
	
	public void incRelevance(int i) {
		//this.relevance=this.relevance+i;
		this.relevance=this.relevance+1;
	}
	public int getTotalwords() {
		return totalwords;
	}
	public void setTotalwords(int totalwords) {
		this.totalwords = totalwords;
	}
	public void incTotalwords(int i) {
		//this.relevance=this.relevance+i;
		this.totalwords=this.totalwords+1;
	}

}
