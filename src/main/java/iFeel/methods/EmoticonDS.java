package iFeel.methods;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iFeel.Method;
import util.NLPUtil;

/**
 * @author elias
 */
public class EmoticonDS extends Method {

	private String dictionaryFile;
	private Map<String, Integer> dictionary;

	public EmoticonDS(final String dicFileName) {
		this.dictionaryFile = dicFileName;
		this.loadDictionaries();
	}

	public int analyseText(final String text) {
		int posTotal = 0;
		int negTotal = 0;
		int neuTotal = 0;

		String lowerc = text.toLowerCase();
		List<String> words = new ArrayList<String>(Arrays.asList(lowerc.split(" ")));
		
		//Some words are not important:
		NLPUtil.removeStopWords(words);

		for(int i = 0; i < words.size(); i++) {
			if (this.dictionary.containsKey(words.get(i))) {
				int val = this.dictionary.get(words.get(i));
				if (val == POSITIVE) {
					posTotal++;
				} else if (val == NEGATIVE) {
					negTotal++;
				} else if (val == NEUTRAL) {
					neuTotal++;
				}
			}
		}

		if ((posTotal > negTotal) && (posTotal > neuTotal)) {
			return POSITIVE;
		} else if ((negTotal > posTotal) && (negTotal > neuTotal)) {
			return NEGATIVE;
		}
		return NEUTRAL;
	}
	
	//Saving the file's information in map:
	private Map<String, Integer> readFileLinesToMap(final String fileName) {
		
		Map<String , Integer> map = new HashMap<>();
		try {
			File f = new File(this.dictionaryFile);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			this.dictionary = new HashMap<String, Integer >();
			Integer qntPos = 0, qntNeg = 0;
			while((line = input.readLine()) != null) {
				String []s = line.split("\t");
				//s[0] -> positive, s[1] -> negative, s[2] -> token
				qntPos = Integer.parseInt(s[0]);
				qntNeg = Integer.parseInt(s[1]);

				String aux1 = s[2].trim();
				if(qntPos > qntNeg) {
					map.put(aux1, Integer.valueOf(POSITIVE));
				} else if(qntNeg > qntPos) {
					map.put(aux1, Integer.valueOf(NEGATIVE));
				} else {
					map.put(aux1, Integer.valueOf(NEUTRAL));
				}
			}
			input.close();
		} catch(IOException e) {
			System.err.println("Error opening file: " + fileName);
			System.exit(1989);
		}
		
		return map;
	}

	@Override
	public void loadDictionaries() {
		this.dictionary = readFileLinesToMap(this.dictionaryFile);
	}
	
	private void printDictionary() {
		for(Map.Entry<String, Integer> pair : this.dictionary.entrySet()) {
			System.out.println("Key: " + pair.getKey() + "Value: " + pair.getValue());
		}
	}
	
	@Override
	public String getName(){
		return "EmoticonDS";
	}
}