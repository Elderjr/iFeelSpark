package iFeel.methods;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import iFeel.Method;


/**
 * 
 * @author Lucas
 * @author Manoel
 * 
 * This is the class for SO-Cal Sentiment analysis method
 * 
 */
public class SoCal extends Method {
	// Method dictionaries
	private Map<String, Double> adjDictionary;
	private Map<String, Double> advDictionary;
	private Map<String, Double> nounDictionary;
	private Map<String, Double> verbDictionary;
	private Map<String, Double> intDictionary;
	private Map<String, Map<String[][], Double>> cAdjDictionary;
	private Map<String, Map<String[][], Double>> cAdvDictionary;
	private Map<String, Map<String[][], Double>> cNounDictionary;
	private Map<String, Map<String[][], Double>> cVerbDictionary;
	private Map<String, Map<String[][], Double>> cIntDictionary;
	// Name of Dictionary files listed above
	private String adjDictionaryFile;
	private String advDictionaryFile;
	private String nounDictionaryFile;
	private String verbDictionaryFile; 
	private String intDictionaryFile;
	private String extraDictionaryFile;
	private MaxentTagger tagger;
	
	// Configuration Parameters 
	private Map <String, Boolean> configFlags;
	private Map <String, Double> configModifiers;
	//Word Lists Modifiers
	private List <String> search; // Search can be used to optionally output sentences with certain words
	private List <String> irrealis; // Irrealis blockers
	private List <String> boundaryWords; // These words stop a backward search for negators, highlighters, modifier blockers, and irrealis blockers
	private Map <String, Double> highlighters; // These words increase or decrease the SO weight of words later in a sentence
	private Map <String, Double> weightTags; // Words contained within XML tags with these names will be weighed
	private Map <String, Double> weightsByLocation; // Words within fraction range will be weighed, default is 1
	
	// Internal word lists
	private List <String> notWantedAdj;
	private List <String> notWantedAdv;
	private List <String> notWantedVerb;
	private List <String> negators;
	private List <String> punct;
	private List <String> sentPunct;
	private Map <String, List<String>> skipped;
	private List <String> comparatives;
	private List <String> superlatives;
	private List <String> definites;
	private Map <String, String> tags;
	private Map <String, String> macroReplace;
	private String nounTag;
	private String verbTag;
	private String adjTag;
	private String advTag;
	
	//Text
	private List <String[]> text; // The text is a list of word, tag lists
	private List <Double> weights; // Weights should be the same length as the text, one for each token
	private List <Map<String, Integer>> wordCounts; // keeps track of number of times each word lemma appears in the text
	private double textSO; // a sum of the SO value of all the words in the text
	private int SOCounter; // a count of the number of SO carrying terms
	private List <Integer> boundaries; // the location of newline boundaries from the input
		
	private void loadConfigurations() { // Should Be Called on constructor
		this.configFlags = new HashMap<>();
		this.configModifiers = new HashMap<>();
		this.loadConfigModifiers();
		this.loadConfigFlags();
		this.loadWordListsModifiers();
 		
	}
	
	private void loadWordListsModifiers() {
		String searchElements[] = {"expected", "expecting", "expect", "hoped", "hope", "hoping", "wanted", "want", "wanting", "thought", "supposed", "figured", "guessed", "imagined", "could", "should", "would", "might", "must", "ought", "may", "anything", "any", "used"};
		this.search = new ArrayList<String>(Arrays.asList(searchElements));
		
		String irrealisElements[] = {"expected", "expecting", "expect", "hoped", "hope", "hoping", "wanted", "want", "wanting", "doubt", "doubted", "doubting", "thought", "supposed", "figured", "guessed", "imagined", "could", "should", "would", "might", "must", "ought", "may", "anything", "any", "used"};
		this.irrealis = new ArrayList<String>(Arrays.asList(irrealisElements));
		
		String boundaryWordsElements[] = {"but", "and", "or", "since", "because", "while", "after", "before", "when", "though", "although", "if", "which", "despite", "so", "then", "thus", "where", "whereas", "until", "unless"};
		this.boundaryWords = new ArrayList<String>(Arrays.asList(boundaryWordsElements));
		
		this.highlighters = new HashMap<String, Double>();
		this.highlighters.put("but", 2.0);
		this.highlighters.put("although", 0.5);
		
		this.weightTags = new HashMap<String, Double>();
		this.weightTags.put("COMMENT", 1.0);
		this.weightTags.put("DESCRIBE", 0.0);
		this.weightTags.put("FORMAL", 0.0);
		this.weightTags.put("DESCRIBE+COMMENT", 0.1);
		this.weightTags.put("BACKGROUND", 0.0);
		this.weightTags.put("INTERPRETATION", 0.0);
		
		this.weightsByLocation = new HashMap<String, Double>();
		this.weightsByLocation.put("0-1/5", 0.3);
		this.weightsByLocation.put("4/5-1", 0.3);
		
		String notWantedAdjElements[] = {"other", "same", "such", "first", "next", "last", "few", "many", "less", "more", "least", "most"};
		this.notWantedAdj = new ArrayList<String>(Arrays.asList(notWantedAdjElements));
		String notWantedAdvElements[] = {"really", "especially", "apparently", "actually", "evidently", "suddenly", "completely","honestly", "basically", "probably", "seemingly", "nearly", "highly", "exactly", "equally", "literally", "definitely", "practically", "obviously", "immediately", "intentionally", "usually", "particularly", "shortly", "clearly", "mildly", "sincerely", "accidentally", "eventually", "finally", "personally", "importantly", "specifically", "likely", "absolutely", "necessarily", "strongly", "relatively", "comparatively", "entirely", "possibly", "generally", "expressly", "ultimately", "originally", "initially", "virtually", "technically", "frankly", "seriously", "fairly",  "approximately", "critically", "continually", "certainly",  "regularly", "essentially", "lately", "explicitly", "right", "subtly",  "lastly", "vocally", "technologically", "firstly", "tally", "ideally", "specially", "humanly", "socially", "sexually", "preferably", "immediately", "legally", "hopefully", "largely", "frequently", "factually", "typically"};
		this.notWantedAdv = new ArrayList<String>(Arrays.asList(notWantedAdvElements));
		String notWantedVerbElements[] = {};
		this.notWantedVerb = new ArrayList<String>(Arrays.asList(notWantedVerbElements));
		String negatorsElements[] = {"not", "no", "n't", "neither", "nor", "nothing", "never", "none", "lack", "lacked", "lacking", "lacks", "missing", "without", "absence", "devoid"};
		this.negators = new ArrayList<String>(Arrays.asList(negatorsElements));
		String punctElements[] = {".", ",", ";", "!", "?", ":", ")", "(", "\"", "'", "-"};
		this.punct = new ArrayList<String>(Arrays.asList(punctElements));
		String sentPunctElements[] = {".", ";", "!", "?", ":", "\n", "\r"};
		this.sentPunct = new ArrayList<String>(Arrays.asList(sentPunctElements));
		
		String skippedJJElements[] = {"even", "to", "being", "be", "been", "is", "was", "'ve", "have", "had", "do", "did", "done", "of", "as", "DT", "PSP$"};
		List<String> skippedJJ = new ArrayList<String>(Arrays.asList(skippedJJElements));
		String skippedRBElements[] = {"VB", "VBZ", "VBP", "VBG"};
		List<String> skippedRB = new ArrayList<String>(Arrays.asList(skippedRBElements));
		String skippedVBElements[] = {"TO", "being", "been", "be"};
		List<String> skippedVB = new ArrayList<String>(Arrays.asList(skippedVBElements));
		String skippedNNElements[] = {"DT", "JJ", "NN", "of", "have", "has", "come", "with", "include"};
		List<String> skippedNN = new ArrayList<String>(Arrays.asList(skippedNNElements));
		this.skipped = new HashMap<String, List<String>>();
		this.skipped.put("JJ", skippedJJ);
		this.skipped.put("RB", skippedRB);
		this.skipped.put("VB", skippedVB);
		this.skipped.put("NN", skippedNN);
		
		String comparativesElements[] = {"less", "more", "as"};
		this.comparatives = new ArrayList<String>(Arrays.asList(comparativesElements));
	    String superlativesElements[] = {"most", "least"};
	    this.superlatives = new ArrayList<String>(Arrays.asList(superlativesElements));
	    String definitesElements[] = {"the","this", "POS", "PRP$"};
	    this.definites = new ArrayList<String>(Arrays.asList(definitesElements));
	    
	    String macroReplaceKeys[] = { "#NP?#","#PER?#","#give#","#fall#","#get#","#come#","#go#", "#show#","#make#","#hang#","#break#", "#see#", "#be#", "#bring#", "#think#" , "#have#", "#blow#", "#build#", "#do#", "#can#", "#grow#", "#hang#", "#run#", "#stand#", "#string#", "#hold#" , "#take#" };
	    String macroReplaceValues[] = {"[PDT]?_[DET|PRP|PRP$|NN|NNP]?_[POS]?_[NN|NNP|JJ]?_[NN|NNP|NNS|NNPS]?",  "[me|us|her|him]?",  "give|gave|given",  "fall|fell|fallen",  "get|got|gotten",  "come|came",  "go|went|gone",  "show|shown",  "make|made",  "hang|hung",  "break|broke|broken",  "see|saw|seen",  "be|am|are|was|were|been",  "bring|brought",  "think|thought",  "has|have|had",  "blow|blew",  "build|built",  "do|did|done",  "can|could", "grow|grew|grown",  "hang|hung",  "run|ran",  "stand|stood",  "string|strung",  "hold|held",  "take|took|taken"};
	    this.macroReplace = new HashMap<>();
	    if(macroReplaceKeys.length == macroReplaceValues.length)
	    	for(int i =0; i<macroReplaceKeys.length; i++)
	    		this.macroReplace.put(macroReplaceKeys[i], macroReplaceValues[i]);
	    else {
	    	System.out.println("Error: Missing value or key for MacroReplace Options");
	    	System.exit(-1);
	    }
	    
	    this.nounTag = "NN";
	    this.verbTag = "VB";
	    this.adjTag = "JJ";
	    this.advTag = "RB";
	}
	
	private void loadConfigModifiers() {
		this.configModifiers.put("adj_multiplier", 1.0);   // multiply all adjectives by this amount
		this.configModifiers.put("adv_multiplier", 1.0);   // multiply all adverbs by this amount
		this.configModifiers.put("verb_multiplier", 1.0);  // multiply all verbs by this amount
		this.configModifiers.put("noun_multiplier", 1.0);  // multiply all nouns by this amount
		this.configModifiers.put("int_multiplier", 1.0);   // multiply all intensified word groups by this amount
		this.configModifiers.put("neg_multiplier", 1.5);   // multiply all negative word groups by this amount
		this.configModifiers.put("capital_modifier", 2.0); // multiply all fully capitalized words by this amount
		this.configModifiers.put("exclam_modifier", 2.0);  // multiply words appearing in exclamations by this amount
		this.configModifiers.put("verb_neg_shift", 4.0);   // shift negated verbs by this amount
		this.configModifiers.put("noun_neg_shift", 4.0);   // shift negated nouns by this amount
		this.configModifiers.put("adj_neg_shift", 4.0);    // shift negated adjectives by this amount
		this.configModifiers.put("adv_neg_shift", 4.0);    // shift negated adverbs by this amount
		this.configModifiers.put("blocker_cutoff", 3.0);   // lowest (absolute) SO value that will block opposite polarity
	}
	
