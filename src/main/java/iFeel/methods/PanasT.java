package iFeel.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import iFeel.Method;
import util.NLPUtil;
import util.Utils;

/**
 * @author miller, jpaulo
 */
public class PanasT extends Method {

	private Set<String> positiveWords;
	private Set<String> negativeWords;
	private Set<String> neutralWords;

	/**
	 * helps dealing with bigrams existent in dictionary(ies), for example, at ease
	 * key = bigram; value = bigram transformed into a single token
	 */
	private Map<String, String> bigramsAux;
	
	private String positiveWordsFile;
	private String negativeWordsFile;	
	private String neutralWordsFile;

	public PanasT(String posWordsFile, String negWordsFile, String neuWordsFile) {

		this.positiveWordsFile = posWordsFile;
		this.negativeWordsFile = negWordsFile;
		this.neutralWordsFile = neuWordsFile;
		this.loadDictionaries();
	}

	/**
	 * From dictionary file, generates a set with its ngrams 
	 */
	private Set<String> loadDictionary(String fileName) {
		
		Set<String> set = new HashSet<>();
		
		try {
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String aux = br.readLine();

			while (aux != null) {
				aux = aux.trim();
				if (aux.contains(" ")) { //bigram found
					String bigram = aux;
					aux = aux.replace(" ", ""); 
					bigramsAux.put(bigram, aux);
				}

				set.add(aux);
				aux = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return set;
	}

	@Override
	public void loadDictionaries() {

		this.bigramsAux = new HashMap<>();
		this.positiveWords = this.loadDictionary(this.positiveWordsFile); //Positive words file
		this.negativeWords = this.loadDictionary(this.negativeWordsFile); //Negative words file
		this.neutralWords = this.loadDictionary(this.neutralWordsFile); //Neutral words file
	}

	@Override
	public int analyseText(String text) {

		int positive = 0;
		int negative = 0;
		int neutral = 0;
		Set<String> stopWords = NLPUtil.loadDefaultStopWords();

		String aux =  Utils.removePunctuation("[^a-zA-Z ]", text).trim().toLowerCase(); //keep only letters/spaces and lc transform
		
		/*
		 * that approach 
		 */
		for (String bigram : this.bigramsAux.keySet()) {			
			if (aux.contains(bigram)) {
				aux = aux.replace(bigram, bigramsAux.get(bigram)); //remove space of bigram
			}
		}

		String[] words = aux.split(" +");
		int lengthWords = words.length;

		for (int i=0; i < lengthWords; i++){

			if (stopWords.contains(aux) == false) {
				if (positiveWords.contains(words[i]) == true) {
					positive++;
				}
				else if (negativeWords.contains(words[i]) == true) {
					negative++;
				}
				else if (neutralWords.contains(words[i])) {
					neutral++;
				}
			}
		}

		if ((positive > negative) && (positive > neutral)) {
			return POSITIVE;
		}
		else if ((negative > positive) && (negative > neutral)) {
			return NEGATIVE;
		}
		return NEUTRAL;
	}
	
	@Override
	public String getName(){
		return "PanasT";
	}
}