package Database.model;

import java.util.ArrayList;

public class Location {
private ArrayList<String> physicalLoc;
private String url;
private String note;
public Location() {

}

public ArrayList<String> getPhysicalLoc() {
	return physicalLoc;
}
public void setPhysicalLoc(ArrayList<String> physicalLoc) {
	this.physicalLoc = physicalLoc;
}

public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
public String getNote() {
	return note;
}
public void setNote(String note) {
	this.note = note;
}
}
