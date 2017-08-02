package methods;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import adapters.MaxentTaggerSerializable;
//Libraries of NLPStanford:
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/* Description about the tags:	
 * https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
 * 
 * Adverbs:
 * RB = Adverb				RBR = Adverb, comparative		RBS = Adverb, superlative
 * 
 * Nouns:
 * NN = Noun, singular or mass 		NNS = Noun, plural			
 * NNP = Proper noun, singular		NNPS = Proper noun, plural
 * 
 * Adjectives:
 * JJ = Adjective			JJR = Adjective, comparative		JJS = Adjective, superlative
 * 
 * Verbs:
 * VB = Base form		VBD = Past tense			VBG = Gerund or present participle 
 * VBN = Past participle 							VBP = Non-3rd person singular present 
 * VBZ = 3rd person singular present
 */

/*Important observations:
 * Can't use string.toLowerCase() before this.partOfSpeech(string), if do, some 
 * words does not make sense for tagger, an example is I.
 */

/*
 * SetiWordNet simplified algorithm:
 * 	score = 0.0
 * 	sum = 0.0
 * 	for each synTermRank do:
 * 		score += 1 / synTermRank * (positive - negative)
 * 		sum += 1 / synTermRank
 * 	score /= sum
 * The algorithm is at loadDictionaries().
 */

/**
 * @author elias
 */
public class SentiWordNet extends Method implements Serializable{
	private String modelFilePath;
	private String dictionaryFilePath;
	private Map<String, Double> sentiWordNetDictionary;
	private MaxentTaggerSerializable tagger;
	
	public SentiWordNet(String dictionaryFilePath, String modelFilePath) {
		this.dictionaryFilePath = dictionaryFilePath;
		this.modelFilePath = modelFilePath;
		this.loadDictionaries();
		this.tagger = new MaxentTaggerSerializable(this.modelFilePath);
	}
	
	private String partOfSpeech(String text) {
		byte[] bytes = text.getBytes( Charset.forName("UTF-8" ));
		String sentence =  new String( bytes, Charset.forName("UTF-8") );
		String tag = this.tagger.tagString(sentence);
		tag = tag.replace("_", "#");
		return tag;
	}
	
	/**
	 * Transform the POS out of the Stanford Library in
	 * original SentiWordNet standard and adds #POS in each
	 * word.
	 */
	private String formatString(final String text) {
		String tag = this.partOfSpeech(text);
		String[] words = tag.split(" ");
		String mSentence = "";
		
		for(String word : words) {
			String[] s = word.split("#");
			// When the "s" is many "#", the s[1] don't exist and there is an exception.
			if(s.length > 1) {
				switch(s[1]) {
					case "RB":	//Checking adverbs:
					case "RBR":
					case "RBS":
						mSentence += s[0] + "#r ";
						break;
					case "JJ":	//Checking adjectives
					case "JJR":
					case "JJS":
						mSentence += s[0] + "#a ";
						break;
					case "VB":	//Checking verbs
					case "VBD":
					case "VBG":
					case "VBN":
					case "VBP":
					case "VBZ":
						mSentence += s[0] + "#v ";
						break;
					case "NNP":	//Checking nouns
					case "NN":
					case "NNPS":
					case "NNS":
						mSentence += s[0] + "#n ";
						break;
					default:
						//The tag not is important. Do nothing.
				}
			}
		}
		mSentence = mSentence.toLowerCase();
		return mSentence;
	}
	
	@Override
	public void loadDictionaries() {
		try{
			File f = new File(this.dictionaryFilePath);
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			Map<String, HashMap<Integer, Double>> tempDict = new HashMap<String, HashMap<Integer, Double>>();
			this.sentiWordNetDictionary = new HashMap<String, Double>();
			String line;
			String wordTypeMarker = "";
			while((line = input.readLine()) != null) {
				if(!line.trim().startsWith("#")) {
					String[] data = line.split("\t");
					//data[0] = POS			data[1] = ID			data[2]= PosScore
					//data[3] = NegScore	data[4] = SynsetTerms	data[5] = Gloss
					wordTypeMarker = data[0];
					Double synsetScore = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);
					
					// Get all Synset terms
				  	String[] synTermsSplit = data[4].split(" ");
				  	for(String synTermSplit : synTermsSplit) {
				  		// Get synterm and synterm rank
				    	String[] synTermAndRank = synTermSplit.split("#");
				    	String synTerm = synTermAndRank[0] + "#" + wordTypeMarker;
				    	
				    	int synTermRank = Integer.parseInt(synTermAndRank[1]);
				    	
				    	if (!tempDict.containsKey(synTerm)) {
				    		tempDict.put(synTerm, new HashMap<Integer, Double>());
				    	}
				    	tempDict.get(synTerm).put(synTermRank,  synsetScore);
				  	}
					
				}
			}
			for (Map.Entry<String, HashMap<Integer, Double>> entry 
					: tempDict.entrySet()) {
				String word = entry.getKey();
				Map<Integer, Double> synSetScoreMap = entry.getValue();
				double score = 0.0;
				double sum = 0.0;
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap
			    	   .entrySet()) {
			  		score += setScore.getValue() / (double) setScore.getKey();
			  		sum += 1.0 / (double) setScore.getKey();
				}
				score /= sum;

				this.sentiWordNetDictionary.put(word, score);
			}
			input.close();
		} catch(IOException e) {
			System.err.println("Error opening file: " + this.dictionaryFilePath + "!");
			System.exit(1989);
		}
	}

	@Override
	public int analyseText(final String text) {
		String sentence = this.formatString(text);
		Double finalScore = 0.;
		String[] words = sentence.split(" ");
		for(String word : words) {
			if(sentiWordNetDictionary.containsKey(word)) {
				Double aux1 = sentiWordNetDictionary.get(word);
				finalScore += aux1;
			}
		}
		
		//System.out.println("Score: " + finalScore);
		
		if(finalScore < 0d) {
			return NEGATIVE;
		} else if(finalScore > 0d) {
			return POSITIVE;
		} else{
			return NEUTRAL;
		}
	}

	private void printDictionary() {
		for(Map.Entry<String, Double> pair : sentiWordNetDictionary.entrySet()) {
			System.out.print("Key " + pair.getKey() + " : " + pair.getValue().toString() + "\n");
		}
	}
}
