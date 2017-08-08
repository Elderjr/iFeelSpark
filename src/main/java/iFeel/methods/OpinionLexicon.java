package iFeel.methods;

import java.util.Set;

import iFeel.Method;
import util.Utils;

/**
 * @author miller
 */
public class OpinionLexicon extends Method {

	/**
	 * all chars that can't be removed
	 */
	private static String PUNCTUATION_REGEX = "[^a-zA-Z0-9\\*\\+\\-]";

	private Set<String> positiveWords; //lexicon dictionary
	private Set<String> negativeWords; //lexicon dictionary
	private String positiveWordsFile;
	private String negativeWordsFile;	

	public OpinionLexicon(String posWordsFile, String negWordsFile) {

		this.positiveWordsFile = posWordsFile;
		this.negativeWordsFile = negWordsFile;
		loadDictionaries();
	}

	private Set<String> loadDictionary(String fileName) {
		
		return Utils.readFileLinesToSet(fileName);
	}

	@Override
	public void loadDictionaries() {

		this.positiveWords = this.loadDictionary(this.positiveWordsFile); //Positive words file
		this.negativeWords = this.loadDictionary(this.negativeWordsFile); //Negative words file
	}

	@Override
	public int analyseText(String text) { //throws IOException

		int positive = 0;
		int negative = 0;
//		Set<String> stopWords = NLPUtil.loadDefaultStopWords();
		String[] words = text.split(" ");
		int lengthWords = words.length;

		for(int i = 0; i < lengthWords; i++){
			String aux = words[i].toLowerCase();
			aux = Utils.removePunctuation(PUNCTUATION_REGEX, aux);

//			if(stopWords.contains(aux) == false){
				if (positiveWords.contains(aux) == true){
					positive++;
				}
				else if (negativeWords.contains(aux) == true){
					negative++;
				}
//			}
		}

		if (positive > negative){
			return POSITIVE;
		}
		else if (negative > positive){
			return NEGATIVE;
		}
		else {
			return NEUTRAL;
		}
	}
	
	@Override
	public String getName(){
		return "OpinionLexicon";
	}
}