	private void loadConfigFlags() {
		this.configFlags.put("use_adjectives", true);
		this.configFlags.put("use_nouns", true);
		this.configFlags.put("use_verbs", true);
		this.configFlags.put("use_adverbs", true);
		this.configFlags.put("use_intensifiers", true);
		this.configFlags.put("use_negation", true);
		this.configFlags.put("use_comparatives", true);
		this.configFlags.put("use_superlatives", true);
		this.configFlags.put("use_multiword_dictionaries", true);
		this.configFlags.put("use_extra_dict", false);
		this.configFlags.put("use_XML_weighing", true);
		this.configFlags.put("use_weight_by_location", false);
		this.configFlags.put("use_irrealis", true); // irrealis markers (e.g. modals) nullify the SO value of words
		this.configFlags.put("use_imperative", false); // nullify words that appear in imperatives
		this.configFlags.put("use_subjunctive", false); // nullify words that appear in subjunctives, only relevant to Spanish
		this.configFlags.put("use_conditional", false); // nullify words that appear in conditionals, only relevant to Spanish
		this.configFlags.put("use_highlighters", true); // highlighers amplify (or deamplify) the SO value of words
		this.configFlags.put("use_cap_int", true); // intensify words in all caps
		this.configFlags.put("fix_cap_tags", true); // try to fix mistagged capitalized words
		this.configFlags.put("use_exclam_int", true); // intensify words in sentences with exclamation marks
		this.configFlags.put("use_quest_mod", true); // don't use words that appear in questions
		this.configFlags.put("use_quote_mod", true); // don't use words that appear in quotes
		this.configFlags.put("use_definite_assertion", true); // Presence of definites indicates assertion, ignore irrealis 
		this.configFlags.put("use_clause_final_int", true); // look for verb intensifiers at the edge of VPs
		this.configFlags.put("use_heavy_negation", true); // multiply all negative words by a fixed amount
		this.configFlags.put("use_word_counts_lower", true); // lower SO_value of words that appear often in text
		this.configFlags.put("use_word_counts_block", false); // do not assign SO value to repeated words
		this.configFlags.put("use_blocking", true); // a strong modifier will block items of opposite polarity
		this.configFlags.put("adv_learning", true); // add to the adverb dictionary by using the adjective dictionary
		this.configFlags.put("limit_shift", false); //limit negation shifting, no shifting beyond switched value
		this.configFlags.put("neg_negation_nullification", true); // the negation of negative terms simply nullifies them.
		this.configFlags.put("polarity_switch_neg", false); // switch polarity on negated items instead of shift
		this.configFlags.put("simple_SO", false); // Treat the SO of words as binary rather than as a continuous value
													// Also simplifies intensification to +1 -1
		this.configFlags.put("use_boundary_words", true); // disable only if preprocessor already segments into clauses
		this.configFlags.put("use_boundary_punctuation", true); // disable only if preprocessor already segments into clauses
		this.configFlags.put("restricted_neg_JJ", true);
		this.configFlags.put("restricted_neg_RB", true);
		this.configFlags.put("restricted_neg_NN", true);
		this.configFlags.put("restricted_neg_VB", true);		
	}
	/**
	 * 
	 * Default constructor for SO-Cal class.
	 * 
	 * @param adjDictionaryFile   -- path to adjectives dictionary file
	 * @param advDictionaryFile   -- path to adverbs dictionary file
	 * @param intDictionaryFile   -- path to intensifiers dictionary file
	 * @param nounDictionaryFile  -- path to nouns dictionary file
	 * @param verbDictionaryFile  -- path to verbs dictionary file
	 * @param extraDictionaryFile -- path to an optional extra dictionary file
	 * @param modelFile           -- path to Stanford Tagger
	 */
	public SoCal(String adjDictionaryFile, String advDictionaryFile, String intDictionaryFile, String nounDictionaryFile, String verbDictionaryFile, String extraDictionaryFile, String modelFile) {
		this.loadConfigurations();
		this.adjDictionaryFile = adjDictionaryFile;
		this.advDictionaryFile = advDictionaryFile;
		this.nounDictionaryFile = nounDictionaryFile;
		this.verbDictionaryFile = verbDictionaryFile; 
		this.intDictionaryFile = intDictionaryFile;
		if(this.configFlags.get("use_extra_dict"))
			this.extraDictionaryFile = extraDictionaryFile;
		this.loadDictionaries();
		
		this.tagger = new MaxentTagger(modelFile); // Stanford Tagger
		
	}
	
	private List<Map<String, String[][]>> getMultiwordEntries(String multiword) {
		if(multiword.contains("#")) // Ok
			for(Map.Entry<String, String> entry : this.macroReplace.entrySet()) {
				multiword = multiword.replace(entry.getKey(), entry.getValue());
			}
		
		String[] words = multiword.split("_");
		
		List<List<String>> entry = new ArrayList<>(); 
		int keyindex = words.length -1 ;
		
		for(int index = 0; index < words.length; index++) { // Ok
			String word = words[index];
			
			List<String> slot = new ArrayList<>();
			
			if(word.charAt(0) == '(') {
				slot.add("1");
				for(String wordTemp: word.substring(1, word.length()-1).split("\\|"))
					slot.add(wordTemp);
				keyindex = index;
			} else if(word.charAt(0) == '[') {
				Boolean ordinality = false;
				if(word.charAt(word.length()-1) != ']')
					ordinality = true;
				String modifier;
				if(ordinality) {
					modifier = "" + word.charAt(word.length()-1);
					word = word.substring(0, word.length()-1);
				} else
					modifier = "1";
				slot.add(modifier);
				for(String wordTemp: word.substring(1, word.length()-1).split("\\|"))
					slot.add(wordTemp);					 
			} else {
				slot.add("");
				slot.add(word);
			}
			entry.add(slot);
		} // Ok - Saída: Lista das partes da expressão ----> Partes da Expressão é uma lista de [0] modificadores [1~...] palavras
		
		String[][] entryArray = new String [entry.size()][];
		
		for(int i=0; i<entry.size(); i++) {
			entryArray[i] = new String[entry.get(i).size()];
			entryArray[i] = entry.get(i).toArray(entryArray[i]);
		} // Ok - Partes da expressão agora é um array[][]
		
		List<Map<String, String[][]>> finalEntries = new ArrayList<>();
		Map<String, String[][]> finalEntry;
		
		String key;
		entryArray[keyindex][0] = "#";
		for(int index = 1; index < entryArray[keyindex].length; index++) {
			finalEntry = new HashMap<>();
			key = entryArray[keyindex][index];
			finalEntry.put(key, entryArray);
			finalEntries.add(finalEntry);
		}
		// Ok - Lista com chaves relacionando cada parte da expressão
		return finalEntries;
	}
	
	private String tagText(String Text) {
		byte[] bytes = Text.getBytes( Charset.forName("UTF-8" ));
		Text =  new String( bytes, Charset.forName("UTF-8") );
		String Lines[] = this.addSpace(this.sentenceBoundary(this.swapNOT(Text))).split("\n");
		for(int i = 0; i < Lines.length; i++) {
			Lines[i] = this.tagger.tagString(Lines[i]).replace('_', '/'); 
		}
		return String.join("\n", Lines); 
	}
	
