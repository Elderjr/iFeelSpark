package iFeel.methods;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iFeel.Method;
import util.Utils;

/**
 * This method was ported from Sasa Python's library code, version 0.1.3.
 * @author jpaulo
 */
public class Sasa extends Method implements Serializable{

	private Map<String, Map<String, Double>> dictionary;
	private String dictionaryFilePath;

	//regular expressions to normalize text for SASA algorithm
	private String urlRegex;
	private String emoPosRegex;
	private String emoNegRegex;
	private String emoRegex;
	private String repeatRegex;
	private String lowerCaseRegex;
	private String emoticonRegex;
	private Pattern regexStringsPattern; //to tokenize

	public Sasa(String dictionaryFilePath) {
		this.dictionaryFilePath = dictionaryFilePath;
		this.loadDictionaries();
		this.loadSasaRegex();
	}
	
	private void loadSasaRegex() {
		
		ResourceBundle rb = ResourceBundle.getBundle("bundles.sasa_regex");
		this.urlRegex = rb.getString("URL");
		this.emoPosRegex = rb.getString("EMO_POS");
		this.emoNegRegex = rb.getString("EMO_NEG");
		this.emoRegex = rb.getString("EMO");
		this.repeatRegex = rb.getString("REPEAT");
		this.lowerCaseRegex = rb.getString("LOWERCASE");
		//this.repeatPattern = Pattern.compile(this.repeatRegex);
		//this.lowerCasePattern = Pattern.compile(this.lowerCaseRegex);

		this.emoticonRegex = rb.getString("EMOTICON");

		StringBuilder sb = new StringBuilder();
		sb.append("(")
				.append(rb.getString("PHONE_NUMBERS")).append("|")
				.append(this.emoticonRegex).append("|")
				.append(rb.getString("HTML_TAGS")).append("|")
				.append(rb.getString("TWITTER_USERNAME")).append("|")
				.append(rb.getString("TWITTER_HASHTAGS")).append("|")
				.append(rb.getString("REMAINING_WORD_TYPES"))
				.append(")");
		
		this.regexStringsPattern = Pattern.compile(sb.toString());
		//System.out.println("p: " + this.regexStringsPattern.pattern());
	}
	
