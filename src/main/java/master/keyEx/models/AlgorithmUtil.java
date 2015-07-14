package master.keyEx.models;

/**A collection of useful Algorithms
 * @author Simon
 *
 */
public class AlgorithmUtil {

	public AlgorithmUtil() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param tPDFocc - occurrence of term t in pdf
	 * @param totalterms
	 * @return
	 */
	public static double calcTF(double tPDFocc,double totalterms){
		return tPDFocc/totalterms;
	}
	
	/**
	 * @param docN
	 * @param docNt number of doc having the term t
	 * @return
	 */
	public static double calcIDF(double docN,double docNt){

		return Math.log10((double)docN/(double)docNt);
	}
	
	public static double calcTFIDF(double tf,double idf){
		return tf*idf;
	}
	
	//SOURCE WIKIPEDIA
	/**Calc. Levenshteindistance in int
	 * @param wordA
	 * @param wordB
	 * @return
	 */
	public static int LevenshteinDistance (String wordA, String wordB) {                          
	    int len0 = wordA.length() + 1;                                                     
	    int len1 = wordB.length() + 1;                                                     
	 
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	 
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	 
	    // dynamicaly computing the array of distances                                  
	 
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	 
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (wordA.charAt(i - 1) == wordB.charAt(j - 1)) ? 0 : 1;             
	 
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	 
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	 
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	 
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}
	
	/**Calc. word similarity of word A and word B
	 * @param word (longer word of the two)
	 * @param diff (levenstein distance)
	 * @return similarity
	 */
	public static double calculateWordSim(String word,double diff){
		return diff/word.length();
	}

}
