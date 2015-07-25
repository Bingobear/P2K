package master.keyEx.models;

import Database.model.Author;

import java.util.ArrayList;

/**
 * Publication model class
 * 
 * @author Simon Bruns
 *
 */

public class Publication {
private int pubID;
private String title;
private ArrayList<Author> authors = new ArrayList<Author>();
	public Publication(int id, String title) {
		this.pubID = id;
		this.title=title;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getPubID() {
		return pubID;
	}
	public void setPubID(int pubID) {
		this.pubID = pubID;
	}
	public ArrayList<Author> getAuthors() {
		return authors;
	}
	public void setAuthors(ArrayList<Author> authors) {
		this.authors = authors;
	}
	
	public void addAuthors(Author author) {
		this.authors.add(author);
	}

}