	@Override
	public void loadDictionaries() {
		
		this.dictionary = new HashMap<>();

		try {
			File f = new File(this.dictionaryFilePath);
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				
				String[] data = line.split("\t");				
				String feature = data[0];

				for (int i=1; i<data.length; ++i) {
					
					String[] polarityAndProb = data[i].split(":");
					String polarity = polarityAndProb[0];
					Double prob = Double.valueOf(polarityAndProb[1]);

					if (!this.dictionary.containsKey(feature)) {

						Map<String, Double> map = new HashMap<>();
						map.put(polarity, prob);
						this.dictionary.put(feature, map);
					}
					else {

						this.dictionary.get(feature).put(polarity, prob);
					}
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int analyseText(String text) {

		List<String> tokens = normalize(tokenize(text));
		
		//original SASA's code includes tokens <s> and </s> in the tokens list
		tokens.add(0, "<s>"); //first element
		tokens.add("</s>"); //last element

		Set<String> feats = new HashSet<>();
		for (String token : tokens) {
			feats.add(token);
		}

		//for each sentiment
//		Map<String, Double> probabilityByPolarity = new HashMap<>();
		Map<String, Double> logProbByPolarity = new HashMap<>();
		
		for (String token : feats) {

			if (this.dictionary.containsKey(token)) {

				for (String polarity : this.dictionary.get(token).keySet()) {

					//double prob = this.dictionary.get(token).get(polarity);
					double logProb = Utils.log2(this.dictionary.get(token).get(polarity));
					
					//if (probabilityByPolarity.containsKey(polarity)) {
					if (logProbByPolarity.containsKey(polarity)) {
						//double curr = probabilityByPolarity.get(polarity);
						//probabilityByPolarity.put(polarity, prob * curr);
						double curr = logProbByPolarity.get(polarity);
						logProbByPolarity.put(polarity, logProb + curr);					
					}
					else {
						//from SASA code: probability of each of the 4 polarities starts with 0.25
						//probabilityByPolarity.put(polarity, prob * 0.25);
						logProbByPolarity.put(polarity, logProb + Utils.log2(0.25));
					}
				}
			}
		}

//		normalizeProbabilitiesToSumOne(probabilityByPolarity);
		normalizeLogProbabilities(logProbByPolarity);
		
		String maxProbabilityPolarity = null;
		double maxProbabilityValue = Float.NEGATIVE_INFINITY;
		//double maxProbabilityValue = 0d;

//		for (String polarity : probabilityByPolarity.keySet()) {
//			double probability = probabilityByPolarity.get(polarity);
		for (String polarity : logProbByPolarity.keySet()) {
			double probability = logProbByPolarity.get(polarity);
			
			if (probability > maxProbabilityValue) {
				maxProbabilityValue = probability;
				
				if (!polarity.equals(maxProbabilityPolarity)) {
					maxProbabilityPolarity = polarity;
				}
			}
		}

		switch (maxProbabilityPolarity) {
		case "positive":
			return POSITIVE; //strength must be set to maxProbabilityValue
		case "negative":
			return NEGATIVE; //strength must be set to -maxProbabilityValue
		case "neutral":
		case "unsure":
		default:
			return NEUTRAL; //strength must be set to 0
		}
	}
	
	/**
	 * changes mapLogProbs logs such that probabilities sum 1
	 */
	private static void normalizeLogProbabilities(Map<String, Double> mapLogProbs) {

		double sumLog = sumLogsArguments(new ArrayList<>(mapLogProbs.values()));

		if (sumLog <= Float.NEGATIVE_INFINITY) {
			double logp = Utils.log2(1d / mapLogProbs.size());
			for (String key : mapLogProbs.keySet()) {
				mapLogProbs.put(key, logp);
			}
		}
		else {
			for (String key : mapLogProbs.keySet()) {
				mapLogProbs.put(key, mapLogProbs.get(key) - sumLog);
			}
		}
	}

	/**
	 * changes mapProbs values so that their sum is equals to 1
	 */
	private static void normalizeProbabilitiesToSumOne(Map<String, Double> mapProbs) {

		//sum
		double sum = 0d;
		for (double prob : mapProbs.values()) {
			sum += prob;
		}

		if (sum == 0d) {
			double p = 1d / mapProbs.size(); 
			for (String key : mapProbs.keySet()) {
				mapProbs.put(key, p);
			}
		}
		else {
			double factor = 1d / sum; 
			//normalizing prob's values
			for (String key : mapProbs.keySet()) {
				mapProbs.put(key, mapProbs.get(key) * factor);
			}
		}
	}

	/**
	 * from sasa.normalizer.N1 
	 */
	private List<String> normalize(List<String> tokens) {
		
		List<String>norms = new ArrayList<>();
		for (String token : tokens) {

			String temp = token.replaceAll(this.urlRegex, "URL").replaceAll(this.emoPosRegex, "EMOTICON+")
					.replaceAll(this.emoNegRegex, "EMOTICON-").replaceAll(this.emoRegex, "EMOTICON");

			//if whole token is formed by a single repeated char different from a "word": 
			if (temp.matches(this.repeatRegex)) {
				temp = temp.charAt(0) + "_REPEAT";
			}

			//if the token has a lowercase letter, the whole token is converted to lowercase 
			if (temp.matches(this.lowerCaseRegex)) {
				temp = temp.toLowerCase();
			}

			norms.add(temp);
		}

		return norms;
	}
	
	/**
	 * from sasa.happyfuntokenizing.Tokenizer 
	 * Value: a tokenized list of strings; concatenating this list returns the original string if preserve_case=False
	 */
	private List<String> tokenize(String text) {

		List<String> words = new ArrayList<>();
		Matcher m = this.regexStringsPattern.matcher(text);

		while (m.find()) {
			String grp = m.group();
			//System.out.println("gr: " + grp);

			//possible alter the case, but avoid changing emoticons like :D into :d
			if (text.matches(this.emoticonRegex)) {
				words.add(grp);
			} else {
				words.add(grp.toLowerCase());
			}
		}

		return words;
	}

	/**
	 * From NLTK probability.py add_logs
	 * @param logx a value: log of x at base 2
	 * @param logy a value: log of x at base 2
	 * @return return log(x+y).
	 * Conceptually, this is the same as returning log(2^(logx) + 2^(logy))
	 * @return
	 */
	private static double sumLogsArguments(double logx, double logy) {
		
		if (logx < Float.NEGATIVE_INFINITY) {
			return logy;
		}
		else if (logy < Float.NEGATIVE_INFINITY) {
			return logx;
		}
		
		//approach to avoid overflow: log(2^(logx - m) + 2^(logy - m)) + m, where m = max(x,y)
		double base = Math.max(logx, logy);
		return base + Utils.log2( Math.pow(2, logx - base) + Math.pow(2, logy - base) );
	}

	/**
	 * From NLTK probability.py sum_logs
	 * @return result of calling sumLogsArguments(logx, logy) with logs
	 */
	private static double sumLogsArguments(List<Double> logs) {
		
		double aux = logs.get(0);
		for (int i=1; i < logs.size(); ++i) {
			aux = sumLogsArguments(aux, logs.get(i));
		}
		
		return aux;
	}

	/**
	 * just to debug
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
	
	public String getName(){
		return "Sasa";
	}
}
