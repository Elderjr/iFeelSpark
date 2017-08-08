package iFeel.methods;
import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iFeel.Method;
import util.NLPUtil;
import util.Utils;

/**
 * @author jpaulo
 * Lexicons developed by Saif Mohammad. http://saifmohammad.com/WebPages/lexicons.html.
 * Must be an abstract class to force concrete methods to extend it,
 * despite all functions were already been implemented here.
 */
public abstract class MohammadSentimentLexiconMethod extends Method {

	/**
	 * key: a String representing an unigram;
	 * value: the unigram's score
	 */
	protected Map<String, Double> unigramsDictionary;

	/**
	 * key: a String representing a bigram, in the format "unigram<space>unigram"; 
	 * value: the bigram's score
	 */
	protected Map<String, Double> bigramsDictionary;

	/**
	 * key: a String representing a pair of non-contiguous ngrams, formated
	 * as 'ngram---ngram', where ngram can be a unigram or a bigram; 
	 * value: the pair's score
	 */
	protected Map<String, Double> pairsDictionary;

	private String unigramsDictionaryFilePath;
	private String bigramsDictionaryFilePath;
	private String pairsDictionaryFilePath;

	public MohammadSentimentLexiconMethod(String unigramsDictionaryFilePath, 
			String bigramsDictionaryFilePath, String pairsDictionaryFilePath) {

		this.unigramsDictionaryFilePath = unigramsDictionaryFilePath;
		this.bigramsDictionaryFilePath = bigramsDictionaryFilePath;
		this.pairsDictionaryFilePath = pairsDictionaryFilePath;
		this.loadDictionaries();
	}

	/**
	 * 
	 */
	@Override
	public void loadDictionaries() {

		this.unigramsDictionary = new HashMap<>();			
		this.bigramsDictionary = new HashMap<>();			
		this.pairsDictionary = new HashMap<>();			

		loadDictionary(this.unigramsDictionary, this.unigramsDictionaryFilePath);
		loadDictionary(this.bigramsDictionary, this.bigramsDictionaryFilePath);
		loadDictionary(this.pairsDictionary, this.pairsDictionaryFilePath);
	}

