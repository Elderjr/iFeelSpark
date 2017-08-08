package iFeel.methods;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iFeel.Method;
import util.Utils;
import util.Utils;

/**
 * @author jpaulo
 */
public class Emolex extends Method {

	/**
	 * 
	 */
	private Map<String, Map<String, Integer>> dictionary;
	private String dictionaryFilePath;

	public Emolex(String dictionaryFilePath) {

		this.dictionaryFilePath = dictionaryFilePath;
		this.loadDictionaries();
	}
	
	/**
	 * 
	 */
	@Override
	public void loadDictionaries() {
		
		final int TOKEN_IDX = 0;
		final int SENTIMENT_IDX = 1;
		final int VALUE_IDX = 2;
		this.dictionary = new HashMap<>();			

		try {
			File f = new File(this.dictionaryFilePath);			
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				
				String[] data = line.split("\t");
				
				if (data[VALUE_IDX].equals("1")) {

					if (!this.dictionary.containsKey(data[TOKEN_IDX])) {

						Map<String, Integer> wordValueBySentiment = new HashMap<>();
						wordValueBySentiment.put(data[SENTIMENT_IDX], 1);
						this.dictionary.put(data[TOKEN_IDX], wordValueBySentiment);
					}
					else {

						this.dictionary.get(data[TOKEN_IDX]).put(data[SENTIMENT_IDX], 1);
					}
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int analyseText(String text) {

		String[] tokens = Utils.removePunctuation(text).toLowerCase().split(" ");

		//for each sentiment
		Map<String, Integer> sumSentimentValues = new HashMap<>();

		for (String token : tokens) {

			if (this.dictionary.containsKey(token)) {
								
				for (String sentiment : this.dictionary.get(token).keySet()) {

					int value = this.dictionary.get(token).get(sentiment);
					
					if (sumSentimentValues.containsKey(sentiment)) {
						int curr = sumSentimentValues.get(sentiment);
						sumSentimentValues.put(sentiment, value + curr);
					}
					else {
						sumSentimentValues.put(sentiment, value);
					}
				}
			}
		}
		
		int posTotal = (sumSentimentValues.get("positive") != null) ? sumSentimentValues.get("positive") : 0;
		int negTotal = (sumSentimentValues.get("negative") != null) ? sumSentimentValues.get("negative") : 0;

		/*
		 * sentiments could be considered as positive: joy, trust
		 * sentiments could be considered as negative: anger, disgust, fear, sadness
		 * sentiments could be considered as neutral:  surprise, anticipation
		 */

		//System.out.println("pos: " + posTotal + "; neg: " + negTotal);
		if (posTotal > negTotal) {
			return POSITIVE;
		}
		else if (negTotal > posTotal) {
			return NEGATIVE;
		}		

		return NEUTRAL;
	}

	/**
	 * just to verify
	 */
	private void printDictionary() {

		List<String> keys = new ArrayList<String>();
		keys.addAll(this.dictionary.keySet());
		Collections.sort(keys);
		int count = 0;
		for (String k : keys) {
			for (String j : this.dictionary.get(k).keySet()) {
				System.out.println(++count + ") [" + k + "][" + j + "] = " + this.dictionary.get(k).get(j));
			}
		}
	}
	
	@Override
	public String getName(){
		return "Emolex";
	}
}
