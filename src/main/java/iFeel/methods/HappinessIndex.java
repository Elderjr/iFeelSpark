package iFeel.methods;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iFeel.Method;
import util.Utils;

public class HappinessIndex extends Method implements Serializable{

	private Map<String, List<Double>> anewSet; //dictionary
	private String dictionaryFilePath;
	
	public HappinessIndex(String dictionaryFilePath) {

		this.dictionaryFilePath = dictionaryFilePath;
		loadDictionaries();
	}

	private static Map<String, Integer> getWordFreq(String text) {

		List<String> words = Arrays.asList( Utils.removePunctuation(text).toLowerCase().split(" ") );
		Map<String, Integer> wordFreq = new HashMap<String,Integer>();

		for (String word : words) {
			if (wordFreq.containsKey(word)) {
				wordFreq.put(word, wordFreq.get(word) + 1);
			} else {
				wordFreq.put(word, 1);
			}
		}

		return wordFreq;
	}


	@Override
	public void loadDictionaries() {

		this.anewSet = new HashMap<String, List<Double>>();
		try {
			File f = new File(this.dictionaryFilePath);			
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				//parse Line
				List<Double> values = new ArrayList<>();
				String[] lineItems = line.split(",");

				values.add(Double.valueOf(lineItems[1]));
				values.add(Double.valueOf(lineItems[2]));
				values.add(Double.valueOf(lineItems[3]));
				values.add(Double.valueOf(lineItems[4]));
				values.add(Double.valueOf(lineItems[5]));
				values.add(Double.valueOf(lineItems[6]));
				values.add(Double.valueOf(lineItems[7]));

				try {
					//some lines end with . besides a number
					values.add(Double.valueOf(lineItems[8]));
				} catch (Exception e) {
//					System.out.println(line);
					values.add(0d);
				}

				this.anewSet.put(lineItems[0], values);
				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int analyseText(String text) {

		Map<String, Integer> wordFreq = getWordFreq(text);
		//double valence = 0d;
		double numerator = 0d;
		double sumFreq = 0d;
		double anewVal = 0d;
		double sentiment = 0d;

		for (String word : wordFreq.keySet()) {
			
			if (anewSet.containsKey(word)) {
				sumFreq += wordFreq.get(word);
				anewVal = anewSet.get(word).get(1);
				numerator += anewVal * wordFreq.get(word);
			}
		}

		if (sumFreq == 0d) {
			return NEUTRAL; //neutral
		}

		sentiment = numerator/sumFreq;
		sentiment = (sentiment - 5) / 4d;

		//System.out.print(sentiment + " ");
		
		if (sentiment > 0d) {
			return POSITIVE;
		}
		else if (sentiment < 0d) {
			return NEGATIVE;
		}

		return NEUTRAL;
	}

	public String getName(){
		return "HappinessIndex";
	}
}
