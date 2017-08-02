package iFeel.methods;

/**
 * @author jpaulo
 * Lexicon developed by Saif Mohammad
 */
public class Sentiment140Lexicon extends MohammadSentimentLexiconMethod {

	/*
	 * score's range of each dictionary:
	 * unigrams: -4.999 to 5.0
	 * bigrams:  -5.606 to 7.352
	 * pairs:    -4.999 to 5.0
	 */

	public Sentiment140Lexicon(String unigramsDictionaryFilePath, 
			String bigramsDictionaryFilePath, String pairsDictionaryFilePath) {

		super(unigramsDictionaryFilePath, bigramsDictionaryFilePath, pairsDictionaryFilePath);

		//absolute score's value threshold to classify as NEUTRAL
		//Double thresholdNeutral = null; 
	}
}
