package Database.model;

public class Author {
private String name;
private String adress;
private int pos;
private int authorID;


public Author(){
	
}

public Author(String name2, int id){
	this.name = name2;
	this.authorID = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getAdress() {
	return adress;
}
public void setAdress(String adress) {
	this.adress = adress;
}
public int getPos() {
	return pos;
}
public void setPos(int pos) {
	this.pos = pos;
}
public int getAuthorID() {
	return authorID;
}
public void setAuthorID(int authorID) {
	this.authorID = authorID;
}
}
