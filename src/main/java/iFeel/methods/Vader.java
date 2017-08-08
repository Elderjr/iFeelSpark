package iFeel.methods;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iFeel.Method;
import util.Utils;

/**
 * @author's matheus, jpaulo
 * This implementation is based on C.J Hutto Python Implementation
 */
public class Vader extends Method {

	private static final double THRESHOLD_NEUTRAL = 0.5; 
	private static final double bIncr = 0.293; //empirically derived mean sentiment intensity rating increase for booster words
	private static final double bDecr = -bIncr; //same above
	private static final double cIncr = 0.733; //empirically derived mean sentiment intensity rating increase for using ALLCAPs to emphasize a word

	private boolean isCapDiff;
	private List<String> negate;
	private List<String> puncList;
	private Map<String, Double> boosterDict;
	private Map<String, Double> specialCaseIdioms;
	private Map<String, Double> wordValenceDict; //lexicon
	private String dictionaryFilePath;

	public Vader(String dictionaryFilePath) {

		this.dictionaryFilePath = dictionaryFilePath;

		loadDictionaries();
		loadAuxCollections();
	}

	@Override
	public void loadDictionaries() {

		this.wordValenceDict = new HashMap<>(); //token and score

		try {
			File f = new File(this.dictionaryFilePath);			
			BufferedReader br = new BufferedReader(new FileReader(f));

			String line = br.readLine();
			while (line != null) {
				//parse Line
				String[] lineItems = line.split("\t");
				this.wordValenceDict.put(lineItems[0], Double.valueOf(lineItems[1]));

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int analyseText(String text) {

		Map<String, Double> sentiments = sentiment(text);
		//System.out.println(sentiments);

		double compound = sentiments.get("compound");

		if (compound > THRESHOLD_NEUTRAL) {
			return POSITIVE;
		}
		else if (compound < -THRESHOLD_NEUTRAL) {
			return NEGATIVE;
		}

		return NEUTRAL;
	}

	/**
	 * from VADER's code
	 * @param score
	 * @param alpha
	 * @return
	 */
	private static double normalize(double score, double alpha) {

		double normalScore = score / Math.sqrt((score * score) + alpha);
		return normalScore;
	}

	/**
	 * From VADER's code
	 * @return
	 */
	private List<String> wildCardMatch(String pattern, List<String> listOfStringToMatchAgainst) {

		List<String> listMatched = new ArrayList<>();

		for (String word : listOfStringToMatchAgainst) {

			if (word.matches(pattern)) {
				listMatched.add(word);
			}
		}

		return listMatched;
	}

	/**
	 * From VADER's code, alpha default value is 15.0.
	 * @return <code>normalize(score, 15.0)</code>
	 */
	private static double normalize(double score) {

		return normalize(score, 15d);
	}

	/**
	 * from VADER's code
	 * @param wordList
	 * @return
	 */
	private boolean isALLCAP_differential(List<String> wordList) {

		int countALLCAPS = 0;

		for (String word : wordList) {
			
			if (Utils.isUpperString(word)) {
				++countALLCAPS;
			}
		}

		int capDifferential = wordList.size() - countALLCAPS;

		return (capDifferential > 0 && capDifferential < wordList.size());
	}

	/**
	 * From VADER's code, includeNT default = true.
	 * @return <code>negated(list, true)</code>
	 */
	private boolean negated(List<String> list) { 

		return negated(list, true);
	}

	/**
	 * From VADER's code.
	 * @param list
	 * @param includeNT consider negation part n't (don't, doesn't)
	 * @return whether <code>list</code> is negated.
	 */
	private boolean negated(List<String> list, boolean includeNT) {

		for (String word : this.negate) {
			
			if (list.contains(word)) {
				return true;
			}
		}

		if (includeNT) {
			for (String word : list) {
				if (word.contains("n't")) { //or endsWith("n't") ?
					return true;
				}
			}
		}

		int i = list.indexOf("least"); //index of first occurrence 
		if (i > 0 && !list.get(i-1).equals("at")) {
				return true;
		}

		return false;
	}

	/**
	 * From VADER's code
	 * check if the preceding words increase, decrease, or negate/nullify the valence
	 * @param word
	 * @param valence
	 * @return
	 */
	private double scalarIncDec(String word, double valence) {

		String wordLower = word.toLowerCase();
		double scalar = 0d;
		
		if (this.boosterDict.containsKey(wordLower)) {

			scalar = this.boosterDict.get(wordLower);
			if (valence < 0) {
				scalar *= -1;
			}

			if (Utils.isUpperString(word) && this.isCapDiff) {
				if (valence > 0) {
					scalar += cIncr;
				} else {
					scalar -= cIncr;
				}
			}
		}

		return scalar;
	}

	/** 
	 * Used the same name variables from original.
	 * @return float for sentiment strength based on the input text.
	 * Positive values are positive valence, negative value are negative valence.
	 */
	private Map<String, Double> sentiment(String text) {

		//String textMod;
		List<String> wordsOnly = new ArrayList<>();
		List<Double> sentiments = new ArrayList<>();
		double total = 0d;
		double compound = 0d;
		double pos = 0d;
		double neg = 0d;
		double neu = 0d;
		double s1 = 0f;
		double s2 = 0f;
		double s3 = 0f;
		double nScalar = -0.74;
		//----end declaration
		
		//System.out.println("Analizing:" + text);
		List<String> wordsAndEmoticons = new ArrayList<>(Arrays.asList(text.split(" ")));
		String textMod = Utils.removePunctuation(text);

		//get rid of empty items or single letter "words" like 'a' and 'I' from wordsOnly
		String temp[] = textMod.split(" ");
		for (String str : temp) {
			if (str.length() > 1) {
				wordsOnly.add(str);
			}
		}

		//now remove adjacent & redundant punctuation from [wordsAndEmoticons] while keeping emoticons and contractions
		for (int i = 0; i < wordsOnly.size(); i++) {
			for (int j = 0; j < this.puncList.size(); j++) {

				String p = this.puncList.get(j);
				String word = wordsOnly.get(i);
				String pword = p + word;

				int x1 = Utils.countOccurrences(wordsAndEmoticons, pword);
				while (x1 > 0) {
					int index = wordsAndEmoticons.indexOf(pword);
					if (index < wordsAndEmoticons.size()) {
						//wordsAndEmoticons.erase(wordsAndEmoticons.begin() + index);
						//wordsAndEmoticons.insert(wordsAndEmoticons.begin() + index, word);
						wordsAndEmoticons.set(index, word);

						x1 = Utils.countOccurrences(wordsAndEmoticons, pword);
					}
				}

				String wordp = word + p;
				int x2 = Utils.countOccurrences(wordsAndEmoticons, wordp);
				while (x2 > 0) {
					int index = wordsAndEmoticons.indexOf(wordp);
					if (index < wordsAndEmoticons.size()) {
						//wordsAndEmoticons.erase( wordsAndEmoticons.begin() + index);
						//wordsAndEmoticons.insert( wordsAndEmoticons.begin() + index, word);
						wordsAndEmoticons.set(index, word);

						x2 = Utils.countOccurrences(wordsAndEmoticons, wordp);
					}
				}
			}
		}

		//get rid of residual empty items or single letter "words" like 'a' and 'I' from wordsAndEmoticons
		for (int i = 0; i < wordsAndEmoticons.size(); ++i) {
			if (wordsAndEmoticons.get(i).length() <= 1) {
				//wordsAndEmoticons.erase(wordsAndEmoticons.begin() + i);
				wordsAndEmoticons.remove(i);
			}
		}
		//check for negation

		this.isCapDiff = isALLCAP_differential(wordsAndEmoticons);

		// line 145 vaderSentiment.py
		for (String item : wordsAndEmoticons) {
			double v = 0;
			int i = wordsAndEmoticons.indexOf(item);
			String itemLowercase;

			if ((i < (wordsAndEmoticons.size() - 1)
					&& item.toLowerCase().equals("kind")
					&& wordsAndEmoticons.get(i + 1).toLowerCase().equals("of") )
					|| boosterDict.containsKey(item)) {
				sentiments.add(v);
				continue;
			}

			itemLowercase = item.toLowerCase();

			if (this.wordValenceDict.containsKey(itemLowercase)) {
				//get the sentiment valence
				v = this.wordValenceDict.get(itemLowercase);

				//check if sentiment laden word is in ALLCAPS (while others aren't)
				if (Utils.isUpperString(item) && this.isCapDiff) {
					if (v > 0) {
						v += cIncr;
					} else {
						v -= cIncr;
					}
				}

				//check if the preceding words increase, decrease, or negate/nullify the valence
				if (i > 0 && !this.wordValenceDict.containsKey(wordsAndEmoticons.get(i - 1).toLowerCase()) ) {

					s1 = this.scalarIncDec(wordsAndEmoticons.get(i - 1), v);

					v = v + s1;
					//python: if negated([wordsAndEmoticons[i-1]]): v = v*n_scalar
					List<String> aux = new ArrayList<>();
					aux.add(wordsAndEmoticons.get(i - 1));

					if (this.negated(aux)) {
						v = v * nScalar;
					}
				}

				if (i > 1 && !this.wordValenceDict.containsKey(wordsAndEmoticons.get(i - 2).toLowerCase()) ) {

					s2 = this.scalarIncDec(wordsAndEmoticons.get(i - 2), v);
					if (s2 != 0) {
						s2 = s2 * 0.95;
					}
					v = v + s2;
					//check for special use of 'never' as valence modifier instead of negation
					if (wordsAndEmoticons.get(i - 2).equals("never")
							&& (wordsAndEmoticons.get(i - 1).equals("so")
									|| wordsAndEmoticons.get(i - 1).equals("this"))) {
						v = v * 1.5;
					}
					//otherwise, check for negation/nullification
					else {
						//python: elif negated([wordsAndEmoticons[i-2]]): v = v*n_scalar
						List<String> aux2 = new ArrayList<>();
						aux2.add(wordsAndEmoticons.get(i - 2));
						if (this.negated(aux2)) {
							v = v * nScalar;
						}
					}
				}

				if (i > 2 && this.wordValenceDict.containsKey(wordsAndEmoticons.get(i - 3).toLowerCase()) == false) {
					
					s3 = scalarIncDec(wordsAndEmoticons.get(i - 3), v);
					if (s3 != 0) {
						s3 = s3 * 0.9;
					}

					v = v + s3;
					
					//check for special use of 'never' as valence modifier instead of negation
					if ((wordsAndEmoticons.get(i - 3).equals("never"))
							&& ((wordsAndEmoticons.get(i - 2).equals("so")
									|| wordsAndEmoticons.get(i - 2).equals("this"))
									|| (wordsAndEmoticons.get(i - 1).equals("so")
											|| wordsAndEmoticons.get(i - 1).equals("this")))) {
						v = v * 1.25;
					} else {
						List<String> aux3 = new ArrayList<>();
						aux3.add(wordsAndEmoticons.get(i - 3));
						// otherwise, check for negation/nullification
						if (negated(aux3)) {
							v = v * nScalar;
						}
					}

					//check for special case idioms using a sentiment-laden keyword known to SAGE
					String oneZero = wordsAndEmoticons.get(i - 1) + " " + wordsAndEmoticons.get(i);
					String twoOneZero = wordsAndEmoticons.get(i - 2) + " " + wordsAndEmoticons.get(i - 1) + " " + wordsAndEmoticons.get(i);
					String twoOne = wordsAndEmoticons.get(i - 2) + " " + wordsAndEmoticons.get(i - 1);
					String threeTwoOne = wordsAndEmoticons.get(i - 3) + " " + wordsAndEmoticons.get(i - 2) + " " + wordsAndEmoticons.get(i - 1);
					String threeTwo = wordsAndEmoticons.get(i - 3) + " " + wordsAndEmoticons.get(i - 2);

					if (specialCaseIdioms.containsKey(oneZero)) {
						v = this.specialCaseIdioms.get(oneZero);
					} else if (specialCaseIdioms.containsKey(twoOneZero)) {
						v = this.specialCaseIdioms.get(twoOneZero);
					} else if (specialCaseIdioms.containsKey(twoOne)) {
						v = this.specialCaseIdioms.get(twoOne);
					} else if (specialCaseIdioms.containsKey(threeTwoOne)) {
						v = this.specialCaseIdioms.get(threeTwoOne);
					} else if (specialCaseIdioms.containsKey(threeTwo)) {
						v = this.specialCaseIdioms.get(threeTwo);
					}

					if (wordsAndEmoticons.size() - 1 > i) {
						String zeroOne = wordsAndEmoticons.get(i) + " " + wordsAndEmoticons.get(i + 1);
						if (specialCaseIdioms.containsKey(zeroOne)) {
							v = this.specialCaseIdioms.get(zeroOne);
						}
					}
					if (wordsAndEmoticons.size() - 1 > i + 1) {
						String zeroonetwo = wordsAndEmoticons.get(i) + " " +
								wordsAndEmoticons.get(i + 1) + " " + wordsAndEmoticons.get(i + 2);

						if (specialCaseIdioms.containsKey(zeroonetwo)) {
							v = this.specialCaseIdioms.get(zeroonetwo);
						}
					}

					// check for booster/dampener bi-grams such as 'sort of' or 'kind of'
					if (this.boosterDict.containsKey(threeTwo) || this.boosterDict.containsKey(twoOne)) {
						v = v + bDecr;
					}
				}
				//check for negation case using "least"
				if (i > 1
						&& this.wordValenceDict.containsKey(wordsAndEmoticons.get(i - 1).toLowerCase()) == false
						&& wordsAndEmoticons.get(i - 1).toLowerCase().equals("least")) {
					
					if (wordsAndEmoticons.get(i - 2).toLowerCase() != "at"
							&& wordsAndEmoticons.get(i - 2).toLowerCase().equals("very")) {
						v = v * nScalar;
					}
				} else if (i > 0
						&& this.wordValenceDict.containsKey(wordsAndEmoticons.get(i - 1).toLowerCase())
						&& wordsAndEmoticons.get(i - 1).toLowerCase().equals("least")) {
					v = v * nScalar;
				}

			}

			sentiments.add(v);
		}

		// check for modification in sentiment due to contrastive conjunction 'but'
		int but_i = wordsAndEmoticons.indexOf("but");
		int BUT_i = wordsAndEmoticons.indexOf("BUT");

		if (but_i > -1 || BUT_i > -1) {
			int bi; 
			try {
				wordsAndEmoticons.get(but_i);
				bi = but_i;
			} catch (Exception e) {
				wordsAndEmoticons.get(BUT_i);
				bi = BUT_i;
			}

			for (Double s : sentiments) {
				int si = sentiments.indexOf(s);

				if (si < bi) {
					sentiments.set(si, s * 0.5);
				} else if (si > bi) {
					sentiments.set(si, s * 1.5);
				}
			}
		}

		if (sentiments.size() > 0) {

			float sumS = 0;
			for (int j = 0; j < sentiments.size(); j++) {
				sumS += sentiments.get(j);
			}

			//check for added emphasis resulting from exclamation points (up to 4 of them)
			int epCount = Utils.countChars(text, '!');
			if (epCount > 4) {
				epCount = 4;
			}

			float epAmplifier = epCount * 0.292f; //(empirically derived mean sentiment intensity rating increase for exclamation points)
			if (sumS > 0) {
				sumS += epAmplifier;
			} else if (sumS < 0) {
				sumS -= epAmplifier;
			}

			//check for added emphasis resulting from question marks (2 or 3+)
			int qmCount = Utils.countChars(text, '?');
			float qmAmplifier = 0f;
			if (qmCount > 1) {
				if (qmCount <= 3) {
					qmAmplifier = qmCount * 0.18f;
				} else {
					qmAmplifier = 0.96f;
				}
				if (sumS > 0) {
					sumS += qmAmplifier;
				} else if (sumS < 0) {
					sumS -= qmAmplifier;
				}
			}
			compound = normalize(sumS);
			// want separate positive versus negative sentiment scores
			double posSum = 0.0;
			double negSum = 0.0;
			int neuCount = 0;
			for (double sentiment_score : sentiments) {
				if (sentiment_score > 0) {
					posSum += sentiment_score + 1; // compensates for neutral words that are counted as 1
				}
				if (sentiment_score < 0) {
					negSum += sentiment_score - 1; // when used with math.Math.abs(), compensates for neutrals
				}
				if (sentiment_score == 0) {
					neuCount += 1;
				}
			}

			if (posSum > Math.abs(negSum)) {
				posSum += (epAmplifier + qmAmplifier);
			} else if (posSum < Math.abs(negSum)) {
				negSum -= (epAmplifier + qmAmplifier);
			}

			total = posSum + Math.abs(negSum) + neuCount;
			pos = Math.abs(posSum / total);
			neg = Math.abs(negSum / total);
			neu = Math.abs(neuCount / total);
		} else {
			compound = 0d;
			pos = 0d;
			neg = 0d;
			neu = 0d;
		}

		//return
		Map<String, Double> s = new HashMap<>();
		s.put("compound", Utils.setPrecision(compound, 4));
		s.put("pos", Utils.setPrecision(pos, 3));
		s.put("neg", Utils.setPrecision(neg, 3));
		s.put("neu", Utils.setPrecision(neu, 3));
		
		return s;
	}

	/**
	 * @author jpaulo
	 */
	private void loadAuxCollections() {

		this.puncList = new ArrayList<>();
		this.puncList.add(".");
		this.puncList.add("!");
		this.puncList.add("?");
		this.puncList.add(",");
		this.puncList.add(";");
		this.puncList.add(":");
		this.puncList.add("-");
		this.puncList.add("'");
		this.puncList.add("\\");
		this.puncList.add("!!");
		this.puncList.add("!!!");
		this.puncList.add("??");
		this.puncList.add("???");
		this.puncList.add("?!?");
		this.puncList.add("!?!");
		this.puncList.add("?!?!");
		this.puncList.add("!?!?");

		this.negate = new ArrayList<>();
		this.negate.add("aint");
		this.negate.add("arent");
		this.negate.add("cannot");
		this.negate.add("cant");
		this.negate.add("couldnt");
		this.negate.add("darent");
		this.negate.add("didnt");
		this.negate.add("doesnt");
		this.negate.add("ain't");
		this.negate.add("aren't");
		this.negate.add("can't");
		this.negate.add("coundn't");
		this.negate.add("daren't");
		this.negate.add("didn't");
		this.negate.add("doesn't");
		this.negate.add("dont");
		this.negate.add("hadnt");
		this.negate.add("hasnt");
		this.negate.add("havent");
		this.negate.add("isnt");
		this.negate.add("mightnt");
		this.negate.add("mustnt");
		this.negate.add("neither");
		this.negate.add("don't");
		this.negate.add("hadn't");
		this.negate.add("haven't");
		this.negate.add("isn't");
		this.negate.add("mightn't");
		this.negate.add("mustn't");
		this.negate.add("neednt");
		this.negate.add("needn't");
		this.negate.add("never");
		this.negate.add("none");
		this.negate.add("nope");
		this.negate.add("nor");
		this.negate.add("not");
		this.negate.add("nothing");
		this.negate.add("nowhere");
		this.negate.add("oughtnt");
		this.negate.add("shant");
		this.negate.add("shouldnt");
		this.negate.add("uhuh");
		this.negate.add("wasnt");
		this.negate.add("werent");
		this.negate.add("oughtn't");
		this.negate.add("shan't");
		this.negate.add("shouldn't");
		this.negate.add("uh-uh");
		this.negate.add("wasn't");
		this.negate.add("weren't");
		this.negate.add("without");
		this.negate.add("wont");
		this.negate.add("wouldnt");
		this.negate.add("won't");
		this.negate.add("wouldn't");
		this.negate.add("rarely");
		this.negate.add("seldom");
		this.negate.add("despite");

		//booster/dampener 'intensifiers' or 'degree adverbs' http://en.wiktionary.org/wiki/Category:English_degree_adverbs
		this.boosterDict = new HashMap<>();
		this.boosterDict.put("absolutely", bIncr);
		this.boosterDict.put("amazingly", bIncr);
		this.boosterDict.put("awfully", bIncr);
		this.boosterDict.put("completely", bIncr);
		this.boosterDict.put("considerably", bIncr);
		this.boosterDict.put("decidedly", bIncr);
		this.boosterDict.put("deeply", bIncr);
		this.boosterDict.put("effing", bIncr);
		this.boosterDict.put("enormously", bIncr);
		this.boosterDict.put("entirely", bIncr);
		this.boosterDict.put("especially", bIncr);
		this.boosterDict.put("exceptionally", bIncr);
		this.boosterDict.put("extremely", bIncr);
		this.boosterDict.put("fabulously", bIncr);
		this.boosterDict.put("flipping", bIncr);
		this.boosterDict.put("flippin", bIncr);
		this.boosterDict.put("fricking", bIncr);
		this.boosterDict.put("frickin", bIncr);
		this.boosterDict.put("frigging", bIncr);
		this.boosterDict.put("friggin", bIncr);
		this.boosterDict.put("fully", bIncr);
		this.boosterDict.put("fucking", bIncr);
		this.boosterDict.put("greatly", bIncr);
		this.boosterDict.put("hella", bIncr);
		this.boosterDict.put("highly", bIncr);
		this.boosterDict.put("hugely", bIncr);
		this.boosterDict.put("incredibly", bIncr);
		this.boosterDict.put("intensely", bIncr);
		this.boosterDict.put("majorly", bIncr);
		this.boosterDict.put("more", bIncr);
		this.boosterDict.put("most", bIncr);
		this.boosterDict.put("particularly", bIncr);
		this.boosterDict.put("purely", bIncr);
		this.boosterDict.put("quite", bIncr);
		this.boosterDict.put("really", bIncr);
		this.boosterDict.put("remarkably", bIncr);
		this.boosterDict.put("so", bIncr);
		this.boosterDict.put("substantially", bIncr);
		this.boosterDict.put("thoroughly", bIncr);
		this.boosterDict.put("totally", bIncr);
		this.boosterDict.put("tremendously", bIncr);
		this.boosterDict.put("uber", bIncr);
		this.boosterDict.put("unbelievably", bIncr);
		this.boosterDict.put("unusually", bIncr);
		this.boosterDict.put("utterly", bIncr);
		this.boosterDict.put("very", bIncr);

		this.boosterDict.put("almost", bDecr);
		this.boosterDict.put("barely", bDecr);
		this.boosterDict.put("hardly", bDecr);
		this.boosterDict.put("just enough", bDecr);
		this.boosterDict.put("kind of", bDecr);
		this.boosterDict.put("kinda", bDecr);
		this.boosterDict.put("kindof", bDecr);
		this.boosterDict.put("kind-of", bDecr);
		this.boosterDict.put("less", bDecr);
		this.boosterDict.put("little", bDecr);
		this.boosterDict.put("marginally", bDecr);
		this.boosterDict.put("occasionally", bDecr);
		this.boosterDict.put("partly", bDecr);
		this.boosterDict.put("scarcely", bDecr);
		this.boosterDict.put("slightly", bDecr);
		this.boosterDict.put("somewhat", bDecr);
		this.boosterDict.put("sort of", bDecr);
		this.boosterDict.put("sorta", bDecr);
		this.boosterDict.put("sortof", bDecr);
		this.boosterDict.put("sort-of", bDecr);

		//check for special case idioms using a sentiment-laden keyword known to SAGE
		this.specialCaseIdioms = new HashMap<>();
		this.specialCaseIdioms.put("the shit", 3d);
		this.specialCaseIdioms.put("the bomb", 3d);
		this.specialCaseIdioms.put("bad ass", 1.5);
		this.specialCaseIdioms.put("yeah right", -2d);
		this.specialCaseIdioms.put("cut the mustard", 2d);
		this.specialCaseIdioms.put("kiss of death", -1.5);
		this.specialCaseIdioms.put("hand to mouth", -2d);
		//future work: consider other sentiment-laden idioms
		//other_idioms = {"back handed": -2, "blow smoke": -2, "blowing smoke": -2, "upper hand": 1, "break a leg": 2,
		//"cooking with gas": 2, "in the black": 2, "in the red": -2, "on the ball": 2,"under the weather": -2}
	}
	
	@Override
	public String getName(){
		return "Vader";
	}
}