	private void loadDictionary(String DictName, Map<String, Double> dict, Map<String, Map<String[][], Double>> cDict) {		

		try {
			//InputStream in = getClass().getResourceAsStream(DictName);
			//BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedReader br = new BufferedReader(new FileReader(new File(DictName)));
			String line = br.readLine();
			while (line != null) {
				String[] data = line.split("\t");
				if(data.length == 2) {
					if(!data[0].contains("_"))
						dict.put(data[0], Double.valueOf(data[1]));
					else if(this.configFlags.get("use_multiword_dictionaries")) {
						
						List<Map<String, String[][]>> entries = getMultiwordEntries(data[0]);
						
						for(int i = 0; i < entries.size(); i++) { // For each keyword
							
							for(Map.Entry<String, String[][]> entry : entries.get(i).entrySet()) { // For the only entry in map
								
								Map<String[][], Double> valuedPair = new HashMap<>();
								if(!cDict.containsKey(entry.getKey())) { // If keyword isn't in dictionary
									valuedPair.put(entry.getValue(), Double.valueOf(data[1]));
									cDict.put(entry.getKey(), valuedPair);
								} else {
									valuedPair = cDict.remove(entry.getKey());
									valuedPair.put(entry.getValue(), Double.valueOf(data[1]));
									cDict.put(entry.getKey(), valuedPair);
								}
							}
							
						}
					}
				}

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
	public void loadDictionaries() {
		this.adjDictionary =new HashMap<>();
		this.advDictionary =new HashMap<>();
		this.nounDictionary = new HashMap<>();
		this.verbDictionary = new HashMap<>(); 
		this.intDictionary =  new HashMap<>();
		this.cAdjDictionary = new HashMap<>();
		this.cAdvDictionary = new HashMap<>();
		this.cNounDictionary = new HashMap<>();
		this.cVerbDictionary = new HashMap<>();
		this.cIntDictionary = new HashMap<>();
		loadDictionary(this.adjDictionaryFile, this.adjDictionary, this.cAdjDictionary);
		loadDictionary(this.advDictionaryFile, this.advDictionary, this.cAdvDictionary);
		loadDictionary(this.nounDictionaryFile, this.nounDictionary, this.cNounDictionary);
		loadDictionary(this.verbDictionaryFile, this.verbDictionary, this.cVerbDictionary); 
		loadDictionary(this.intDictionaryFile, this.intDictionary, this.cIntDictionary);
		if(this.configFlags.get("use_extra_dict")) 
			loadExtraDict(this.extraDictionaryFile);
		if(this.configFlags.get("simple_SO")) {
			applySimpleSO(this.adjDictionary, false);			
			applySimpleSO(this.advDictionary, false);
			applySimpleSO(this.nounDictionary, false);
			applySimpleSO(this.verbDictionary, false);
			applySimpleSO(this.intDictionary, true);
			
			applyCSimpleSO(this.cAdjDictionary, false);
			applyCSimpleSO(this.cAdvDictionary, false);
			applyCSimpleSO(this.cNounDictionary, false);
			applyCSimpleSO(this.cVerbDictionary, false);
			applyCSimpleSO(this.cIntDictionary, true);
		}
	}
	
	private void loadExtraDict(String DictName) {
		Map<String, Double> sDict = null;
		Map<String, Map<String[][], Double>> cDict = null;
		try {
			InputStream in = getClass().getResourceAsStream(DictName);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine().trim();
			while (line != null) {
				
				switch (line) {
				case "adjectives":
					sDict = this.adjDictionary;
					cDict = this.cAdjDictionary;
					break;
					
				case "nouns":
					sDict = this.nounDictionary;
					cDict = this.cNounDictionary;
					break;
				
				case "verbs":
					sDict = this.verbDictionary;
					cDict = this.cVerbDictionary;
					break;
					
				case "adverbs":
					sDict = this.advDictionary;
					cDict = this.cAdvDictionary;
					break;
					
				case "intensifiers":
					sDict = this.intDictionary;
					cDict = this.cIntDictionary;
					break;

				default:
					if(!sDict.equals(null)) {
						String[] data = line.split("\t");
						if(data.length == 2) {
							if(!data[0].contains("_"))
								sDict.put(data[0], Double.valueOf(data[1]));
							else if(this.configFlags.get("use_multiword_dictionaries")) {
								
								List<Map<String, String[][]>> entries = getMultiwordEntries(data[0]);
								
								for(int i = 0; i < entries.size(); i++) { // For each keyword
									
									for(Map.Entry<String, String[][]> entry : entries.get(i).entrySet()) { // For the only entry in map
										
										Map<String[][], Double> valuedPair = new HashMap<>();
										if(!cDict.containsKey(entry.getKey())) { // If keyword isn't in dictionary
											valuedPair.put(entry.getValue(), Double.valueOf(data[1]));
											cDict.put(entry.getKey(), valuedPair);
										} else {
											valuedPair = cDict.remove(entry.getKey());
											valuedPair.put(entry.getValue(), Double.valueOf(data[1]));
											cDict.put(entry.getKey(), valuedPair);
										}
									}
									
								}
							}
						}
					}
					break;
				}


				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An error ocurred while opening the lexicon file.");
			System.exit(-1);
		}
	}
	
	private void applyCSimpleSO(Map<String, Map<String[][], Double>> cDict, Boolean isIntDict) {
		if(!isIntDict.booleanValue()) {
			for(Map.Entry<String, Map<String[][], Double>> entry : cDict.entrySet()) {
				Map<String[][], Double> TempMap = new HashMap<>();
				for(Map.Entry<String[][], Double> entry2 : entry.getValue().entrySet()) {
					if (entry2.getValue() > 0.0)
		                TempMap.put(entry2.getKey(), 2.0); 
					else if(entry2.getValue() < 0.0)
						TempMap.put(entry2.getKey(), -2.0);
				}
				cDict.put(entry.getKey(), TempMap);
			}
		} else {
			for(Map.Entry<String, Map<String[][], Double>> entry : cDict.entrySet()) {
				Map<String[][], Double> TempMap = new HashMap<>();
				for(Map.Entry<String[][], Double> entry2 : entry.getValue().entrySet()) {
					if (entry2.getValue() > 0.0)
						TempMap.put(entry2.getKey(), 0.5);
					 else if(entry2.getValue() < 0.0 && entry2.getValue() > -1.0) 
						 TempMap.put(entry2.getKey(), -0.5);
					 else if(entry2.getValue() < -1.0)
						 TempMap.put(entry2.getKey(), -2.0);
				}
				cDict.put(entry.getKey(), TempMap);
			}
		}
	}
	
	private void applySimpleSO(Map<String, Double> dict, Boolean isIntDict) {
		if(!isIntDict.booleanValue()) {
			for(Map.Entry<String, Double> entry : dict.entrySet()) {
				if (entry.getValue() > 0.0)
	                dict.put(entry.getKey(), 2.0); 
				else if(entry.getValue() < 0.0)
					dict.put(entry.getKey(), -2.0);
			}
		} else {
			for(Map.Entry<String, Double> entry : dict.entrySet()) {
				if (entry.getValue() > 0.0)
					dict.put(entry.getKey(), 0.5);
				 else if(entry.getValue() < 0.0 && entry.getValue() > -1.0) 
					dict.put(entry.getKey(), -0.5);
				 else if(entry.getValue() < -1.0)
					dict.put(entry.getKey(), -2.0);
				
			}
		}
	}
	
	private void fillTextAndWeights(String infile) {
		double weight = 1.0;
		double tempWeight = 1.0;
		double weightModifier = 0.0;
		this.text = new ArrayList<>();
		this.weights = new ArrayList<>();
		this.boundaries = new ArrayList<>();
	    this.wordCounts = new ArrayList<>();
	    for(int i = 0; i < 4; i++) {
	    	Map<String, Integer> tempMap = new HashMap<>();
	    	this.wordCounts.add(tempMap);
	    }
		this.textSO = 0.0;
		this.SOCounter = 0;
		infile = infile.replace("<", " <").replace(">", "> ");
		for(String line: infile.split("\n")) {
			
			for(String word : line.trim().split(" ")) {
					
				if(!word.equals("")) {
					if(word.charAt(0)=='<' && word.charAt(word.length()-1)=='>') { // XML Tag
						String XMLTag = word.substring(1, word.length()-1);
						if(this.configFlags.get("use_XML_weighing")) {
							if(this.weightTags.containsKey(XMLTag))
								weightModifier = this.weightTags.get(XMLTag);
							else if(isDecimal(XMLTag))
								weightModifier = Double.parseDouble(XMLTag);
							else
								weightModifier = 1.0;
							if(word.charAt(1)=='/') {
								if(weightModifier != 0)
									weight /= weightModifier; // remove Weight
								else
									weight = tempWeight; // use pre-zero weight
							} else {
								if(weightModifier != 0)
									weight *= weightModifier; // add weight
								else {
									tempWeight = weight;
									weight = 0;
								}
							}
						}
					} else if(word.contains("/")){
						this.text.add(word.split("\\/"));
						this.weights.add(1.0);
					}
				}
			}
			this.boundaries.add(this.weights.size());
		}
		if(this.configFlags.get("use_weight_by_location")) {
			List<Double[]> rangeDict = convertRanges();
			for(int i = 0; i < this.weights.size(); i++) {
				for(int j = 0; j < rangeDict.size(); j++) {
					if(rangeDict.get(j)[0] <= (double) i/this.weights.size() && rangeDict.get(j)[1] > i/this.weights.size()) {
						this.weights.remove(i);
						this.weights.add(i, rangeDict.get(j)[2]);
					}
				}
			}
		}
	}
	/** 
	 * Converts a list of string ranges in faction form (e.g. ["1/4-1/2", 2]) into a a list of numerical ranges plus weight (e.g. [0.25, .5, 2]
	 * @return
	 */
	
	private List<Double[]> convertRanges() {
		List<Double[]> newRanges = new ArrayList<>();
		for(Map.Entry<String, Double> entry : this.weightsByLocation.entrySet()) {
			String[] Pair = entry.getKey().split("\\-"); 
			if(Pair.length == 2) {
				Double[] TempValues = new Double[3]; // [0] - StartRange [1] - EndRange [2] - Value
				TempValues[0] = convertFraction(Pair[0].trim());
				TempValues[1] = convertFraction(Pair[1].trim());
				
				if(TempValues[0] >= 0 && TempValues[0] <= 1 && TempValues[1] >= 0 && TempValues[1] <= 1 && TempValues[0] < TempValues[1]) {
					TempValues[2] = entry.getValue();
					newRanges.add(TempValues);
				}
			}
		}
		return newRanges;
	}
	
	private String[] getTextFromIndex(int index) {
		return this.text.get(index<0? this.text.size()+index: index);
	}

	@Override
	public int analyseText(String text) {
		String preProcessedText = this.tagText(text);
		this.fillTextAndWeights(preProcessedText);
		
		int advCount = this.advDictionary.size(); // For determining if there are new adverbs
		
		if(this.configFlags.get("fix_cap_tags"))
			fixAllCaps();
		if(this.configFlags.get("use_nouns")) {
			double nounsSO = 0.0;
			for(int index = 0; index < this.text.size(); index++) {
				String tag = this.getTextFromIndex(index)[1];
				if(tag.length() > 1 && tag.substring(0, 2).equals(this.nounTag)) {
					double wordSO = 0;
					try{
						wordSO = this.getNounSO(index);
					} catch(java.lang.ArrayIndexOutOfBoundsException e){
						wordSO = 0;
					}

					if(wordSO != 0) {
						wordSO = this.applyWeights(wordSO, index);
						nounsSO += wordSO;
					}
				}
			}
			int nounCount = this.sum_word_counts(this.wordCounts.get(0));
			if(nounCount > 0) {
				this.textSO += nounsSO;
				this.SOCounter += nounCount;
			}
		}
		if(this.configFlags.get("use_verbs")){
			double verbsSO = 0.0;
			for(int i = 0; i< this.text.size(); i++){
				if(this.getTextFromIndex(i).length == 2){
					String word = this.getTextFromIndex(i)[0];
					String tag = this.getTextFromIndex(i)[1];
					if(tag.length() > 1 && tag.substring(0, 2).equals(this.verbTag)){
						double wordSO = this.getVerbSO(i);
						if(wordSO != 0){
							wordSO = this.applyWeights(wordSO, i);
							verbsSO += wordSO;
						}
					}
				}
			}
			int verbCount = this.sum_word_counts(this.wordCounts.get(1));
			
			if(verbCount > 0){
				this.textSO += verbsSO;
		        this.SOCounter += verbCount;
			}
		}

		if(this.configFlags.get("use_adverbs")){
			double advsSO = 0.0;
			for(int i = this.text.size()-1; i>-1; i--){
				if(this.getTextFromIndex(i).length == 2){
					String word = this.getTextFromIndex(i)[0];
					String tag = this.getTextFromIndex(i)[1];
					if(tag.length() > 1 && tag.substring(0, 2).equals(this.advTag)){
						double wordSO = this.getAdvSO(i);
						if(wordSO != 0){
							wordSO = this.applyWeightsAdv(wordSO, i);
							advsSO += wordSO;
						}
					}
				}
				advCount = this.sum_word_counts(this.wordCounts.get(3));
			}
			if(advCount > 0){
				textSO += advsSO;
				SOCounter += advCount;
			}
		}

		if(this.configFlags.get("use_adjectives")) {
			double adjsSO = 0.0;
			for(int i = 0; i < this.text.size(); i++) { 
				if(this.getTextFromIndex(i).length == 2){
					String word = this.getTextFromIndex(i)[0];
					String tag = this.getTextFromIndex(i)[1];
					
					if(tag.length() > 1 && tag.substring(0, 2).equals(this.adjTag)) {
						double wordSO = this.getAdjSO(i);
						if(wordSO != 0) {
							wordSO = this.applyWeights(wordSO, i);
							adjsSO += wordSO;
						}
					}
				}
			}
			int adjCount = this.sum_word_counts(this.wordCounts.get(2));
			if(adjCount > 0) {
				this.textSO += adjsSO;
				this.SOCounter += adjCount;
			}
		}
		
		if(this.SOCounter > 0)
			this.textSO = this.textSO / this.SOCounter;
		if(this.textSO > 0)
			return POSITIVE;
		else if(this.textSO < 0)
			return NEGATIVE;
		else
			return NEUTRAL;		
	}
	/**
	 * This is the last step in the calculation, external weights and negation weights are applied
	 * @param wordSO
	 * @param index
	 * @return
	 */
	private double applyWeights(double wordSO, int index) {
		if(this.configFlags.get("use_heavy_negation") && wordSO < 0)
			wordSO *= this.configModifiers.get("neg_multiplier");
		wordSO *= this.weights.get(index);
		return wordSO;
	}
	
	/**
	 * Comparative and superlative adjectives require special stemming, and are
	 * treated as if they have been intensified with "more" or "most". Non-
	 * predicative uses of this kind of adjective are often not intended to
	 * express sentiment, and are therefore ignored. Adjectives often have
	 * more than one intensifier (e.g. really very good) so the search for
	 * intensifiers is iterative.
	 * @param index
	 * @return
	 */
	private double getAdjSO(int index) {
		String JJ = this.getWord(this.getTextFromIndex(index));
		String originalJJ = JJ;
		double intModifier = 0.0;
		
		if(StringUtils.isAllUpperCase(JJ)) 
			JJ = JJ.toLowerCase();
		if(this.sentPunct.contains(this.getWord(this.getTextFromIndex(index - 1))))
			JJ = JJ.toLowerCase();
		String adjType = this.getTag(this.getTextFromIndex(index)).substring(2);
		if(!this.configFlags.get("use_comparatives") && (adjType.equals("R") || this.comparatives.contains(this.getWord(this.getTextFromIndex(index-1)))))
			return 0;
		if(!this.configFlags.get("use_superlatives") && (adjType.equals("S") || this.superlatives.contains(this.getWord(this.getTextFromIndex(index-1))) || JJ.equals("best") || JJ.equals("worst")))
			return 0;
		if(adjType.equals("R") && !this.adjDictionary.containsKey(JJ) && !this.notWantedAdj.contains(JJ)) {
			JJ = this.stemCompJJ(JJ);
			if(this.configFlags.get("use_intensifiers"))
				intModifier += this.intDictionary.get("more");
		} else if(adjType.equals("S") && !this.adjDictionary.containsKey(JJ) && !this.notWantedAdj.contains(JJ)) {
			JJ = this.stemSuperAdj(JJ);
			if(this.configFlags.get("use_intensifiers"))
				intModifier += 1;
		}
		double[] multiwordResult;
		if(this.cAdjDictionary.containsKey(JJ))
			multiwordResult = this.findMultiword(index, this.cAdjDictionary.get(JJ));
		else
			multiwordResult = null;
		if(this.notWantedAdj.contains(JJ))
			return 0;
		else if((adjType.equals("S") || this.superlatives.contains(this.getWord(this.getTextFromIndex(index-1)))) && (!this.wordsWithinNum(index, definites, 2) || ! this.isInPredicate(index)) || ((adjType.equals("R") || this.comparatives.contains(this.getWord(this.getTextFromIndex(index -1)))) && ! this.isInPredicate(index)))
			return 0;
		else if(!this.adjDictionary.containsKey(JJ) && multiwordResult == null)
			return 0;
		else {
			double adjSO, backCount, forwardCount;
			int i;
			if(multiwordResult != null) {
				adjSO = multiwordResult[0];
				backCount = multiwordResult[1];
				forwardCount = multiwordResult[2];
				intModifier = multiwordResult[3];
				i = index - (int) backCount - 1;
			} else {
				adjSO = this.adjDictionary.get(JJ);
				i = index -1;
			}
			
			if(this.getTag(this.getTextFromIndex(i)).equals("DET") || this.getWord(this.getTextFromIndex(i)).equals("as"))
				i -= 1;
			
			if(this.configFlags.get("use_intensifiers")) {
				double[] intensifier = this.findIntensifier(i);
				while(intensifier != null) {
					intensifier = this.findIntensifier(i);
					if(intensifier != null){
						intModifier += intensifier[1];
						for (int j = 0 ; j < intensifier[0]; j++) {
							this.getTextFromIndex(i)[1] = "MOD";
							i -=1;
						}
					}
				}
			}
			int negation = this.find_negation(i, this.adjTag);
			
			double intModifierNegex = 0;
			if(negation != -1) {
				if(this.configFlags.get("use_intensifiers")) {
					i = negation -1;
					while(this.skipped.get("JJ").contains(this.getTextFromIndex(i)[0]))
						i -= 1;
					double[] intensifier = findIntensifier(i);
					if(intensifier != null) {
						intModifierNegex = intensifier[1];
						for (int j = 0 ; j < intensifier[0]; j++) {
							this.getTextFromIndex(i)[1] = "MOD";
							i -=1;
						}
					}
				}
				
			}
			
			if(intModifier != 0)
				adjSO *= (1 + intModifier);	
			 else if(this.configFlags.get("use_blocking") && this.findBlocker(adjSO, index, this.adjTag))
				adjSO = 0;
			
			if(this.configFlags.get("use_negation") && negation != -1) {
				double negShift;
				if(this.configFlags.get("neg_negation_nullification") && adjSO < 0)
					negShift = Math.abs(adjSO);
				else if(this.configFlags.get("polarity_switch_neg") || (this.configFlags.get("limit_shift") && Math.abs(adjSO) * 2 < this.configModifiers.get("adj_neg_shift")))
					negShift = Math.abs(adjSO) * 2;
				else
					negShift = this.configModifiers.get("adj_neg_shift");
				
				if(adjSO > 0) 
					adjSO -= negShift;
				else if(adjSO < 0)
					adjSO += negShift;
				
				if(this.configFlags.get("use_intensifiers") && intModifierNegex != 0)
					adjSO *= (1+intModifierNegex);		
			}
			adjSO = this.applyOtherModifiers(adjSO, index, i);
			
			if(intModifier != 0 && this.configModifiers.get("int_multiplier") != 1)
				adjSO *= this.configModifiers.get("int_multiplier");
			
			if(!this.wordCounts.get(2).containsKey(JJ))
				this.wordCounts.get(2).put(JJ, 1);
			else {
				this.wordCounts.get(2).put(JJ, this.wordCounts.get(2).get(JJ) + 1);
				if(negation == -1) {
					if(this.configFlags.get("use_word_counts_lower"))
						adjSO /= this.wordCounts.get(2).get(JJ);
					if(this.configFlags.get("use_word_counts_block"))
						adjSO = 0;
				}
			}
			
			if(this.configModifiers.get("adj_multiplier") != 1)
				adjSO *= this.configModifiers.get("adj_multiplier");
			
			return adjSO;
			
		}
	}
	/**
	 * backwards search for a verb of any kind. Used to determine if a comparative
     * or superlative adjective is in the predicate
	 * @param index
	 * @return
	 */
	private boolean isInPredicate(int index) {
		while(! this.atBoundary(index) && index > 0) {
			index -= 1;
			String tag = this.getTag(this.getTextFromIndex(index));
			if(tag.substring(0,2).equals("VB") || tag.equals("AUX") || tag.equals("AUXG"))
				return true;
		}
		return false;
	}

	/**
	 * @author lucas
	 * @param index
	 * @return
	 */
	private double getNounSO(int index) {
		String NN = this.getWord(this.getTextFromIndex(index));
		if(StringUtils.isAllUpperCase(NN)) // If all upper case, change to lower case
			NN = NN.toLowerCase();
		if(this.sentPunct.contains(this.getWord(this.getTextFromIndex(index))))
			NN = NN.toLowerCase(); // Change the word to lower case if sentence initial
		NN = this.stemNoun(NN);
		double multiwordResult[];
		if(this.cNounDictionary.containsKey(NN))
			multiwordResult = findMultiword(index, this.cNounDictionary.get(NN));
		else
			multiwordResult = null;
		
		if(!this.nounDictionary.containsKey(NN) && multiwordResult==null)
			return 0;
		else {
			double nounSO, backCount, forwardCount, intModifier;
			int i;
			if(multiwordResult != null) {
				nounSO = multiwordResult[0];
				backCount = multiwordResult[1];
				forwardCount = multiwordResult[2];
				intModifier = multiwordResult[3];
				i = index - 1 - (int)backCount;
			} else {
				intModifier = 0;
				nounSO = this.nounDictionary.get(NN);
				i = index -1;
			}
			
			if(this.configFlags.get("use_intensifiers")) {
				double intensifier[] = findIntensifier(i);
				if(intensifier!= null) {
					intModifier = intensifier[1];
					for(int j = 0; j < (int)intensifier[0]; j++) {
						this.getTextFromIndex(i)[1] = "MOD";
						i -= 1;
					}
				}
			}
			
			int negation = find_negation(i, this.nounTag);
			double intModifierNegex = 0;
			if(negation!= -1) {
				if(this.configFlags.get("use_intensifiers")) {
					intModifierNegex = 0;
					i = negation - 1;
					while(this.skipped.get(this.adjTag).contains(this.getTextFromIndex(i)[0]))
						i-=1;
					double intensifier[] = findIntensifier(i);
					if(intensifier!= null) {
						intModifierNegex = intensifier[1];
						for(int j = 0; j < (int)intensifier[0]; j++) {
							this.getTextFromIndex(i)[1] = "MOD";
							i -= 1;
						}
					}
				}
			}
			
			if(intModifier != 0.0)
				nounSO = nounSO * (1+intModifier);
			else if(this.configFlags.get("use_blocking") && this.findBlocker(nounSO, index, this.nounTag)) 
				nounSO = 0;
			if(this.configFlags.get("use_negation") && negation != -1) {
				double negShift;
				if(this.configFlags.get("neg_negation_nullification") && nounSO < 0)
						negShift = Math.abs(nounSO);
				else if(this.configFlags.get("polarity_switch_neg") || (this.configFlags.get("limit_shift") && Math.abs(nounSO) * 2 < this.configModifiers.get("noun_neg_shift")))
					negShift = Math.abs(nounSO) * 2;
				else
					negShift = this.configModifiers.get("noun_neg_shift");
				
				if(nounSO > 0)
					nounSO -= negShift;
				else if (nounSO < 0)
					nounSO += negShift;
				if(this.configFlags.get("use_intensifiers") && intModifierNegex != 0)
					nounSO *=(1+intModifierNegex);
			}
			
			nounSO = this.applyOtherModifiers(nounSO, index, i);
			
			if(nounSO != 0) {
				if(intModifier != 0 && this.configModifiers.get("int_multiplier") != 1.0)
					nounSO *= this.configModifiers.get("int_multiplier");
				if(!this.wordCounts.get(0).containsKey(NN))
					this.wordCounts.get(0).put(NN, 1);
				else {
					this.wordCounts.get(0).put(NN, this.wordCounts.get(0).get(NN) + 1);
					if(negation == -1) {
						if(this.configFlags.get("use_word_counts_lower"))
	                        nounSO /= (double) this.wordCounts.get(0).get(NN);
	                        
	                    if(this.configFlags.get("use_word_counts_block"))
	                        nounSO = 0;
					}
				}
			}
			
			if(this.configModifiers.get("noun_multiplier") != 1.0)
				nounSO *= this.configModifiers.get("noun_multiplier");
			return nounSO;
 		}
	}
	
	private double getVerbSO(int index){
		String VB = this.getWord(this.getTextFromIndex(index));
		if(StringUtils.isAllUpperCase(VB))
			VB = VB.toLowerCase(); // if all upper case, change to lower case    
		if(this.sentPunct.contains(this.getWord(this.getTextFromIndex(index-1))))
			VB = VB.toLowerCase(); // change the word to lower case if sentence initial
		String vtype = this.getTag(this.getTextFromIndex(index));
		vtype = vtype.substring(2, vtype.length());
		VB = this.stemVB(VB, vtype);
		double[] multiwordResult = {};
		if(this.cVerbDictionary.containsKey(VB)){
			multiwordResult = this.findMultiword(index, this.cVerbDictionary.get(VB));
		}else{
			multiwordResult = null;
		}
		if(this.notWantedVerb.contains(VB))
			return 0;
		else if(!this.verbDictionary.containsKey(VB) && multiwordResult == null)
			return 0;		
		else{
			int i;
			double intModifier, verbSO;
			if(multiwordResult!= null){
				verbSO = multiwordResult[0];
				double backcount = multiwordResult[1];
				//double forwardcount = multiwordResult[2];
				intModifier = multiwordResult[3];
				i = (int) (index - backcount - 1);
				
			}else{
				intModifier = 0;
	            verbSO = this.verbDictionary.get(VB);
	            i = index - 1;
			}
			if(this.configFlags.get("use_intensifiers")){
				double[] intensifier = this.findIntensifier(i);
				if(intensifier != null){
					intModifier += intensifier[1];
					for(int j = 0; j < intensifier[0]; j++){
						this.getTextFromIndex(i)[1] = "MOD";
						i -= 1;
					}
				}
				if(this.configFlags.get("use_clause_final_int")){ // look for clause-final modifier
					int edge = this.findVPBoundary(index);
					intensifier = this.findIntensifier(edge-1);
					if(intensifier != null){
						intModifier = intensifier[1];
						for(int j=0; j < (int) intensifier[0]; j++){
							this.getTextFromIndex(edge-1-j)[1] = "MOD";
						}
					}
				}
			}
			int negation = this.find_negation(i, verbTag);
			double intModifierNegex = 0;
			if(negation != -1){
				if(this.configFlags.get("use_intensifiers")){
					intModifierNegex = 0;
	                i = negation - 1;
	                while(this.skipped.get("JJ").contains(this.getTextFromIndex(i)[0])){
	                	i -= 1;
	                }
	                double[] intensifier = this.findIntensifier(i);
	                if(intensifier != null){
	                	intModifierNegex = intensifier[1];
	                	for(int j=0; j<intensifier[0]; j++){
	                		this.getTextFromIndex(i)[1] = "MOD";
	                		i-= 1;
	                	}
	                }
				}
			}
			
			if(intModifier != 0)
				verbSO = verbSO *(1+intModifier);
			else if(this.configFlags.get("use_blocking") && this.findBlocker(verbSO, index, verbTag))
				verbSO = 0;
			if(this.configFlags.get("use_negation") && negation != -1){
				double negShift;
				if(this.configFlags.get("neg_negation_nullification") && verbSO < 0)
					negShift = Math.abs(verbSO);
				else if(this.configFlags.get("polarity_switch_neg") || (this.configFlags.get("limit_shift") && Math.abs(verbSO)*2 < this.configModifiers.get("verb_neg_shift")))
					negShift = Math.abs(verbSO)*2;
				else
					negShift = this.configModifiers.get("verb_neg_shift");
				if(verbSO > 0)
					verbSO -= negShift;
				else if(verbSO < 0)
					verbSO += negShift;
				if(this.configFlags.get("use_intensifiers") && intModifierNegex != 0)
					verbSO *= (1+intModifierNegex);
			}
			verbSO = this.applyOtherModifiers(verbSO, index, i);
			if(verbSO != 0){
				if(intModifier != 0 && this.configModifiers.get("int_multiplier") != 1)
					verbSO *= this.configModifiers.get("int_multiplier");
				if(!this.wordCounts.get(1).containsKey(VB))
					this.wordCounts.get(1).put(VB, 1);
				else{
					this.wordCounts.get(1).put(VB, this.wordCounts.get(1).get(VB) + 1);
					if(negation == -1){
						if(this.configFlags.get("use_word_counts_lower"))
							verbSO /= this.wordCounts.get(1).get(VB);
						if(this.configFlags.get("use_word_counts_block"))
							verbSO = 0;
					}
				}
																
			}
			if(this.configModifiers.get("verb_multiplier") != 1)
				verbSO *= this.configModifiers.get("verb_multiplier");
			return verbSO;
		}		
	}
	
	/* There are two special things to note about dealing with adverbs: one is that
	 * their SO value can be derived automatically from the lemma in the
	 * adjective dictionary. The other is the special handling of "too", which
	 * is counted only when it does not appear next to punctuation (which rules out
	 * most cases of "too" in the sense of "also")
	 */
	private double getAdvSO(int index){
		String RB = this.getWord(this.getTextFromIndex(index));
		String originalRB = RB;
		if(StringUtils.isAllUpperCase(RB))
			RB.toLowerCase(); // if all upper case, change to lower case    
		if(this.sentPunct.contains(this.getWord(this.getTextFromIndex(index-1==-1?this.text.size()-1:index-1))))
			RB.toLowerCase(); // change the word to lower case if sentence initial
		if(this.configFlags.get("adv_learning") && !this.advDictionary.containsKey(RB) && !this.notWantedAdv.contains(RB)){
			String JJ = this.stemAdvToAdj(RB); // stem the adverb to its corresponding adj
			if(this.adjDictionary.containsKey(JJ)){
				this.advDictionary.put(RB, this.adjDictionary.get(JJ)); // take its SO value
				//this.newAdvDictionary.put(RB, this.adjDictionary.get(JJ)); This may not be necessary
			}
		}
		double[] multiwordResult = {};
		if(this.cAdvDictionary.containsKey(RB))
			multiwordResult = this.findMultiword(index, this.cAdvDictionary.get(RB));
		else
			multiwordResult = null;
		if(this.notWantedAdj.contains(RB) || ((RB.equals("too") && index < this.text.size()-1 && this.punct.contains(this.getWord(this.getTextFromIndex(index+1))))) || (RB.equals("well") && index < this.text.size()-1 && this.getWord(this.getTextFromIndex(index+1)).equals(",")))
			return 0;
		else if(!this.advDictionary.containsKey(RB) && multiwordResult == null)
			return 0;
		else{
			double advSO, backcount, forwardcount, intModifier;
			int i;
			double[] intensifier;
			if(multiwordResult != null){
				advSO = multiwordResult[0];
				backcount = multiwordResult[1];
				forwardcount = multiwordResult[2];
				intModifier = multiwordResult[3];
				i = index -1;
			}else{
				intModifier = 0;
				advSO = this.advDictionary.get(RB);
				i = index - 1;
			}
			if(this.getWord(this.getTextFromIndex(i==-1?this.text.size()-1:i-1)).equals("as")) // look past "as" for intensification
				i -= 1;
			if(this.configFlags.get("use_intensifiers")){
				intensifier = this.findIntensifier(i);
				if(intensifier != null){
					intModifier += intensifier[1];
					for(int j = 0; j < intensifier[0]; j++){
						this.getTextFromIndex(i)[1] = "MOD"; // block modifier being used twice
						i -= 1;
					}
				}
			}
			int negation = this.find_negation(i, advTag);
			double intModifierNegex = 0;
			if(negation != -1){
				if(this.configFlags.get("use_intensifiers")){
					intModifierNegex = 0;
	                i = negation - 1;
	                while(this.skipped.get("JJ").contains(this.getTextFromIndex(i)[0]))              	
	                	i -= 1;
	                intensifier = this.findIntensifier(i);
	                if(intensifier != null){
	                	intModifierNegex = intensifier[1];
	                	for(int j = 0; j < intensifier[0]; j++){
	                		this.getTextFromIndex(i)[1] = "MOD"; // block modifier being used twice
	                		i -= 1;
	                	}
	                }
				}
			}
			if(intModifier != 0)
				advSO = advSO *(1+intModifier);
			else if(this.configFlags.get("use_blocking") && this.findBlocker(advSO, index, advTag))
				advSO = 0;
			if(this.configFlags.get("use_negation") && negation != -1){
				double negShift;
				if(this.configFlags.get("neg_negation_nullification") && advSO < 0)
					negShift = Math.abs(advSO);
				else if(this.configFlags.get("polarity_switch_neg") || (this.configFlags.get("limit_shift") && Math.abs(advSO)*2 < this.configModifiers.get("adv_neg_shift")))
					negShift = Math.abs(advSO) * 2;
				else
					negShift = this.configModifiers.get("adv_neg_shift");
				if(advSO > 0)
					advSO -= negShift;
				else if(advSO < 0)
					advSO += negShift;
				if(this.configFlags.get("use_intensifiers") && intModifierNegex != 0)
					advSO *= (1+intModifierNegex);
			}
			advSO = this.applyOtherModifiers(advSO, index, i);
			if(advSO != 0){
				if(intModifier != 0 && this.configModifiers.get("int_multiplier") != 1)
					advSO *= this.configModifiers.get("int_multiplier");
				if(!this.wordCounts.get(3).containsKey(RB))
					this.wordCounts.get(3).put(RB, 1);
				else{
					this.wordCounts.get(3).put(RB, this.wordCounts.get(3).get(RB) + 1);
					if(negation == -1){
						if(this.configFlags.get("use_word_counts_lower"))
							advSO /= this.wordCounts.get(3).get(RB);
						if(this.configFlags.get("use_word_counts_block"))
							advSO = 0;
					}
				}				
				
			}
			if(this.configModifiers.get("adv_multiplier") != 1)
				advSO *= this.configModifiers.get("adv_multiplier");
			return advSO;
		}		
	}
	
	/* this is the last step in the calculation, external weights and negation
	 * weights are applied
	 */
	private double applyWeightsAdv(double wordSO, int index){
		if(this.configFlags.get("use_heavy_negation") && wordSO < 0) // weighing of negative SO
			wordSO *= this.configModifiers.get("neg_multiplier"); // items
		wordSO *= this.weights.get(index);
		return wordSO;
	}
	
	/* several modifiers that apply equally to all parts of speech based on
 	 * their context. Words in all caps, in a sentences ending with an
 	 * exclamation mark, or with some other highlighter are intensified,
 	 * while words appearing in a question or quotes or with some other
 	 * irrealis marker are nullified
	 */
	private double applyOtherModifiers(double SO, int index, int leftedge){
		if(this.configFlags.get("use_cap_int") && StringUtils.isAllUpperCase(this.getWord(this.getTextFromIndex(index))))
			SO *= this.configModifiers.get("capital_modifier");
		if(this.configFlags.get("use_exclam_int") && this.getSentPunct(index).equals("!"))
			SO *= this.configModifiers.get("exclam_modifier");
		if(this.configFlags.get("use_highlighters")){
			String highlighter = this.getSentHighlighter(leftedge);
			if(highlighter != null)
				SO *= this.highlighters.get(highlighter);
		}
		if(this.configFlags.get("use_quest_mod") && this.getSentPunct(index).equals("?") && !(this.configFlags.get("use_definite_assertion") && this.wordsWithinNum(leftedge, definites, 1)))
			SO = 0;
		if(this.configFlags.get("use_imperative") && this.isInImperative(leftedge))
			SO = 0;
		if(this.configFlags.get("use_quote_mod") && this.isInQuotes(index))
			SO = 0;
		if(this.configFlags.get("use_irrealis") && this.hasSentIrrealis(leftedge))
			SO = 0;
		return SO;
	}
	
	private String getSentPunct(int index){
		while(!this.sentPunct.contains(this.getTextFromIndex(index)[0])){
			if(index == this.text.size() - 1) // if the end of the text is reached
				return "EOF";
			index += 1;
		}
		return this.getWord(this.getTextFromIndex(index));		
	}
	
	
	/* If there is a word in the sentence prior to the index but before a boundary
	 * marker (including a boundary marker) in the highlighter list, return it
	 */
	private String getSentHighlighter(int index){
		while(index != -1 && !this.atBoundary(index)){
			if(this.highlighters.containsKey(this.getWord(this.getTextFromIndex(index)).toLowerCase()))
				return this.getWord(this.getTextFromIndex(index)).toLowerCase();
			else
				index -= 1;
		}
		return null;
	}
	
	/* check to see if something in words_tags is within num of index (including
	 * index), returns true if so
	 */
	private boolean wordsWithinNum(int index, List<String> wordsTags, int num){
		while(num > 0){
			if(wordsTags.contains(this.getWord(this.getTextFromIndex(index==-1?this.text.size()-1:index))) || wordsTags.contains(this.getTag(this.getTextFromIndex(index==-1?this.text.size()-1:index))))
				return true;
			num -= 1;
			index -= 1;
		}
		return false;
	}
	
	/* Tries to determine if the word at index is in an imperative based on whether
	 * first word in the clause is a VBP (and not a question or within the
	 * scope of a definite determiner) 	
	 */
	private boolean isInImperative(int index){
		if(!this.getSentPunct(index).equals("?") && !this.wordsWithinNum(index, definites, 1)){
			int i = index;
			while(i>-1 && !this.sentPunct.contains(this.getWord(this.getTextFromIndex(i)))){
				if(this.atBoundary(index))
					return false;
				i -= 1;
			}
			String word = this.getTextFromIndex(i+1)[0];
			String tag = this.getTextFromIndex(i+1)[1];
			String[] arr = {"were", "was", "am"};
			if((tag.equals("VBP") || tag.equals("VB")) && !Arrays.asList(arr).contains(word.toLowerCase()))
				return true;
		}
		return false;
	}
		
	/*check to see if a particular word is contained within quotation marks.
	 * looks to a sentence boundary on the left, and one past the sentence
	 * boundary on the right; an item in quotes should have an odd number of
	 * quotation marks in the sentence on either sides
	 */
	private boolean isInQuotes(int index){
		int quotesLeft = 0;
	    int quotesRight = 0;
	    boolean found = false;
	    String current = "";
	    int i = index;
	    while(!this.sentPunct.contains(current) && i > -1){
	    	current = this.getWord(this.getTextFromIndex(i));
	    	if(current.equals('"') || current.equals("'"))
	    		quotesLeft += 1;
	    	i-= 1;
	    }
	    if(quotesLeft%2 == 1){
	    	current = "";
	    	i = index;
	    	while(!found && !this.sentPunct.contains(current) && i < this.text.size()){
	    		current = this.getWord(this.getTextFromIndex(i));
	    		if(current.equals('"') || current.equals("'"))
	    			quotesRight += 1;
	    		i += 1;
	    	}
	    	if((quotesLeft - quotesRight == 1) && (i < this.text.size()-1) && (this.getWord(this.getTextFromIndex(i+1)).equals('"')))
	    		quotesRight += 1;
	    	if(quotesRight%2 == 1){
	    		found = true;
	    	}
	    }
	    return found;
	}
	
	/* Returns true if there is a irrealis marker in the sentence and no
	 * punctuation or boundary word intervenes between the marker and the index
	 */
	private boolean hasSentIrrealis(int index){
		if(!(this.configFlags.get("use_definite_assertion") && this.wordsWithinNum(index, definites, 1))){
			while(index != -1 && !this.atBoundary(index)){
				if(this.irrealis.contains(this.getWord(this.getTextFromIndex(index)).toLowerCase()))
					return true;
				index -= 1;
			}
		}
		return false;
	}
	
	private boolean isBlocker(double SO, int index){
		if(index > -1 && index < this.text.size() && this.getTextFromIndex(index).length == 2){
			String modifier = this.getTextFromIndex(index)[0];
			String tag = this.getTextFromIndex(index)[1];
			if(tag.equals(advTag) && this.advDictionary.containsKey(modifier) && Math.abs(this.advDictionary.get(modifier)) >= this.configModifiers.get("blocker_cutoff")){
				if(Math.abs(SO + this.advDictionary.get(modifier)) < Math.abs(SO) + Math.abs(this.advDictionary.get(modifier)))
					return true;				
			}else if(tag.equals(this.adjTag) && this.adjDictionary.containsKey(modifier) && Math.abs(this.adjDictionary.get(modifier)) >= this.configModifiers.get("blocker_cutoff")){
				if(Math.abs(SO + this.adjDictionary.get(modifier)) < Math.abs(SO) + Math.abs(this.adjDictionary.get(modifier)))
					return true;
			}else if(tag.length() > 1 && tag.substring(0, 2).equals(this.verbTag) && this.verbDictionary.containsKey(modifier) && Math.abs(this.verbDictionary.get(modifier)) >= this.configModifiers.get("blocker_cutoff")){
				if(Math.abs(SO + this.verbDictionary.get(modifier)) < Math.abs(SO) + Math.abs(this.verbDictionary.get(modifier)))
					return true;
			}
		}
		return false;
	}
	
	/* this function tests if the item at index is of the correct type, orientation
	 * and strength (as determined by blocker_cutoff) to nullify a word having the
	 * given SO value
	 */
	private boolean findBlocker(double SO, int index, String POS){
		boolean stop = false;
		while(index > 0 && !stop && !this.atBoundary(index)){
			if(this.getTextFromIndex(index-1).length == 2){
				String modifier = this.getTextFromIndex(index-1)[0];
				String tag = this.getTextFromIndex(index-1)[1];
				if(this.isBlocker(SO, index-1))
					return true;
				if(tag.length() > 1 && !this.skipped.get(POS).contains(modifier) && !this.skipped.get(POS).contains(tag.substring(0, 2)))
					stop = true;
			}
			index -= 1;
		}
		return false;
	}
	
	/*forward search for the index immediately preceding punctuation or a boundary
	 *word or punctuation. Used to find intensifiers remote from the verb
	 */
	private int findVPBoundary(int index){
		while(!this.atBoundary(index) && index < this.text.size()-1){
			index += 1;
		}
		return index;
	}
	
	private boolean atBoundary(int index){
		if(this.boundaries.contains(index+1))
			return true;
		else if(this.configFlags.get("use_boundary_punctuation") && this.punct.contains(this.getWord(this.getTextFromIndex(index!=-1?index:this.text.size()-1))))
			return true;
		else if(this.configFlags.containsKey("use_boundary_words") && this.boundaryWords.contains(this.getWord(this.getTextFromIndex(index!=-1?index:this.text.size()-1))))
			return true;
		else
			return false;
			
	}
	
	/* looks backwards for a negator and returns its index if one is found and
	 * there is no intervening puctuation or boundary word. If restricted negation
	 * is used (for the given word type), the search will only continue if each
	 * word or its tag is in the skipped list for its type
	 */
	private int find_negation(int index, String wordType){
		boolean search = true;
		int found = -1;
		while(search && !this.atBoundary(index) && index != -1){
			String current = this.getWord(this.getTextFromIndex(index)).toLowerCase();
			if(this.negators.contains(current)){
				search = false;
				found = index;
			}
			if(this.configFlags.get("restricted_neg_" + wordType) && this.skipped.get(wordType).contains(current) && !this.skipped.get(wordType).contains(this.getTag(this.getTextFromIndex(index)))){
				search = false;
			}
			index = -1;
		}
		return found;
	}
	
	/**
	 * This function determines whether the words surrounding the key word at index match one of the dictionary
	 * definitions in dict_entry_list. If so, it returns a list containing the SO value, the number of words in 
	 * the phrase, that are to the left of index, the number of words to the right, and the value of any 
	 * intensifier. Any word specifically designated in the defintion will have its tag changed to "MOD" so that
	 * it will not be counted twice 
	*/
	private double[] findMultiword(int index, Map<String[][], Double> dictEntryList) {
		for(Map.Entry<String[][], Double> dictEntry : dictEntryList.entrySet()) {
			String words[][] = dictEntry.getKey();
			double SO = dictEntry.getValue();
			
			int start = 0; // Stores index of key value of compound word
			for(int i = 0; i < words.length; i++)
				if(words[i][0].equals("#"))
					start = i;
			double intensifier = 0;
			
			double[] returnMultiWordF = new double[2];
			if(start < words.length -1) {
				returnMultiWordF = this.matchMultiwordF(index + 1, Arrays.copyOfRange(words, start+1, words.length));
				if(returnMultiWordF[1] != 0)
					intensifier = returnMultiWordF[1]; // int_temp 
			} else
				returnMultiWordF[0] = 0.0; // Countfoward
			
			double[] returnMultiWordB = new double[2];
			if(start > 0) {
				returnMultiWordB = this.matchMultiwordB(index-1, Arrays.copyOfRange(words, 0, start));
				if(returnMultiWordB[1] != 0)
					intensifier = returnMultiWordB[1]; // int_temp 
			} else
				returnMultiWordB[0] = 0.0; // Countback
			if(returnMultiWordF[0] != -1 && returnMultiWordB[0] != -1) {
				for(int i = index - (int)returnMultiWordB[0]; i < index + (int)returnMultiWordF[0] + 1; i++) {
					for(int j = 0; j < dictEntry.getKey().length; j++)
						for(int k = 1; k < dictEntry.getKey()[j].length; k++)
							if(this.getWord(this.getTextFromIndex(i)).equals(dictEntry.getKey()[j][k]))
								this.getTextFromIndex(i)[1] = "MOD";
				}
				
				return new double[] {SO, returnMultiWordB[0], returnMultiWordF[0], intensifier};
			}
		}
		return null;
	}

	private double[] matchMultiwordB(int index, String[][] words) {
		double[] temp;
		if(words.length == 0){
			return new double[] {0,0};
		}else{
			String[] current = words[words.length-1];
			if(current[0].equals("") && current.length==2)
				current[0] = "1"; // Unmodified words should be appear once
			if(current[0].equals("0"))
				return this.matchMultiwordB(index, Arrays.copyOfRange(words, 0, words.length-1)); // This word done
			if(current[0].equals("?") || current[0].equals("*")) { // Word optional - try
				temp = this.matchMultiwordB(index, Arrays.copyOfRange(words, 0, words.length-1));
				if(temp[0] != -1)
					return temp;
			}
			if(index == -1)
				return new double[] {-1, 0};
			boolean match = false;
			for(String wordOrTag : Arrays.copyOfRange(current, 1, current.length)) {
				if(StringUtils.isAllLowerCase(wordOrTag)) // Match by word
					match = (match || getWord(this.getTextFromIndex(index)).toLowerCase() == wordOrTag);
				else if(StringUtils.isAllUpperCase(wordOrTag)) { // Match by tag
					if(wordOrTag.equals("INT")) { // If looking for a intensifiers
						double [] intensifier = this.findIntensifier(index);
						if(intensifier != null) {
							int i = (int) intensifier[0];
							double[] result = this.matchMultiwordB(index  - i, Arrays.copyOfRange(words, 0, words.length-1));
							if(result[0] != -1)
								return new double[] {result[0] + i, intensifier[1]};
						}
					} else
						match = (match || getTag(this.getTextFromIndex(index)) == wordOrTag);
				}
			}
			if(!match)
				return new double[] {-1, 0};
			else {
				if(current[0].equals("*")) {
					temp = matchMultiwordB(index - 1, words);
				} else if (current[0].equals("+")) {
					
					String[][] tempMatrix = new String[words.length][];
					for(int i = 0; i < words.length; i++) {
						tempMatrix[i] = new String[words[i].length];
						for(int j = 0 ; j < words[i].length; j++) {
							tempMatrix[i][j] = words[i][j];
						}
					}
					tempMatrix[tempMatrix.length-1][0] = "*";
					
					temp = matchMultiwordB(index - 1, tempMatrix);
				} else if (current[0].equals("?")) {
					temp = matchMultiwordB(index - 1, Arrays.copyOfRange(words, 0, words.length - 1));
				} else {
					String[][] tempMatrix = new String[words.length][];
					for(int i = 0; i < words.length; i++) {
						tempMatrix[i] = new String[words[i].length];
						for(int j = 0 ; j < words[i].length; j++) {
							tempMatrix[i][j] = words[i][j];
						}
					}
					
					tempMatrix[tempMatrix.length-1][0] = String.valueOf(Integer.parseInt(tempMatrix[tempMatrix.length-1][0]) - 1);
					temp = matchMultiwordB(index - 1, tempMatrix);
				}
				
				if(temp[0] == -1) // failed
					return temp;
				else // success
					return new double[] {temp[0] + 1.0, temp[1]};
			}
		}
	}

	/**
	 * Tagger tags most all uppercase words as NNP, this function tries to see if they belong in another dictionary (if so, it changes the tag)
	 */
	private void fixAllCaps() {
		for(int i = 0; i < this.text.size(); i++) {
			if(this.getTextFromIndex(i).length == 2) {
				String word = this.getTextFromIndex(i)[0];
				String tag = this.getTextFromIndex(i)[1];
				if(word.length() > 2 && (StringUtils.isAllUpperCase(word) || Character.isUpperCase(word.charAt(0))) && tag.equals("NNP")) {
					word = word.toLowerCase();
					if(this.adjDictionary.containsKey(word) || this.cAdjDictionary.containsKey(word)) {
						this.text.get(i)[1] = "JJ";
					} else if(this.advDictionary.containsKey(word) || this.cAdvDictionary.containsKey(word)) {
						this.text.get(i)[1] = "RB";
					} else {
						String exTag = new String(); // Verbs need to be stemmed
						if(word.charAt(word.length()-1) == 's') {
							word = stemVB(word, "Z");
							exTag = "Z";
						} else if(word.substring(word.length() - 3).equals("ing")) {
							word = stemVB(word, "G");
							exTag = "G";
						} else if(word.substring(word.length() - 2).equals("ed")) {
							word = stemVB(word, "D");
							exTag = "D";
						}
						
						if(this.verbDictionary.containsKey(word) || this.cVerbDictionary.containsKey(word))
							this.text.get(i)[1] = "VB"+exTag;
					}
				}
			}
		}
	}
	
	private String swapNOT(String Input) {
		String OldStr[] = {"aren't", "can't", "couldn't", "didn't", "doesn't", "don't", "hadn't", "hasn't", "haven't", "isn't", "mightn't", 
				"mustn't", "oughtn't", "shan't", "shouldn't", "wasn't", "weren't", "won't", "wouldn't", "arent", "cant", "couldnt", "didnt", 
				"doesnt", "dont", "hadnt", "hasnt", "havent", "isnt","mightnt", "mustnt", "oughtnt","shant", "shouldnt", "wasnt","werent", 
				"wont", "wouldnt" };
		String NewStr[] = { "are not", "can not", "could not", "did not", "does not", "do not", "had not", "has not", "have not", "is not", "might not", 
				"must not", "ought not", "shall not", "should not", "was not", "were not", "will not", "would not", "are not", "can not", "could not", 
				"did not", "does not", "do not","had not", "has not", "have not", "is not", "might not", "must not", "ought not", "shall not", 
				"should not", "was not", "were not", "will not", "would not"};
		for(int i =0; i< OldStr.length; i++)
			Input = Input.replaceAll(OldStr[i], NewStr[i]);
		return Input;
	}
	
	private String sentenceBoundary(String Input) {
		// Split Input in sub strings terminated by a sentence boundary . ? or !
		Input = this.replaceRegex("(.+?[.!?][\")]?)(?=(?:\\s+[\"(]?[A-Z]|\\s*$))", "$1"+"\n", Input);
		// Remove spaces before substrings
		Input = this.replaceRegex("(\\n)+(\\s)+", "\n", Input);
		// Remove mistakes (Abbreviations will have a wrong line break after it).
		Input = this.replaceRegex("(Dr|Mr|Ms|Mrs|Miss|Sir|Hon|Jr|Sr|Cpl|Capt|Comdr|Sgt|Ltd|Corp|Inc|Ave|St|Blvd|Rd|dept|dist|div|est|etc|(A\\.M)|(P \\. " +
				"M)|(U \\. S)|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sept|Oct|Nov|Dec)\\s*\\.\\s*\\n", "$1"+ ". ", Input);

		return Input;
	}
	
	private String addSpace(String Input) {
		// Swap slashes for commas
		Input = this.replaceRegex("\\/", ",", Input);
		// Look for two words separated by a comma or slash and space the comma slash
		Input = this.replaceRegex("([a-z|A-Z]+)(\\/|\\,)([a-z|A-Z]+)", "$1$2 $3", Input);
		// Look two non word characters one after other and both before a word character and space them all.
		Input = this.replaceRegex("([\\W&&[^\\s]])([\\W&&[^\\s]])(\\w)", "$1 $2 $3", Input);
		Input = this.replaceRegex("^([\\W&&[^\\s]])(\\w)", "$1 $2", Input);
		// This will take care of punctuation marks at the end of a word
	    Input = this.replaceRegex("(\\w)([\\W&&[^\\s]])([\\W&&[^\\s]])", "$1 $2 $3", Input);
	    Input = this.replaceRegex("(\\w)([\\W&&[^\\s]])", "$1 $2", Input);
	    Input = this.replaceRegex("([\\,\\.\\:\\;\\(\\)\\\"\\?\\!\\'\\[\\]\\{\\}\\@\\#\\$\\%\\^\\&\\*\\=\\+\\|\\\\\\<\\>\\/\\~\\`\\-])" +
	    		"([\\,\\.\\:\\;\\(\\)\\\"\\?\\!\\'\\[\\]\\{\\}\\@\\#\\$\\%\\^\\&\\*\\=\\+\\|\\\\\\<\\>\\/\\~\\`\\-])+", "$1", Input);
	    //TODO - This code is a translate from a Perl Script inside the So-Cal, I think it has some mistakes
		return Input;
	}
	/**
	 * @author Lucas
	 * @param pattern -- Regular expression that will be used as 'search element'
	 * @param Replace -- String that will be replaced
	 * @param Input   -- The string that will changes
	 * @return        -- Input with each match of Pattern replaced by Replace
	 */
	private String replaceRegex(String pattern, String Replace, String Input) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(Input);		
		return m.replaceAll(Replace);
	}
	
	/**
	 * @author Manoel
	 * @param fraction	-- String representing a fraction value
	 * @return			-- Double value corresponding to the fraction string 
	 */
	private double convertFraction(String fraction){
		if(!fraction.contains("/")){
			return Double.parseDouble(fraction);			
		}else{
			String[] parts = fraction.split("/");
			if(parts.length == 2)
				return (double) Integer.parseInt(parts[0])/Integer.parseInt(parts[1]);
			else
				return -1;
		}
	}
	
	/**
	 * @author Manoel
	 * @param input	-- String that can possibly represent a numeric value
	 * @return		-- Boolean value that indicates is input is numeric or not 
	 */
	private boolean isDecimal(String input){
		return input.matches("-?\\d+(\\.\\d)?\\d*"); //match a number with optional '-' and decimal
	}
	
	//English steming functions
	
	private String stemNN(String NN){
		if(!this.nounDictionary.containsKey(NN) && !this.cNounDictionary.containsKey(NN) && NN.length() > 2 && NN.endsWith("s")){  // boys -> boy
			NN = NN.substring(0, NN.length()-1);
			if(!this.nounDictionary.containsKey(NN) && !this.cNounDictionary.containsKey(NN) && NN.endsWith("e")){ // watches -> watch
				NN = NN.substring(0, NN.length()-1);
				if(!this.nounDictionary.containsKey(NN) && !this.cNounDictionary.containsKey(NN) && NN.endsWith("i")){ // flies -> fly
					NN = NN.substring(0, NN.length()-1).concat("y");					
				}
			}
		}
		return NN;			
	}
	
	private String stemVB(String VB, String type){
		if(type == "" || type == "P" || VB.length() < 4 || this.verbDictionary.containsKey(VB) || this.cVerbDictionary.containsKey(VB)){
			return VB;
		}else if(type == "D" || type == "N"){
			if(VB.endsWith("d")){
				VB = VB.substring(0, VB.length()-1); // loved -> love
				if(!this.verbDictionary.containsKey(VB) && !this.cVerbDictionary.containsKey(VB)){
					if(VB.endsWith("e"))
						VB = VB.substring(0, VB.length()-1); // enjoyed -> enjoy
					if(!this.verbDictionary.containsKey(VB) && !this.cVerbDictionary.containsKey(VB)){
						if(VB.endsWith("i")){
							VB = VB.substring(0, VB.length()-1);
							VB = VB.concat("y"); //tried -> try
						}else if(VB.length() > 1 && VB.charAt(VB.length()-1) == VB.charAt(VB.length()-2)){
							VB = VB.substring(0, VB.length()-1); // compelled -> compel
						}							
					}
				}
			}
			return VB;
		}else if(type == "G"){
			VB = VB.substring(0, VB.length()-3); // obeying -> obey
			if(!this.verbDictionary.containsKey(VB) && !this.cVerbDictionary.containsKey(VB)){
				if(VB.length() > 1 && VB.charAt(VB.length()-1) == VB.charAt(VB.length()-2))
					VB = VB.substring(0, VB.length()-1); // stopping -> stop
				else
					VB = VB.concat("e"); // amusing -> amuse
			}
			return VB;
		}else if(type == "Z" && VB.length() > 3){
			if(VB.endsWith("s")){
				VB = VB.substring(0, VB.length()-1); // likes -> like
				if(!this.verbDictionary.containsKey(VB) && !this.cVerbDictionary.containsKey(VB) && VB.endsWith("e")){
					VB = VB.substring(0, VB.length()-1); // watches -> watch
					if(!this.verbDictionary.containsKey(VB) && !this.cVerbDictionary.containsKey(VB) && VB.endsWith("i")){
						VB = VB.substring(0, VB.length()-1); 
						VB = VB.concat("y"); // flies -> fly
					}
				}
			}
			return VB;
		}
		return VB;
	}
	
	/*
	 * used to find the adjective that is the stem of an adverb so that the adverb can be added automatically 
	 * to the dictionary
	 */
	private String stemRBToJJ(String RB){
		String JJ = RB;
		if(JJ.length() > 3 && JJ.endsWith("ly")){
			JJ = JJ.substring(0, JJ.length()-2); // sharply -> sharp
			if(!this.adjDictionary.containsKey(JJ)){
				if(this.adjDictionary.containsKey(JJ.concat("l")))
					JJ = JJ.concat("l"); // fully -> full
				else if(this.adjDictionary.containsKey(JJ.concat("le")))
					JJ = JJ.concat("le"); // simply -> simple
				else if(JJ.endsWith("i") && this.adjDictionary.containsKey(JJ.substring(0, JJ.length()-1).concat("y")))
					JJ = JJ.substring(0, JJ.length()-1).concat("y"); // merrily -> merry
				else if(JJ.length() > 5 && JJ.endsWith("al") && this.adjDictionary.containsKey(JJ.substring(0, JJ.length()-2)))
					JJ = JJ.substring(0, JJ.length()-2); //angelic -> angelically 
			}
		}
		return JJ;
	}
	
	/* this function does stemming for both comparative and superlative adjectives
	 * after the suffix "er" or "est" has been removed
	 */
	private String stemAtiveAdj(String JJ){
		if(!this.adjDictionary.containsKey(JJ)){
			if(this.adjDictionary.containsKey(JJ.concat("e")))
				JJ = JJ.concat("e"); //abler/ablest -> able
			else if(this.adjDictionary.containsKey(JJ.substring(0, JJ.length()-1)))
				JJ = JJ.substring(0, JJ.length()-1); //bigger/biggest -> big
			else if(JJ.endsWith("i") && this.adjDictionary.containsKey(JJ.substring(0, JJ.length()-1).concat("y")))
				JJ = JJ.substring(0, JJ.length()-1).concat("y"); //easier/easiest -> easy
		}
		return JJ;
	}
	
	private String stemCompJJ(String JJ){
		if(JJ.endsWith("er"))
			JJ = this.stemAtiveAdj(JJ.substring(0, JJ.length()-2)); //fairer -> fair
		return JJ;
	}
	
	private String stemSuperJJ(String JJ){
		if(JJ.endsWith("est"))
			JJ = this.stemAtiveAdj(JJ.substring(0, JJ.length()-3)); //fairest -> fair
		return JJ;
	}
	
	/*
	 * The methods below (Language general stemming functions) were meant to select the appropriate 
	 * stem function depending on the language selected. As this SOCAL implementation is designed 
	 * to work only with the English language, they are not useful, but they're implemented in order 
	 * to make it easier to translate methods that use them. 
	 */
	
	private String stemNoun(String noun){
		return this.stemNN(noun);
	}
	
	private String stemAdvToAdj(String adverb){
		return this.stemRBToJJ(adverb);
	}
	
	private String stemSuperAdj(String adj){
		return this.stemSuperJJ(adj);
	}
	
	// General functions
	
	private String getWord(String[] pair){
		return pair[0]; //get word from (word, tag) pair
	}
	
	private String getTag(String[] pair){
		return pair[1].length()>=2?pair[1]:pair[1]+"  "; //get tag from (word, tag) pair
	}
	
	// gives the total count in a word count dictionary
	private int sum_word_counts(Map<String, Integer> word_count_dict){
		int count = 0;
		for(Map.Entry<String, Integer> entry : word_count_dict.entrySet()){
			count += entry.getValue();
		}
		return count;
	}
	
	/* this function determines whether the given index is the last word (or,
	 * trivially, the only word) in an intensifier. If so, it returns a list
	 * containing, as its first element, the length of the intensifier and,
	 * as its second element, the modifier from the relevant intensifier dictionary
	 */
	private double[] findIntensifier(int index){ 
		double returnValue[] = new double[2];  
		if(index < 0 || index >= this.text.size() || this.getTag(this.getTextFromIndex(index)).equals("MOD")) // Already modifying something
			return null;
		
		if(this.cIntDictionary.containsKey(this.getWord(this.getTextFromIndex(index)).toLowerCase())){ // Might be complex
			for(Map.Entry<String[][], Double> wordModPair : this.cIntDictionary.get(this.getWord(this.getTextFromIndex(index)).toLowerCase()).entrySet()){ 
				List <String> tempWordsListDict = new ArrayList<>(); // Return words from intensifier multiword array
				for(int i = 0; i < wordModPair.getKey().length -1; i++)
					tempWordsListDict.add(wordModPair.getKey()[i][1]);
				
				List <String> tempWordsListText = new ArrayList<>(); // Return words from text
				for(int i = index - wordModPair.getKey().length +1; i < index; i++)
					tempWordsListText.add(this.getWord(this.getTextFromIndex(i)).toLowerCase());
				
				if(tempWordsListDict.equals(tempWordsListText)) {
					returnValue[0] = wordModPair.getKey().length;
					returnValue[1] = wordModPair.getValue();
					return returnValue;
				}
					
			}
		}
		
		if(this.intDictionary.containsKey(this.getWord(this.getTextFromIndex(index)).toLowerCase())) {
			double modifier = this.intDictionary.get(this.getWord(this.getTextFromIndex(index)).toLowerCase());
			if(StringUtils.isAllUpperCase(this.getWord(this.getTextFromIndex(index))) && this.configFlags.get("use_cap_int"))
				modifier *= this.configModifiers.get("capital_modifier");
			returnValue[0] = 1;
			returnValue[1] = modifier;
		}
		return null;	
	}
	
	/** this function recursively matches the (partial) multi-word dictionary entry
	 * (words) with the corresponding part of the text (from index) the function 
	 * returns a list containing the number of words matched (or -1 if the match 
	 * failed) and the value of any intensifier found
	 * 
	 * @author Lucas
	 */
	private double[] matchMultiwordF(int index, String words[][]){
		double[] temp;
		if(words.length == 0){
			return new double[] {0, 0}; //done
		}else{
			String[] current = words[0];
			if(current[0].equals("") && current.length==2)
				current[0] = "1"; // Unmodified words should be appear once
			if(current[0].equals("0"))
				return this.matchMultiwordF(index, Arrays.copyOfRange(words, 1, words.length)); // This word done
			if(current[0].equals("?") || current[0].equals("*")) { // Word optional - try
				temp = this.matchMultiwordF(index, Arrays.copyOfRange(words, 1, words.length));
				if(temp[0] != -1)
					return temp;
			}
			if(index == this.text.size()) 
				return new double[] {-1, 0};
			
			boolean match = false;
			for(String wordOrTag : Arrays.copyOfRange(current, 1, current.length)) {
				if(StringUtils.isAllLowerCase(wordOrTag)) // Match by word
					match = (match || getWord(this.getTextFromIndex(index)).toLowerCase() == wordOrTag);
				else if(StringUtils.isAllUpperCase(wordOrTag)) { // Match by tag
					if(wordOrTag.equals("INT")) { // If looking for a intensifiers 
						int i = 1;
						while(index + i < this.text.size() && !this.sentPunct.contains(this.getTextFromIndex(index+i)[0])) {
							double[] intensifier = findIntensifier(index + i - 1);
							if(intensifier!= null && intensifier[0] == i) {
								double result[] = this.matchMultiwordF(index + i, Arrays.copyOfRange(words, 1, words.length));
								if (result[0] != -1)
                                    return new double[] {result[0] + i, intensifier[1]};
							}
							i += 1;
						}
					} else
						match = (match || getTag(this.getTextFromIndex(index)) == wordOrTag);
				}
			}
			if(!match)
				return new double[] {-1, 0};
			else {
				if(current[0].equals("*")) {
					temp = this.matchMultiwordF(index + 1, words);
				} else if (current[0].equals("+")) {
					
					String[][] tempMatrix = new String[words.length][];
					for(int i = 0; i < words.length; i++) {
						tempMatrix[i] = new String[words[i].length];
						for(int j = 0 ; j < words[i].length; j++) {
							tempMatrix[i][j] = words[i][j];
						}
					}
					tempMatrix[0][0] = "*";
					
					temp = this.matchMultiwordF(index + 1, tempMatrix);
				} else if (current[0].equals("?")) {
					temp = this.matchMultiwordF(index + 1, Arrays.copyOfRange(words, 1, words.length));
				} else {
					String[][] tempMatrix = new String[words.length][];
					for(int i = 0; i < words.length; i++) {
						tempMatrix[i] = new String[words[i].length];
						for(int j = 0 ; j < words[i].length; j++) {
							tempMatrix[i][j] = words[i][j];
						}
					}
					
					tempMatrix[0][0] = String.valueOf(Integer.parseInt(tempMatrix[0][0]) - 1);
					temp = this.matchMultiwordF(index + 1, tempMatrix);
				}
				
				if(temp[0] == -1) // failed
					return temp;
				else // success
					return new double[] {temp[0] + 1.0, temp[1]};
			}
		}
	}

	@Override
	public String getName(){
		return "SoCal";
	}
}