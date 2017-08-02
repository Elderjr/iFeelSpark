package methods;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import util.Utils;

/**
 * @author elias
 */
public class Emoticons extends Method implements Serializable{

	private String posWordsFile;
	private String negWordsFile;
	private String neuWordsFile;
	private Set<String> posWords;
	private Set<String> negWords;
	private Set<String> neuWords;

	public Emoticons(String posWordsFileName, String negWordsFileName, String neuWordsFileName) {
		this.posWordsFile = posWordsFileName;
		this.negWordsFile = negWordsFileName;
		this.neuWordsFile = neuWordsFileName;
		this.loadDictionaries();
	}

	// Get the dictionaries:
	public void loadDictionaries() {
		this.posWords = Utils.readFileLinesToSet(this.posWordsFile);
		this.negWords = Utils.readFileLinesToSet(this.negWordsFile);
		this.neuWords = Utils.readFileLinesToSet(this.neuWordsFile);
	}

	// Receives a phrase and analysis it:
	public int analyseText(final String text) {
		int posTotal = 0;
		int negTotal = 0;
		int neuTotal = 0;

		// Break the phrase in words:
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(text.split(" ")));

		for (String word : words) {
			if (this.posWords.contains(word)) {
				posTotal += 1;
			} else if (this.negWords.contains(word)) {
				negTotal += 1;
			} else if (this.neuWords.contains(word)) {
				neuTotal += 1;
			}
		}

		if ((posTotal > negTotal) && (posTotal > neuTotal)) {
			return POSITIVE;
		} else if((negTotal > posTotal) && (negTotal > neuTotal)) {
			return NEGATIVE;
		}
		return NEUTRAL;
	}
	
	private void  printDictionary() {
		System.out.println("The positive dictionary:");
		for(Iterator<String> it = this.posWords.iterator(); it.hasNext(); ) {
			String s = it.next();
			System.out.println(s);
		}
		
		System.out.println("The neutral dictionary:");
		for(Iterator<String> it = this.neuWords.iterator(); it.hasNext(); ) {
			String s = it.next();
			System.out.println(s);
		}
		
		System.out.println("The negative dictionary:");
		for(Iterator<String> it = this.negWords.iterator(); it.hasNext(); ) {
			String s = it.next();
			System.out.println(s);
		}
	}
}
