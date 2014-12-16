package master.keyEx.models;

public class Category {
private String title;
private int wOcc;
private int totalwords;
private double relevance;
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
		this.wOcc =0;
	}
	public int getRelevance() {
		return wOcc;
	}
	
	//TODO Not relevance but #t in cat
	public void setwOcc(int relevance) {
		this.wOcc = relevance;
	}
	
	public void incwOcc(int i) {
		//this.relevance=this.relevance+1;
		this.wOcc=this.wOcc+i;
	}
	public int getTotalwords() {
		return totalwords;
	}
	public void setTotalwords(int totalwords) {
		this.totalwords = totalwords;
	}
	public void incTotalwords(int i) {
		//this.relevance=this.relevance+1;
		this.totalwords=this.totalwords+i;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	public void incRelevance(double i) {
		//this.relevance=this.relevance+1;
		this.relevance=this.relevance+i;
	}

}