	/**
	 * 
	 */
	private void loadDictionary(Map<String, Double> dictionary, String dictionaryFilePath) {
		
		try {
//			int cont = 1;
			double maxPos = 0d, minNeg = 0d;
			File f = new File(dictionaryFilePath);			
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				
				String[] data = line.split("\t"); //[0]: sentiment ngram; [1]: sentiment score

				if (!dictionary.containsKey(data[0])) {
					dictionary.put(data[0], Double.valueOf(data[1]));
					
//					double temp = dictionary.get(data[0]);
//					if (temp > 0d && temp > maxPos) maxPos = temp;
//					else if (temp < 0d && temp < minNeg) minNeg = temp;
				}
//				else {
//					System.out.println(cont + ") " + line);
//					break;
//				}
/* Errors with special characters in Sentiment140 files:
unigrams file, line 313: ????????????	5	5	0
bigrams file, line 2629: ? ????	5	6	0
pairs file, line 20303: good---???	5	16	2
*/
//				++cont;
				line = br.readLine();
			}
			
			//System.out.println("Max-pos: " + maxPos + "; Max-neg: " + minNeg);

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Algorithm: 
	 * look for all pairs and sum its scores;
	 * remove all unigrams and bigrams founded by pairs;
	 * look for all bigrams;
	 * remove all unigrams founded in bigrams.
	 *
	 * SEE http://stackoverflow.com/questions/25377666/calculating-the-sentiment-score-of-a-sentence-using-ngrams
	 * Example sum bigrams and unigrams scores, without removing any.
	 */
	@Override
	public int analyseText(String text) {

		double scoreTotal = 0d;
		String[] tokens = Utils.removePunctuation(text).toLowerCase().split(" ");
		List<String> unigrams = Arrays.asList(tokens);
		List<String> bigrams = NLPUtil.bigrams(unigrams);

		int qtUnigrams = unigrams.size();
		int qtBigrams  = bigrams.size();
		
		Set<Integer> unigramsIndexesToRemove = new HashSet<Integer>();
		Set<Integer> bigramsIndexesToRemove  = new HashSet<Integer>();

		//pairs' score (paper text: "480,010 non-contiguous pairs."). 
		//pairs unigram-unigram
		scoreTotal += this.pairsAux(unigrams, 0, qtUnigrams - 2, unigramsIndexesToRemove,
				unigrams, 2, qtUnigrams, unigramsIndexesToRemove);

		//pairs unigram-bigram
		scoreTotal += this.pairsAux(unigrams, 0, qtUnigrams - 2, unigramsIndexesToRemove,
				bigrams, 2, qtBigrams, bigramsIndexesToRemove);
		
		//pairs bigram-unigram
		scoreTotal += this.pairsAux(bigrams, 0, qtBigrams - 2, bigramsIndexesToRemove, 
				unigrams, 3, qtUnigrams, unigramsIndexesToRemove);

		//pairs bigram-bigram
		scoreTotal += this.pairsAux(bigrams, 0, qtBigrams - 2, bigramsIndexesToRemove, 
				bigrams, 3, qtBigrams, bigramsIndexesToRemove);
		
		//remove bigrams found in pairs from bigrams list
		this.removeElementsByIndexes(bigrams, bigramsIndexesToRemove);

		//bigrams' score
		int i = 0;
		for (String bigram : bigrams) {

			if (bigram != null && this.bigramsDictionary.containsKey(bigram)) {
				//System.out.println("bigram found: [" + i + "]" + bigram + " = " + this.bigramsDictionary.get(bigram));
				scoreTotal += this.bigramsDictionary.get(bigram);

				//add indexes to remove respective elements in unigrams list
				unigramsIndexesToRemove.add(i);
				unigramsIndexesToRemove.add(i+1);
			}
			
			++i;
		}

		this.removeElementsByIndexes(unigrams, unigramsIndexesToRemove);

		//unigrams' score
		i = 0;
		for (String unigram : unigrams) {
			if (unigram != null && this.unigramsDictionary.containsKey(unigram)) {
				//System.out.println("unigram found: [" + i + "]" + unigram + " = " + this.unigramsDictionary.get(unigram));
				scoreTotal += this.unigramsDictionary.get(unigram);
			}
			++i;
		}

		//System.out.println("Score of sentence '" + text + "': " + scoreTotal);

		//Ad-hoc for this method: TODO define trashold < 0.5 should be neutral? What about threshold 1.0 value?
		double threshold = 0.5; //1d, 0.75 ?? 

		if (scoreTotal > threshold) {
			return POSITIVE;
		}
		else if (scoreTotal < -threshold) {
			return NEGATIVE;
		}

		return NEUTRAL; //neutral OR N/A
	}
	
	/**
	 * @param ngramsList (uni- or bi-grams list)
	 * @param ngramsIndexesToRemove
	 * Set null to all listed indexes.
	 */
	private void removeElementsByIndexes(List<String> ngramsList, Set<Integer> ngramsIndexesToRemove) {
		
		for (int index : ngramsIndexesToRemove) {
			
			ngramsList.set(index, null);
			//System.out.print(index + " ");
		}
	}

	/**
	 * @param list1 (uni- or bi-grams)
	 * @param list2 (uni- or bi-grams)
	 * I- inclusive indexation ; F- exclusive indexation
	 * @return score/strength of all pairs formed by lists' elements, given indexes
	 */
	private double pairsAux(List<String> list1, int index1I, int index1F, Set<Integer> list1IndexesToRemove,
			List<String> list2, int index2I, int index2F, Set<Integer> list2IndexesToRemove) {

		double scoreTotal = 0d;		
		for (int i1=index1I; i1 < index1F; ++i1) {
			
			for (int i2=index2I + i1; i2 < index2F; ++i2) {
				//pair is represented as ngram---ngram in dictionary
				String pair = new String(list1.get(i1) + "---" + list2.get(i2));
				
				if (this.pairsDictionary.containsKey(pair)) {
					//System.out.println("Pair found: [" + i1 + "]" + pair + "[" + i2 + "] = " + this.pairsDictionary.get(pair));
					scoreTotal += this.pairsDictionary.get(pair);

					//set indexes to remove elements from bigrams and unigrams lists
					list1IndexesToRemove.add(i1);
					list2IndexesToRemove.add(i2);
				}
			}
		}
		
		return scoreTotal;
	}

	/**
	 * just to verify
	 */
	protected void printDictionary(Map<String, Double> dic) {

		List<String> keys = new ArrayList<String>();
		keys.addAll(dic.keySet());
		Collections.sort(keys);
		int count = 0;
		for (String k : keys) {
			System.out.println(++count + ") [" + k + "] = " + dic.get(k));
		}
	}
	
	@Override
	public String getName(){
		return "MohammadSentimentLexiconMethod";
	}
}
