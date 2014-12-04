package Database.model;

import java.util.ArrayList;

public class Publication {

	private String title;
	private String shortTitle;
	private String format;
	private String date;
	private String publisher;
	private String idBTH;
	private String language;
	private String origin;
	private Location loc;
	private Journal journal;
	private ArrayList<Author> authors;
	private RecordInfo record;
	private int pubID;
	
	public Publication(){
		authors = new ArrayList<Author>();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		if(title.length()<90){
			setShortTitle(title);
		}else{
		setShortTitle(title.substring(0, 90));
		}
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIdBTH() {
		return idBTH;
	}

	public void setIdBTH(String idBTH) {
		this.idBTH = idBTH;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	public ArrayList<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(ArrayList<Author> authors) {
		this.authors = authors;
	}

	public RecordInfo getRecord() {
		return record;
	}

	public void setRecord(RecordInfo record) {
		this.record = record;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public int getPubID() {
		return pubID;
	}

	public void setPubID(int pubID) {
		this.pubID = pubID;
	}


}
