package test.tester;

import org.tartarus.snowball.ext.englishStemmer;

public class Stemmer {

	public String[] stem(String[] tokens) {
		englishStemmer stemmer = new englishStemmer();
		String [] stemtokens= new String[tokens.length];
		for(int ii = 0;ii<tokens.length;ii++){
			stemmer.setCurrent(tokens[ii]);
			stemmer.stem();
			stemtokens[ii]=stemmer.getCurrent();
		}
		return stemtokens;
	}

}
