package master.keyEx.models;

public class Category {
private String title;
private int wOcc;
private int totalwords;
private double relevance;
private String normtitle;
private String akronom;
private String associatedGCAT;
private int normAdd=0;
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
	public Category(String name,String normTitle) {
		this.setTitle(name);
		this.setNormtitle(normTitle);
		this.wOcc =0;
	}
	
	public Category(String name,String normTitle,String akro) {
		this.setTitle(name);
		this.setNormtitle(normTitle);
		this.wOcc =0;
		this.akronom = akro;
	}
	
//	public int getRelevance() {
//		return wOcc;
//	}
	public double getRelevance() {
		return this.relevance;
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
	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}
	
	public void incRelevance(double i) {
		//this.relevance=this.relevance+1;
		this.relevance=this.relevance+i;
	}
	public String getNormtitle() {
		return normtitle;
	}
	public void setNormtitle(String normtitle) {
		this.normtitle = normtitle;
	}
	public String getAkronom() {
		return akronom;
	}
	public void setAkronom(String akronom) {
		this.akronom = akronom;
	}
	public String getAssociatedGCAT() {
		return associatedGCAT;
	}
	public void setAssociatedGCAT(String associatedGCAT) {
		this.associatedGCAT = associatedGCAT;
	}
	public int getNormAdd() {
		return normAdd;
	}

	public void incNormAdd(){
		this.normAdd = this.normAdd+1;
	}
	public void setNormAdd(int normAdd) {
		this.normAdd = normAdd;
	}
	
	public void normalizeRelevance(){
		this.relevance = this.relevance/this.normAdd;
	}

}
