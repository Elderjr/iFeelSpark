package iFeel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.spark.Partition;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import iFeel.methods.SentiWordNet;
import scala.Tuple2;

/**
 * @author jpaulo
 * sentence-level sentiment analysis method
 */
public abstract class Method implements Serializable{

	//methods ID
	public static final int AFINN = 0;
	public static final int EMOLEX = 1;
	public static final int EMOTICONDS = 2;
	public static final int EMOTICONS = 3;
	public static final int HAPPINESS_INDEX = 4;
	public static final int MOHAMMAD_SENTIMENT_LEXICON = 5;
	public static final int MPQA = 6;
	public static final int OPINION = 7;
	public static final int PANAST = 8;
	public static final int SANN = 9;
	public static final int SASA = 10;
	public static final int SENTIC_NET = 11;
	public static final int SENTIMENT_140 = 12;
	public static final int SENTI_STRENGTH = 13;
	public static final int SENTI_WORD_NET = 14;
	public static final int SO_CAL = 15;
	public static final int STANFORD = 16;
	public static final int UMIGON = 17;
	public static final int VADER = 18;
	public static final int NRC_HashTag = 19;
	
	//RESULT
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	public static final int NEUTRAL = 0;
	
	/**
	 * must load all lexicon needed to analyse sentences
	 */
	public abstract void loadDictionaries();

	/**
	 * @return the polarity
	 */
	public abstract int analyseText(String text);

	
	
	public List<Integer> analyseFile(String filePath) {
		try {
			List<Integer> results = new ArrayList<>();
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = br.readLine();
			while (line != null) {
				int result = 0;
				try {
					result = this.analyseText(line);
				} catch (OutOfMemoryError exception) {
					result = 0;
				}
				results.add(result);
				line = br.readLine();
			}
			br.close();
			return results;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * same of <code>analyseFile(String)</code>, but using Spark
	 */
	public List<Tuple2<Integer, Integer>> analyseFile(JavaSparkContext sc, String filePath) throws IOException{
		final Method method = this;
		JavaRDD<String> textFile = sc.textFile(filePath);
		JavaPairRDD<Integer, Integer> result = textFile.mapToPair(new PairFunction<String, Integer, Integer>() {
			@Override
			public Tuple2<Integer, Integer> call(String phrase) throws Exception {
				return new Tuple2<>(method.analyseText(phrase), 1);
			}
		}).reduceByKey(new Function2<Integer, Integer, Integer>() {
			@Override
			public Integer call(Integer a, Integer b) throws Exception {
				return a + b;
			}
		});
		return result.collect();
	}
	
	public abstract String getName();
}
