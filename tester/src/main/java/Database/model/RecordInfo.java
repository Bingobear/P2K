package Database.model;

public class RecordInfo {
private String idRecordBTH;
private String createRDate;
private String changeRDate;
private String origin;
public RecordInfo(){
	
}
public String getIdRecordBTH() {
	return idRecordBTH;
}
public void setIdRecordBTH(String idRecordBTH) {
	this.idRecordBTH = idRecordBTH;
}
public String getCreateRDate() {
	return createRDate;
}
public void setCreateRDate(String createRDate) {
	this.createRDate = createRDate;
}
public String getChangeRDate() {
	return changeRDate;
}
public void setChangeRDate(String changeRDate) {
	this.changeRDate = changeRDate;
}
public String getOrigin() {
	return origin;
}
public void setOrigin(String origin) {
	this.origin = origin;
}
}
