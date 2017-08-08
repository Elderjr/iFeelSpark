package iFeel.methods;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import iFeel.Method;
import util.Utils;

/**
 * @author jpaulo
 * Afinn-111
 */
public class Afinn extends Method implements Serializable{

	private static final String PUNCTUATION_REGEX = "[\\(\\)\\,\\!\\?\\.]";
	/**
	 * key: token; value: score
	 */
	private Map<String, Integer> lexiconDictionary;
	private String dictionaryFilePath;

	public Afinn(String dictionaryFilePath) {

		this.dictionaryFilePath = dictionaryFilePath;
		this.loadDictionaries();
	}
	
	/**
	 * Afinn-111 has one dictionary
	 */
	@Override
	public void loadDictionaries() {
		
		this.lexiconDictionary = new HashMap<String, Integer>();			

		try {
			File f = new File(this.dictionaryFilePath);			
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				
				String[] data = line.split("\t");
				
				this.lexiconDictionary.put(data[0], Integer.valueOf(data[1]));
				
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An error ocurred while opening the lexicon file.");
			System.exit(-1);
		}
	}

	@Override
	public int analyseText(String text) {

		Map<String, Integer> wordsInfo = this.parseText(text);
		int wordsValue = 0;
		int totalFrequency = 0;

		// find text's words present in lexicon
		for (String word : wordsInfo.keySet()) {
			
			if (this.lexiconDictionary.containsKey(word)) {
				
				wordsValue += (wordsInfo.get(word) * this.lexiconDictionary.get(word));
				totalFrequency += wordsInfo.get(word);
			}
		}

		double sentimentScore = (double)wordsValue / totalFrequency;		

		//System.out.println("sentiment score: " + sentimentScore);

		if (totalFrequency != 0) {
			if (sentimentScore > 0d) {
				return POSITIVE;
			}
			else if (sentimentScore < 0d) {
				return NEGATIVE;
			}		
		}

		return NEUTRAL;
	}

	/**
	 * @param text
	 * @return
	 */
	private Map<String, Integer> parseText(String text) {

		Map<String, Integer> wordsInfo = new HashMap<>();
				
		String[] tokenized = Utils.removePunctuation(PUNCTUATION_REGEX, text).toLowerCase().split(" ");
		
		for (String token : tokenized) {
			
			if (wordsInfo.containsKey(token)) {
				wordsInfo.put(token, wordsInfo.get(token) + 1);
			}
			else {
				wordsInfo.put(token, 1);
			}
		}
		
		return wordsInfo;
	}

	/**
	 * just to verify
	 */
	private void printDictionary() {

		int count = 0;
		for (String key : this.lexiconDictionary.keySet()) {
			System.out.println(++count + ") [" + key + "] = " + this.lexiconDictionary.get(key));
		}
	}

	@Override
	public String getName() {
		return "Afinn";
	}
}