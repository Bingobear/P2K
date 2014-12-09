package master.keyEx.models;

public class TFIDF {

	public TFIDF() {
		// TODO Auto-generated constructor stub
	}
	public static double calcTF(double tPDFocc,double totalterms){
		return tPDFocc/totalterms;
	}
	
	public static double calcIDF(double docN,double docNt){
		return Math.log10((double)docN/(double)docNt);
	}
	
	public static double calcTFIDF(double tf,double idf){
		return tf*idf;
	}

}
