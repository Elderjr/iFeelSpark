package iFeel.methods;

/**
 * @author jpaulo
 * By Saif Mohammad
 */
public class NRCHashtagSentimentLexicon extends MohammadSentimentLexiconMethod {

	/*
	 * score's range of each dictionary:
	 * unigrams: from -6.925 to 7.526
	 * bigrams:  from -8.639 to 8.888 
	 * pairs:    from -4.999 to 5.0
	 */
	
	public NRCHashtagSentimentLexicon(String unigramsDictionaryFilePath, 
			String bigramsDictionaryFilePath, String pairsDictionaryFilePath) {

		super(unigramsDictionaryFilePath, bigramsDictionaryFilePath, pairsDictionaryFilePath);
	}
	
	@Override
	public String getName(){
		return "NRCHashTag";
	}
}
