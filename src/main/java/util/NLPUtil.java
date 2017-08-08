package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jpaulo, elias, miller, matheus, johnattan
 */
public class NLPUtil {

	/**
	 * Stop-words obtained from Python's NLTK library. 
	 */
	private static final Set<String> stopWordsNLTK = new HashSet<String>(Arrays.asList("i",
			"me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your",
			"yours", "yourself", "yourselves", "he", "him", "his", "himself",
			"she", "her", "hers", "herself", "it", "its", "itself", "they",
			"them", "their", "theirs", "themselves", "what", "which", "who",
			"whom", "this", "that", "these", "those", "am", "is", "are", "was",
			"were", "be", "been", "being", "have", "has", "had", "having",
			"do", "does", "did", "doing", "a", "an", "the", "and", "but", "if",
			"or", "because", "as", "until", "while", "of", "at", "by", "for",
			"with", "about", "against", "between", "into", "through", "during",
			"before", "after", "above", "below", "to", "from", "up", "down", "in",
			"out", "on", "off", "over", "under", "again", "further", "then", "once",
			"here", "there", "when", "where", "why", "how", "all", "any", "both",
			"each", "few", "more", "most", "other", "some", "such", "no", "nor",
			"not", "only", "own", "same", "so", "than", "too", "very", "s", "t",
			"can", "will", "just", "don", "should", "now"));

	/**
	 * Stop-words from SenticNet method.
	 */
	public static final Set<String> stopWordsSenticNet = new HashSet<String>(Arrays.asList("because",
			"ve", "then", "doing", "when", "is", "am", "an", "himself", "are", 
			"yourselves", "our", "its", "if", "ourselves", "these", "what", "i", 
			"her", "whom", "would", "there", "had", "s", "been", "should", "re", 
			"does", "those", "which", "ours", "themselves", "has", "was", "t", 
			"be", "we", "his", "yo", "that", "any", "than", "who", "here", "were", 
			"but", "they", "hers", "during", "herself", "him", "nor", "he", "me", 
			"myself", "don", "d", "did", "theirs", "having", "such", "yours", "their", 
			"this", "while", "so", "she", "each", "my", "or"));

	/**
	 * List of text like "token1 token2"
	 */
	public static List<String> bigrams(String[] unigrams) {
		
		//Arrays.asList makes array and list "share" same elements (or elements area)
		return bigrams(Arrays.asList(unigrams));		
	}

	/**
	 * A bigram has the text format "unigram1 unigram2"
	 * @param unigrams to generate list of bigrams.
	 */
	public static List<String> bigrams(List<String> unigrams) {
		
		List<String> bigrams = new ArrayList<>(unigrams.size()); 

		for (int i=0; i < unigrams.size() - 1; ++i) {

			bigrams.add(unigrams.get(i) + " " + unigrams.get(i + 1));
		}

		return bigrams;
	}

	/**
	 * remove all stop-words from words
	 * @param words
	 */
	public static void removeStopWords(List<String> words) {

		int tam = words.size();
		for (int i = 0; i < tam; i++) {
			String s = words.get(i);
			if (stopWordsNLTK.contains(s)) {
				words.remove(s);
				tam = words.size();
				break;
			}
		}
	}

	/**
	 * @return default stop-words, from NLTK
	 */
	public static Set<String> loadDefaultStopWords() {
		return NLPUtil.stopWordsNLTK;
	}
}